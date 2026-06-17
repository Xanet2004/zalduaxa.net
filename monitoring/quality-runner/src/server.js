import http from "node:http";
import { URL } from "node:url";

import AxeBuilder from "@axe-core/playwright";
import { chromium } from "playwright";
import { launch as launchChrome } from "chrome-launcher";
import lighthouse from "lighthouse";

const PORT = Number.parseInt(process.env.QUALITY_RUNNER_PORT ?? "9102", 10);
const FRONTEND_BASE_URL = normalizeBaseUrl(process.env.QUALITY_FRONTEND_BASE_URL ?? "http://frontend:5173");
const INTERVAL_SECONDS = Number.parseInt(process.env.QUALITY_RUNNER_INTERVAL_SECONDS ?? "900", 10);
const ROUTE_TIMEOUT_MS = Number.parseInt(process.env.QUALITY_ROUTE_TIMEOUT_MS ?? "30000", 10);
const LIGHTHOUSE_ENABLED = (process.env.QUALITY_LIGHTHOUSE_ENABLED ?? "true") === "true";
const AXE_ENABLED = (process.env.QUALITY_AXE_ENABLED ?? "true") === "true";
const RUN_ONCE = (process.env.QUALITY_RUN_ONCE ?? "false") === "true";

const ROUTES = parseCsv(process.env.QUALITY_ROUTES ?? "/,/projects,/login,/signup,/__quality_not_found__");
const LIGHTHOUSE_ROUTES = new Set(
  parseCsv(process.env.QUALITY_LIGHTHOUSE_ROUTES ?? ROUTES.join(","))
);

let latestMetrics = buildInitialMetrics();
let lastRunStartedAt = 0;
let lastRunFinishedAt = 0;
let isRunning = false;

function normalizeBaseUrl(value) {
  return value.endsWith("/") ? value.slice(0, -1) : value;
}

function parseCsv(value) {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function escapeLabel(value) {
  return String(value)
    .replaceAll("\\", "\\\\")
    .replaceAll("\n", "\\n")
    .replaceAll('"', '\\"');
}

function metric(name, labels, value) {
  const labelEntries = Object.entries(labels ?? {});
  const labelText = labelEntries.length
    ? `{${labelEntries.map(([key, val]) => `${key}="${escapeLabel(val)}"`).join(",")}}`
    : "";

  const safeValue = Number.isFinite(value) ? value : 0;
  return `${name}${labelText} ${safeValue}`;
}

function routeToUrl(route) {
  const cleanRoute = route.startsWith("/") ? route : `/${route}`;
  return new URL(cleanRoute, `${FRONTEND_BASE_URL}/`).toString();
}

function buildInitialMetrics() {
  return [
    "# HELP quality_runner_up Whether the frontend quality runner process is up.",
    "# TYPE quality_runner_up gauge",
    "quality_runner_up 1",
    "# HELP quality_last_run_success Whether the last frontend quality run succeeded.",
    "# TYPE quality_last_run_success gauge",
    "quality_last_run_success 0",
    "# HELP quality_last_run_timestamp_seconds Unix timestamp of the last completed quality run.",
    "# TYPE quality_last_run_timestamp_seconds gauge",
    "quality_last_run_timestamp_seconds 0"
  ].join("\n") + "\n";
}

async function runRouteChecks(browser, route) {
  const url = routeToUrl(route);

  const result = {
    route,
    url,
    routeUp: 0,
    consoleErrors: 0,
    pageErrors: 0,
    networkErrors: 0,
    axeTotal: 0,
    axeByImpact: {
      critical: 0,
      serious: 0,
      moderate: 0,
      minor: 0,
      unknown: 0
    },
    axeByRule: [],
    error: null
  };

  const context = await browser.newContext({
    viewport: {
      width: 1366,
      height: 768
    },
    ignoreHTTPSErrors: true
  });

  const page = await context.newPage();

  const consoleErrors = [];
  const pageErrors = [];

  page.on("console", (message) => {
    if (message.type() === "error") {
      consoleErrors.push(message.text());
    }
  });

  page.on("pageerror", (error) => {
    pageErrors.push(error.message);
  });

  page.on("response", (response) => {
    if (response.status() >= 500) {
      result.networkErrors += 1;
    }
  });

  try {
    const response = await page.goto(url, {
      waitUntil: "networkidle",
      timeout: ROUTE_TIMEOUT_MS
    });

    await page.waitForSelector("#root", {
      timeout: ROUTE_TIMEOUT_MS
    });

    const bodyText = await page.locator("body").innerText({
      timeout: ROUTE_TIMEOUT_MS
    });

    result.consoleErrors = consoleErrors.length;
    result.pageErrors = pageErrors.length;

    const responseStatus = response?.status() ?? 0;
    const hasBody = bodyText.trim().length > 0;

    result.routeUp = responseStatus < 500 && hasBody ? 1 : 0;

    if (AXE_ENABLED) {
      const axeResults = await new AxeBuilder({ page }).analyze();

      result.axeTotal = axeResults.violations.length;

      for (const violation of axeResults.violations) {
        const impact = violation.impact ?? "unknown";

        if (!Object.hasOwn(result.axeByImpact, impact)) {
          result.axeByImpact[impact] = 0;
        }

        result.axeByImpact[impact] += 1;

        result.axeByRule.push({
          rule: violation.id,
          impact,
          count: violation.nodes.length
        });
      }
    }
  } catch (error) {
    result.error = error instanceof Error ? error.message : String(error);
    result.routeUp = 0;
    result.consoleErrors = consoleErrors.length;
    result.pageErrors = pageErrors.length;
  } finally {
    await context.close();
  }

  return result;
}

async function runLighthouse(route) {
  const url = routeToUrl(route);

  const result = {
    route,
    scores: {},
    metricsMs: {},
    cls: 0,
    error: null
  };

  let chrome;

  try {
    chrome = await launchChrome({
      chromePath: chromium.executablePath(),
      chromeFlags: [
        "--headless",
        "--no-sandbox",
        "--disable-gpu",
        "--disable-dev-shm-usage"
      ]
    });

    const lighthouseResult = await lighthouse(
      url,
      {
        port: chrome.port,
        logLevel: "error",
        output: "json",
        onlyCategories: ["performance", "accessibility", "best-practices", "seo"]
      }
    );

    const lhr = lighthouseResult?.lhr;

    if (!lhr) {
      throw new Error("Lighthouse did not return an LHR result.");
    }

    for (const [categoryName, category] of Object.entries(lhr.categories ?? {})) {
      result.scores[categoryName] = Math.round((category.score ?? 0) * 100);
    }

    const audits = lhr.audits ?? {};

    result.metricsMs["first-contentful-paint"] = audits["first-contentful-paint"]?.numericValue ?? 0;
    result.metricsMs["largest-contentful-paint"] = audits["largest-contentful-paint"]?.numericValue ?? 0;
    result.metricsMs["total-blocking-time"] = audits["total-blocking-time"]?.numericValue ?? 0;
    result.metricsMs["speed-index"] = audits["speed-index"]?.numericValue ?? 0;
    result.cls = audits["cumulative-layout-shift"]?.numericValue ?? 0;
  } catch (error) {
    result.error = error instanceof Error ? error.message : String(error);
  } finally {
    if (chrome) {
      await chrome.kill();
    }
  }

  return result;
}

function buildMetrics({ routeResults, lighthouseResults, startedAt, finishedAt, success }) {
  const durationSeconds = (finishedAt - startedAt) / 1000;

  const lines = [
    "# HELP quality_runner_up Whether the frontend quality runner process is up.",
    "# TYPE quality_runner_up gauge",
    "quality_runner_up 1",

    "# HELP quality_last_run_success Whether the last frontend quality run succeeded.",
    "# TYPE quality_last_run_success gauge",
    metric("quality_last_run_success", {}, success ? 1 : 0),

    "# HELP quality_last_run_timestamp_seconds Unix timestamp of the last completed quality run.",
    "# TYPE quality_last_run_timestamp_seconds gauge",
    metric("quality_last_run_timestamp_seconds", {}, Math.floor(finishedAt / 1000)),

    "# HELP quality_run_duration_seconds Duration of the last frontend quality run.",
    "# TYPE quality_run_duration_seconds gauge",
    metric("quality_run_duration_seconds", {}, durationSeconds),

    "# HELP quality_route_up Whether a route loaded successfully.",
    "# TYPE quality_route_up gauge",

    "# HELP quality_route_console_errors Browser console error count per route.",
    "# TYPE quality_route_console_errors gauge",

    "# HELP quality_route_page_errors Browser page error count per route.",
    "# TYPE quality_route_page_errors gauge",

    "# HELP quality_route_network_errors HTTP 5xx response count per route.",
    "# TYPE quality_route_network_errors gauge",

    "# HELP quality_axe_violations_total Total axe accessibility violations per route.",
    "# TYPE quality_axe_violations_total gauge",

    "# HELP quality_axe_violations_by_impact Axe accessibility violations by impact.",
    "# TYPE quality_axe_violations_by_impact gauge",

    "# HELP quality_axe_violations_by_rule Axe accessibility violations by rule.",
    "# TYPE quality_axe_violations_by_rule gauge"
  ];

  for (const routeResult of routeResults) {
    const labels = {
      route: routeResult.route
    };

    lines.push(metric("quality_route_up", labels, routeResult.routeUp));
    lines.push(metric("quality_route_console_errors", labels, routeResult.consoleErrors));
    lines.push(metric("quality_route_page_errors", labels, routeResult.pageErrors));
    lines.push(metric("quality_route_network_errors", labels, routeResult.networkErrors));
    lines.push(metric("quality_axe_violations_total", labels, routeResult.axeTotal));

    for (const [impact, count] of Object.entries(routeResult.axeByImpact)) {
      lines.push(metric("quality_axe_violations_by_impact", { ...labels, impact }, count));
    }

    for (const rule of routeResult.axeByRule) {
      lines.push(
        metric(
          "quality_axe_violations_by_rule",
          {
            ...labels,
            impact: rule.impact,
            rule: rule.rule
          },
          rule.count
        )
      );
    }
  }

  lines.push("# HELP quality_lighthouse_score Lighthouse category score from 0 to 100.");
  lines.push("# TYPE quality_lighthouse_score gauge");

  lines.push("# HELP quality_lighthouse_metric_ms Lighthouse timing metrics in milliseconds.");
  lines.push("# TYPE quality_lighthouse_metric_ms gauge");

  lines.push("# HELP quality_lighthouse_cls Lighthouse cumulative layout shift.");
  lines.push("# TYPE quality_lighthouse_cls gauge");

  lines.push("# HELP quality_lighthouse_error Whether Lighthouse failed for a route.");
  lines.push("# TYPE quality_lighthouse_error gauge");

  for (const lighthouseResult of lighthouseResults) {
    const labels = {
      route: lighthouseResult.route
    };

    lines.push(metric("quality_lighthouse_error", labels, lighthouseResult.error ? 1 : 0));

    for (const [category, score] of Object.entries(lighthouseResult.scores)) {
      lines.push(metric("quality_lighthouse_score", { ...labels, category }, score));
    }

    for (const [metricName, metricValue] of Object.entries(lighthouseResult.metricsMs)) {
      lines.push(metric("quality_lighthouse_metric_ms", { ...labels, metric: metricName }, metricValue));
    }

    lines.push(metric("quality_lighthouse_cls", labels, lighthouseResult.cls));
  }

  return lines.join("\n") + "\n";
}

async function runQualityChecks() {
  if (isRunning) {
    console.log("[quality-runner] previous run still active, skipping new run");
    return;
  }

  isRunning = true;
  lastRunStartedAt = Date.now();

  console.log(
    JSON.stringify({
      service: "quality-runner",
      event: "quality_run_started",
      frontendBaseUrl: FRONTEND_BASE_URL,
      routes: ROUTES,
      lighthouseEnabled: LIGHTHOUSE_ENABLED,
      axeEnabled: AXE_ENABLED
    })
  );

  const routeResults = [];
  const lighthouseResults = [];

  let success = true;
  let browser;

  try {
    browser = await chromium.launch({
      headless: true,
      args: [
        "--no-sandbox",
        "--disable-dev-shm-usage"
      ]
    });

    for (const route of ROUTES) {
      const routeResult = await runRouteChecks(browser, route);
      routeResults.push(routeResult);

      console.log(
        JSON.stringify({
          service: "quality-runner",
          event: "route_check_finished",
          route,
          routeUp: routeResult.routeUp,
          consoleErrors: routeResult.consoleErrors,
          pageErrors: routeResult.pageErrors,
          networkErrors: routeResult.networkErrors,
          axeTotal: routeResult.axeTotal,
          axeByImpact: routeResult.axeByImpact,
          error: routeResult.error
        })
      );

      if (routeResult.routeUp !== 1) {
        success = false;
      }
    }

    if (LIGHTHOUSE_ENABLED) {
      for (const route of ROUTES) {
        if (!LIGHTHOUSE_ROUTES.has(route)) {
          continue;
        }

        const lighthouseResult = await runLighthouse(route);
        lighthouseResults.push(lighthouseResult);

        console.log(
          JSON.stringify({
            service: "quality-runner",
            event: "lighthouse_check_finished",
            route,
            scores: lighthouseResult.scores,
            metricsMs: lighthouseResult.metricsMs,
            cls: lighthouseResult.cls,
            error: lighthouseResult.error
          })
        );

        if (lighthouseResult.error) {
          success = false;
        }
      }
    }
  } catch (error) {
    success = false;

    console.error(
      JSON.stringify({
        service: "quality-runner",
        event: "quality_run_failed",
        error: error instanceof Error ? error.message : String(error)
      })
    );
  } finally {
    if (browser) {
      await browser.close();
    }

    lastRunFinishedAt = Date.now();

    latestMetrics = buildMetrics({
      routeResults,
      lighthouseResults,
      startedAt: lastRunStartedAt,
      finishedAt: lastRunFinishedAt,
      success
    });

    console.log(
      JSON.stringify({
        service: "quality-runner",
        event: "quality_run_finished",
        success,
        durationSeconds: (lastRunFinishedAt - lastRunStartedAt) / 1000
      })
    );

    isRunning = false;
  }

  if (RUN_ONCE) {
    process.exit(success ? 0 : 1);
  }
}

const server = http.createServer((request, response) => {
  if (request.url === "/metrics" || request.url === "/") {
    response.writeHead(200, {
      "Content-Type": "text/plain; version=0.0.4; charset=utf-8"
    });
    response.end(latestMetrics);
    return;
  }

  if (request.url === "/health") {
    response.writeHead(200, {
      "Content-Type": "application/json; charset=utf-8"
    });
    response.end(JSON.stringify({ status: "UP", running: isRunning }));
    return;
  }

  if (request.url === "/run" && request.method === "POST") {
    runQualityChecks().catch((error) => {
      console.error(
        JSON.stringify({
          service: "quality-runner",
          event: "manual_run_failed",
          error: error instanceof Error ? error.message : String(error)
        })
      );
    });

    response.writeHead(202, {
      "Content-Type": "application/json; charset=utf-8"
    });
    response.end(JSON.stringify({ accepted: true }));
    return;
  }

  response.writeHead(404, {
    "Content-Type": "application/json; charset=utf-8"
  });
  response.end(JSON.stringify({ error: "Not found" }));
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`[quality-runner] listening on 0.0.0.0:${PORT}`);
  console.log(`[quality-runner] frontend base URL: ${FRONTEND_BASE_URL}`);

  runQualityChecks().catch((error) => {
    console.error(
      JSON.stringify({
        service: "quality-runner",
        event: "initial_run_failed",
        error: error instanceof Error ? error.message : String(error)
      })
    );
  });

  if (!RUN_ONCE) {
    setInterval(() => {
      runQualityChecks().catch((error) => {
        console.error(
          JSON.stringify({
            service: "quality-runner",
            event: "scheduled_run_failed",
            error: error instanceof Error ? error.message : String(error)
          })
        );
      });
    }, INTERVAL_SECONDS * 1000);
  }
});

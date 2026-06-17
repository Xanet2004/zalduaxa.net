import json
import os
import time
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any


MATOMO_URL = os.getenv("MATOMO_URL", "http://matomo:80/").rstrip("/") + "/"
MATOMO_SITE_ID = os.getenv("MATOMO_SITE_ID", "1")
MATOMO_TOKEN_AUTH = os.getenv("MATOMO_TOKEN_AUTH")
MATOMO_EXPORTER_PORT = int(os.getenv("MATOMO_EXPORTER_PORT", "9101"))
CACHE_SECONDS = int(os.getenv("MATOMO_EXPORTER_CACHE_SECONDS", "60"))
TOP_PAGES_LIMIT = int(os.getenv("MATOMO_TOP_PAGES_LIMIT", "10"))

_cache: tuple[float, str] | None = None


def escape_label(value: Any) -> str:
    text = str(value)
    return (
        text.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace('"', '\\"')
    )


def api_call(method: str, extra: dict[str, str] | None = None) -> Any:
    if not MATOMO_TOKEN_AUTH:
        raise RuntimeError("MATOMO_TOKEN_AUTH is missing")

    query = {
        "module": "API",
        "method": method,
        "idSite": MATOMO_SITE_ID,
        "period": "day",
        "date": "today",
        "format": "JSON",
    }

    if extra:
        query.update(extra)

    # Keep token_auth out of the URL so it does not appear in access logs.
    url = MATOMO_URL + "index.php?" + urllib.parse.urlencode(query)
    body = urllib.parse.urlencode({"token_auth": MATOMO_TOKEN_AUTH}).encode("utf-8")

    request = urllib.request.Request(
        url,
        data=body,
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent": "zalduaxa-matomo-exporter/1.0",
        },
        method="POST",
    )

    with urllib.request.urlopen(request, timeout=10) as response:
        raw = response.read().decode("utf-8")
        return json.loads(raw)


def numeric(value: Any, default: float = 0.0) -> float:
    try:
        if value is None or value == "":
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def flatten_pages(items: Any) -> list[dict[str, Any]]:
    if not isinstance(items, list):
        return []

    pages: list[dict[str, Any]] = []

    for item in items:
        if not isinstance(item, dict):
            continue

        label = item.get("label", "unknown")
        url = item.get("url", label)

        pages.append(
            {
                "label": label,
                "url": url,
                "hits": numeric(item.get("nb_hits")),
                "visits": numeric(item.get("nb_visits")),
            }
        )

        for nested_key in ("subtable", "_subtable"):
            if nested_key in item:
                pages.extend(flatten_pages(item[nested_key]))

    pages.sort(key=lambda page: page["hits"], reverse=True)
    return pages[:TOP_PAGES_LIMIT]


def build_metrics() -> str:
    started_at = time.time()
    lines: list[str] = []

    lines.append("# HELP matomo_exporter_up Whether the Matomo exporter can query Matomo.")
    lines.append("# TYPE matomo_exporter_up gauge")

    try:
        summary = api_call("VisitsSummary.get")
        pages_raw = api_call(
            "Actions.getPageUrls",
            {
                "filter_limit": str(TOP_PAGES_LIMIT),
                "flat": "1",
            },
        )

        visits = numeric(summary.get("nb_visits"))
        unique_visitors = numeric(summary.get("nb_uniq_visitors"))
        actions = numeric(summary.get("nb_actions"))
        bounces = numeric(summary.get("bounce_count"))
        visit_length_total = numeric(summary.get("sum_visit_length"))

        avg_visit_duration = visit_length_total / visits if visits > 0 else 0

        lines.append("matomo_exporter_up 1")

        lines.append("# HELP matomo_visits Visits reported by Matomo.")
        lines.append("# TYPE matomo_visits gauge")
        lines.append(f'matomo_visits{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}"}} {visits}')

        lines.append("# HELP matomo_unique_visitors Unique visitors reported by Matomo.")
        lines.append("# TYPE matomo_unique_visitors gauge")
        lines.append(f'matomo_unique_visitors{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}"}} {unique_visitors}')

        lines.append("# HELP matomo_actions Actions/pageviews reported by Matomo.")
        lines.append("# TYPE matomo_actions gauge")
        lines.append(f'matomo_actions{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}"}} {actions}')

        lines.append("# HELP matomo_bounces Bounces reported by Matomo.")
        lines.append("# TYPE matomo_bounces gauge")
        lines.append(f'matomo_bounces{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}"}} {bounces}')

        lines.append("# HELP matomo_avg_visit_duration_seconds Average visit duration in seconds.")
        lines.append("# TYPE matomo_avg_visit_duration_seconds gauge")
        lines.append(
            f'matomo_avg_visit_duration_seconds{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}"}} {avg_visit_duration}'
        )

        pages = flatten_pages(pages_raw)

        lines.append("# HELP matomo_page_hits Page hits reported by Matomo.")
        lines.append("# TYPE matomo_page_hits gauge")
        lines.append("# HELP matomo_page_visits Page visits reported by Matomo.")
        lines.append("# TYPE matomo_page_visits gauge")

        for page in pages:
            label = escape_label(page["label"])
            url = escape_label(page["url"])

            lines.append(
                f'matomo_page_hits{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}",page="{label}",url="{url}"}} {page["hits"]}'
            )
            lines.append(
                f'matomo_page_visits{{period="today",site_id="{escape_label(MATOMO_SITE_ID)}",page="{label}",url="{url}"}} {page["visits"]}'
            )

    except Exception as exc:
        lines.append("matomo_exporter_up 0")
        lines.append("# HELP matomo_exporter_error Last Matomo exporter error.")
        lines.append("# TYPE matomo_exporter_error gauge")
        lines.append(f'matomo_exporter_error{{message="{escape_label(exc)}"}} 1')

    duration = time.time() - started_at
    lines.append("# HELP matomo_exporter_scrape_duration_seconds Exporter scrape duration.")
    lines.append("# TYPE matomo_exporter_scrape_duration_seconds gauge")
    lines.append(f"matomo_exporter_scrape_duration_seconds {duration}")

    return "\n".join(lines) + "\n"


class MetricsHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        global _cache

        if self.path not in ("/metrics", "/"):
            self.send_response(404)
            self.end_headers()
            return

        now = time.time()

        if _cache and now - _cache[0] < CACHE_SECONDS:
            payload = _cache[1]
        else:
            payload = build_metrics()
            _cache = (now, payload)

        encoded = payload.encode("utf-8")

        self.send_response(200)
        self.send_header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def log_message(self, format: str, *args: Any) -> None:
        return


if __name__ == "__main__":
    server = ThreadingHTTPServer(("0.0.0.0", MATOMO_EXPORTER_PORT), MetricsHandler)
    print(f"Matomo exporter listening on 0.0.0.0:{MATOMO_EXPORTER_PORT}")
    server.serve_forever()

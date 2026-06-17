declare global {
  interface Window {
    _paq?: unknown[][];
  }
}

const enabled = import.meta.env.VITE_MATOMO_ENABLED === "true";
const matomoUrl = import.meta.env.VITE_MATOMO_URL as string | undefined;
const siteId = import.meta.env.VITE_MATOMO_SITE_ID as string | undefined;

let initialized = false;

function getNormalizedMatomoUrl(): string | null {
  if (!matomoUrl) return null;
  return matomoUrl.endsWith("/") ? matomoUrl : `${matomoUrl}/`;
}

export function initMatomo(): void {
  if (!enabled || initialized) return;

  const normalizedUrl = getNormalizedMatomoUrl();

  if (!normalizedUrl || !siteId) {
    console.warn("Matomo is enabled but VITE_MATOMO_URL or VITE_MATOMO_SITE_ID is missing.");
    return;
  }

  window._paq = window._paq || [];

  window._paq.push(["setTrackerUrl", `${normalizedUrl}matomo.php`]);
  window._paq.push(["setSiteId", siteId]);
  window._paq.push(["enableLinkTracking"]);

  const script = document.createElement("script");
  script.async = true;
  script.defer = true;
  script.src = `${normalizedUrl}matomo.js`;

  const firstScript = document.getElementsByTagName("script")[0];
  firstScript.parentNode?.insertBefore(script, firstScript);

  initialized = true;
}

export function trackPageView(path: string, title?: string): void {
  if (!enabled) return;

  window._paq = window._paq || [];

  window._paq.push(["setCustomUrl", path]);

  if (title) {
    window._paq.push(["setDocumentTitle", title]);
  }

  window._paq.push(["trackPageView"]);
}

export function trackEvent(category: string, action: string, name?: string): void {
  if (!enabled) return;

  window._paq = window._paq || [];
  window._paq.push(["trackEvent", category, action, name ?? ""]);
}
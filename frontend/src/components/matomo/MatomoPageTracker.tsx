import { initMatomo, trackPageView } from "@/scripts/matomo";
import { useEffect } from "react";
import { useLocation } from "react-router-dom";

export default function MatomoPageTracker() {
  const location = useLocation();

  useEffect(() => {
    initMatomo();
  }, []);

  useEffect(() => {
    const path = `${location.pathname}${location.search}${location.hash}`;
    trackPageView(path, document.title);
  }, [location]);

  return null;
}
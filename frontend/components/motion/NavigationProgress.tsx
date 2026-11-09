"use client";

import { usePathname, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

function Bar() {
  const pathname = usePathname();
  const search = useSearchParams().toString();
  const here = search ? `${pathname}?${search}` : pathname;
  // The address the visitor is leaving: the bar runs until the address changes.
  const [leaving, setLeaving] = useState<string | null>(null);

  useEffect(() => {
    const click = (event: MouseEvent) => {
      if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      const anchor = (event.target as HTMLElement).closest?.("a");
      if (!anchor || anchor.target === "_blank" || !anchor.href) return;
      const url = new URL(anchor.href);
      if (url.origin !== location.origin || url.pathname.startsWith("/oauth2")) return;
      if (url.pathname + url.search !== location.pathname + location.search) setLeaving(location.pathname + location.search);
    };
    document.addEventListener("click", click);
    return () => document.removeEventListener("click", click);
  }, []);

  useEffect(() => {
    if (leaving === null || leaving === here) return;
    const rest = window.setTimeout(() => setLeaving(null), 260);
    return () => window.clearTimeout(rest);
  }, [leaving, here]);

  if (leaving === null) return null;
  return (
    <div aria-hidden="true" className="fixed inset-x-0 top-0 z-[60] h-0.5">
      <div className={`h-full bg-level-4 ${leaving === here ? "w-4/5 transition-[width] duration-[1200ms] ease-out" : "w-full transition-[width] duration-200"}`} />
    </div>
  );
}

/** The answer to a tap while the next page is on its way: on a slow network, silence reads as broken. */
export function NavigationProgress() {
  return (
    <Suspense fallback={null}>
      <Bar />
    </Suspense>
  );
}

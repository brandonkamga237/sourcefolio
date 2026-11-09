"use client";

import { useEffect, useState, type ReactNode } from "react";

/** Slides up a little once the page scrolls, giving the content back a few pixels. */
export function SiteHeader({ children }: { children: ReactNode }) {
  const [compact, setCompact] = useState(false);
  useEffect(() => {
    const read = () => setCompact(window.scrollY > 24);
    read();
    window.addEventListener("scroll", read, { passive: true });
    return () => window.removeEventListener("scroll", read);
  }, []);
  return (
    <header data-compact={compact || undefined} className="group sticky top-0 z-30 border-b border-line bg-surface-raised/95 backdrop-blur-md transition-transform duration-(--dur-3) ease-enter data-compact:-translate-y-1.5">
      <div className="transition-transform duration-(--dur-3) ease-enter group-data-compact:translate-y-[3px]">{children}</div>
    </header>
  );
}

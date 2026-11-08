"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { withoutLocale } from "@/i18n/paths";

export const NIGHT_PAGES = ["/", "/recruiters"];

/** The landing pages are always on the night ground, as their header must be whatever the theme. */
export function SiteHeader({ children }: { children: ReactNode }) {
  const night = NIGHT_PAGES.includes(withoutLocale(usePathname()));
  return <header className={`sticky top-0 z-30 border-b border-line-soft bg-canvas/80 backdrop-blur-md ${night ? "dark-band" : ""}`}>{children}</header>;
}

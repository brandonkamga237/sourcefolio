import type { ComponentType, SVGProps } from "react";
import { IconDiscover, IconDiscoverActive, IconMe, IconPortfolio, IconSearch } from "@/components/icons";

type Icon = ComponentType<SVGProps<SVGSVGElement>>;

export interface Section {
  key: "discover" | "search" | "portfolio" | "me";
  href: "/" | "/search" | "/me/portfolio" | "/me";
  icon: Icon;
  activeIcon: Icon;
}

export type SectionLabels = Record<Section["key"], string> & { label: string };

// The four tabs of the spec, in the same order in the sidebar and in the bottom bar.
export const sections: readonly Section[] = [
  { key: "discover", href: "/", icon: IconDiscover, activeIcon: IconDiscoverActive },
  { key: "search", href: "/search", icon: IconSearch, activeIcon: IconSearch },
  { key: "portfolio", href: "/me/portfolio", icon: IconPortfolio, activeIcon: IconPortfolio },
  { key: "me", href: "/me", icon: IconMe, activeIcon: IconMe },
];

// "/me" is a prefix of "/me/portfolio": each section owns its exact path and what is
// below it, except where a more specific section claims it.
export function isActive(section: Section, pathname: string): boolean {
  if (section.href === "/") return pathname === "/";
  if (section.href === "/me") return pathname === "/me" || (pathname.startsWith("/me/") && !pathname.startsWith("/me/portfolio"));
  return pathname === section.href || pathname.startsWith(`${section.href}/`);
}

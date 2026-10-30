import type { ComponentType, SVGProps } from "react";
import { IconDiscover, IconDiscoverActive, IconMe, IconRanking, IconRankingActive } from "@/components/icons";

type Icon = ComponentType<SVGProps<SVGSVGElement>>;

export interface Section {
  key: "discover" | "ranking" | "me";
  href: "/" | "/ranking" | "/me";
  icon: Icon;
  activeIcon: Icon;
}

export type SectionLabels = Record<Section["key"], string> & { label: string };

// The three tabs of the spec, in the same order in the sidebar and in the bottom bar.
export const sections: readonly Section[] = [
  { key: "discover", href: "/", icon: IconDiscover, activeIcon: IconDiscoverActive },
  { key: "ranking", href: "/ranking", icon: IconRanking, activeIcon: IconRankingActive },
  { key: "me", href: "/me", icon: IconMe, activeIcon: IconMe },
];

// Discover also owns its Recent order and the showcase pages a visitor opens from it; Me owns
// everything under /me (portfolio, profile questions).
export function isActive(section: Section, pathname: string): boolean {
  if (section.href === "/") return pathname === "/" || pathname === "/recent" || pathname.startsWith("/p/");
  return pathname === section.href || pathname.startsWith(`${section.href}/`);
}

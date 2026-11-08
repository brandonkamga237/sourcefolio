import type { ComponentType, SVGProps } from "react";
import { IconMap, IconMe, IconRanking, IconRankingActive } from "@/components/icons";

type Icon = ComponentType<SVGProps<SVGSVGElement>>;

export interface Section {
  key: "rankings" | "map" | "myProfile";
  href: "/rankings" | "/map" | "/me";
  icon: Icon;
  activeIcon: Icon;
}

export type SectionLabels = Record<Section["key"], string> & { label: string };

// The sections of docs/spec-v2.md, in the same order in the top bar and in the bottom tabs.
export const sections: readonly Section[] = [
  { key: "rankings", href: "/rankings", icon: IconRanking, activeIcon: IconRankingActive },
  { key: "map", href: "/map", icon: IconMap, activeIcon: IconMap },
  { key: "myProfile", href: "/me", icon: IconMe, activeIcon: IconMe },
];

// Rankings also own the profiles a visitor opens from them; My profile owns everything under /me.
export function isActive(section: Section, pathname: string): boolean {
  if (section.key === "rankings") return pathname === "/rankings" || pathname.startsWith("/u/");
  return pathname === section.href || pathname.startsWith(`${section.href}/`);
}

import type { ComponentType, SVGProps } from "react";
import { IconHome, IconMap, IconMe, IconRanking, IconRankingActive } from "@/components/icons";

type Icon = ComponentType<SVGProps<SVGSVGElement>>;

export interface Section {
  key: "home" | "rankings" | "map" | "recruiters" | "me";
  href: "/" | "/rankings" | "/map" | "/recruiters" | "/me";
  icon: Icon;
  activeIcon: Icon;
}

export type SectionLabels = Record<Section["key"], string> & { label: string };

const home: Section = { key: "home", href: "/", icon: IconHome, activeIcon: IconHome };
const rankings: Section = { key: "rankings", href: "/rankings", icon: IconRanking, activeIcon: IconRankingActive };
const map: Section = { key: "map", href: "/map", icon: IconMap, activeIcon: IconMap };
const recruiters: Section = { key: "recruiters", href: "/recruiters", icon: IconMe, activeIcon: IconMe };
const me: Section = { key: "me", href: "/me", icon: IconMe, activeIcon: IconMe };

export const headerSections: readonly Section[] = [rankings, map, recruiters];
export const tabSections: readonly Section[] = [home, rankings, map, me];

export function isActive(section: Section, pathname: string): boolean {
  if (section.key === "home") return pathname === "/";
  if (section.key === "rankings") return pathname === "/rankings" || pathname.startsWith("/u/");
  return pathname === section.href || pathname.startsWith(`${section.href}/`);
}

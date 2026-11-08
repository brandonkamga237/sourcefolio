import type { ComponentType, SVGProps } from "react";
import { IconDiscover, IconDiscoverActive, IconHome, IconMe } from "@/components/icons";

type Icon = ComponentType<SVGProps<SVGSVGElement>>;

export interface Section {
  key: "home" | "explore" | "recruiters" | "myProfile";
  href: "/" | "/rankings" | "/recruiters" | "/me";
  icon: Icon;
  activeIcon: Icon;
}

export type SectionLabels = Record<Section["key"], string> & { label: string };

const home: Section = { key: "home", href: "/", icon: IconHome, activeIcon: IconHome };
const explore: Section = { key: "explore", href: "/rankings", icon: IconDiscover, activeIcon: IconDiscoverActive };
const recruiters: Section = { key: "recruiters", href: "/recruiters", icon: IconMe, activeIcon: IconMe };
const myProfile: Section = { key: "myProfile", href: "/me", icon: IconMe, activeIcon: IconMe };

// A wide screen has the logo for home and a button for My profile. A phone's thumb zone holds
// what a tech profile comes for; recruiters arrive through the landing page and the footer.
export const headerSections: readonly Section[] = [explore, recruiters];
export const tabSections: readonly Section[] = [home, explore, myProfile];

// Explore owns the rankings, the map and the profiles a visitor opens from them.
export function isActive(section: Section, pathname: string): boolean {
  if (section.key === "home") return pathname === "/";
  if (section.key === "explore") return pathname === "/rankings" || pathname === "/map" || pathname.startsWith("/u/");
  return pathname === section.href || pathname.startsWith(`${section.href}/`);
}

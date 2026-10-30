"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { isActive, sections, type SectionLabels } from "./sections";

interface SidebarNavProps {
  locale: Locale;
  labels: SectionLabels;
}

export function SidebarNav({ locale, labels }: SidebarNavProps) {
  const pathname = withoutLocale(usePathname());

  return (
    <nav aria-label={labels.label} className="flex flex-col gap-1">
      {sections.map((section) => {
        const active = isActive(section, pathname);
        const Icon = active ? section.activeIcon : section.icon;
        return (
          <Link
            key={section.key}
            href={localizedPath(locale, section.href)}
            aria-current={active ? "page" : undefined}
            className={
              active
                ? "flex h-11 items-center gap-3 rounded-control bg-accent-soft px-3 text-[15px] font-semibold text-accent-ink"
                : "flex h-11 items-center gap-3 rounded-control px-3 text-[15px] font-medium text-ink-soft hover:bg-surface-sunken"
            }
          >
            <Icon className="size-5 shrink-0" />
            {labels[section.key]}
          </Link>
        );
      })}
    </nav>
  );
}

"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { headerSections, isActive, type SectionLabels } from "./sections";

interface HeaderNavProps {
  locale: Locale;
  labels: SectionLabels;
}

export function HeaderNav({ locale, labels }: HeaderNavProps) {
  const pathname = withoutLocale(usePathname());
  return (
    <nav aria-label={labels.label} className="hidden items-center gap-1 lg:flex">
      {headerSections.map((section) => {
        const active = isActive(section, pathname);
        return (
          <Link
            key={section.key}
            href={localizedPath(locale, section.href)}
            aria-current={active ? "page" : undefined}
            className={`flex h-9 items-center rounded-control px-3 text-[14px] transition-colors duration-150 ${
              active ? "font-medium text-ink" : "text-ink-muted hover:bg-surface-sunken hover:text-ink"
            }`}
          >
            {labels[section.key]}
          </Link>
        );
      })}
    </nav>
  );
}

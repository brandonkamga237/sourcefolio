"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { isActive, sections, type SectionLabels } from "./sections";

interface HeaderNavProps {
  locale: Locale;
  labels: SectionLabels;
}

/** The sections in the top bar on a wide screen, the current one underlined like GitHub's tabs. */
export function HeaderNav({ locale, labels }: HeaderNavProps) {
  const pathname = withoutLocale(usePathname());
  return (
    <nav aria-label={labels.label} className="hidden h-full items-stretch gap-1 lg:flex">
      {sections.map((section) => {
        const active = isActive(section, pathname);
        return (
          <Link
            key={section.key}
            href={localizedPath(locale, section.href)}
            aria-current={active ? "page" : undefined}
            className={`flex items-center border-b-2 px-3 text-[14px] transition-colors ${
              active ? "border-tab-active font-semibold text-ink" : "border-transparent text-ink-soft hover:text-ink"
            }`}
          >
            {labels[section.key]}
          </Link>
        );
      })}
    </nav>
  );
}

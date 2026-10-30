"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { isActive, sections, type SectionLabels } from "./sections";

interface BottomTabsProps {
  locale: Locale;
  labels: SectionLabels;
}

export function BottomTabs({ locale, labels }: BottomTabsProps) {
  const pathname = withoutLocale(usePathname());

  return (
    <nav
      aria-label={labels.label}
      className="fixed inset-x-0 bottom-0 z-40 border-t border-line bg-surface/95 pb-[env(safe-area-inset-bottom)] backdrop-blur lg:hidden"
    >
      <ul className="grid grid-cols-3">
        {sections.map((section) => {
          const active = isActive(section, pathname);
          const Icon = active ? section.activeIcon : section.icon;
          return (
            <li key={section.key}>
              <Link
                href={localizedPath(locale, section.href)}
                aria-current={active ? "page" : undefined}
                className={`press flex h-14 flex-col items-center justify-center gap-0.5 text-[11px] font-medium select-none ${
                  active ? "font-semibold text-accent-ink" : "text-ink-muted"
                }`}
              >
                <span
                  className={`flex h-7 w-12 items-center justify-center rounded-full transition-colors duration-200 ${active ? "bg-accent-soft" : ""}`}
                >
                  <Icon className="size-[22px]" />
                </span>
                {labels[section.key]}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

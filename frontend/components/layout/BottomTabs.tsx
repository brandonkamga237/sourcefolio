"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { isActive, tabSections, type SectionLabels } from "./sections";

export function BottomTabs({ locale, labels }: { locale: Locale; labels: SectionLabels }) {
  const pathname = withoutLocale(usePathname());
  return (
    <nav aria-label={labels.label} className="fixed inset-x-0 bottom-0 z-40 border-t border-line bg-surface/95 pb-[env(safe-area-inset-bottom)] backdrop-blur lg:hidden">
      <ul className="grid grid-cols-4">
        {tabSections.map((section) => {
          const active = isActive(section, pathname);
          const Icon = active ? section.activeIcon : section.icon;
          return (
            <li key={section.key}>
              <Link href={localizedPath(locale, section.href)} aria-current={active ? "page" : undefined} className={`press flex h-14 flex-col items-center justify-center gap-0.5 text-[11px] select-none ${active ? "font-semibold text-ink" : "text-ink-muted"}`}>
                <Icon aria-hidden="true" className="size-[22px]" />
                {labels[section.key]}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

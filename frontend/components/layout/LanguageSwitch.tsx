"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import { routing, type Locale } from "@/i18n/routing";

interface LanguageSwitchProps {
  current: Locale;
  label: string;
  names: Record<Locale, string>;
}

// Links rather than buttons: each language is a real address, crawlable and usable
// without JavaScript, and the current page is kept when switching.
export function LanguageSwitch({ current, label, names }: LanguageSwitchProps) {
  const pathname = withoutLocale(usePathname());

  return (
    <div role="group" aria-label={label} className="flex w-max overflow-hidden rounded-control border border-line">
      {routing.locales.map((locale) => {
        const active = locale === current;
        return (
          <Link
            key={locale}
            href={localizedPath(locale, pathname)}
            hrefLang={locale}
            aria-current={active ? "true" : undefined}
            className={`flex h-10 min-w-11 items-center justify-center px-3 text-[13px] font-semibold ${
              active ? "bg-surface text-ink" : "bg-surface-raised text-ink-muted hover:text-ink"
            }`}
          >
            {names[locale]}
          </Link>
        );
      })}
    </div>
  );
}

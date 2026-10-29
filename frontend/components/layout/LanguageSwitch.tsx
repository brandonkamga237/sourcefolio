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
    <div role="group" aria-label={label} className="grid grid-cols-2 gap-1 rounded-control bg-surface-sunken p-1">
      {routing.locales.map((locale) => {
        const active = locale === current;
        return (
          <Link
            key={locale}
            href={localizedPath(locale, pathname)}
            hrefLang={locale}
            aria-current={active ? "true" : undefined}
            className={`flex h-9 items-center justify-center rounded-segment font-mono text-[13px] ${
              active
                ? "bg-surface font-medium text-ink shadow-[0_1px_2px_rgba(18,20,18,0.12)]"
                : "text-ink-muted hover:text-ink"
            }`}
          >
            {names[locale]}
          </Link>
        );
      })}
    </div>
  );
}

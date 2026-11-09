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
    <div role="group" aria-label={label} className="flex">
      {routing.locales.map((locale) => (
        <Link
          key={locale}
          href={localizedPath(locale, pathname)}
          hrefLang={locale}
          aria-current={locale === current ? "true" : undefined}
          className={`flex size-11 items-center justify-center rounded-control text-[13px] transition-colors duration-(--dur-1) lg:w-9 ${locale === current ? "font-semibold text-ink" : "text-ink-muted hover:text-ink"}`}
        >
          {names[locale]}
        </Link>
      ))}
    </div>
  );
}

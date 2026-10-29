import { routing, type Locale } from "./routing";

// The locale is always the first segment, so localising a path is a prefix and nothing more.
// Used with next/link and next/navigation instead of next-intl's client navigation, which
// would need NextIntlClientProvider and ship the ICU message parser to every visitor.
export function localizedPath(locale: Locale, path: string): string {
  return path === "/" ? `/${locale}` : `/${locale}${path}`;
}

const localePrefix = new RegExp(`^/(${routing.locales.join("|")})(?=/|$)`);

export function withoutLocale(pathname: string): string {
  return pathname.replace(localePrefix, "") || "/";
}

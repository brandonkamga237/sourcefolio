import { hasLocale } from "next-intl";
import { defineRouting } from "next-intl/routing";

// The locale is always in the URL (/fr, /en): every page has its own address per language
// for hreflang, the sitemap and the language of shared previews.
export const routing = defineRouting({
  locales: ["fr", "en"],
  defaultLocale: "en",
  localePrefix: "always",
});

export type Locale = (typeof routing.locales)[number];

export function toLocale(value: string): Locale {
  return hasLocale(routing.locales, value) ? value : routing.defaultLocale;
}

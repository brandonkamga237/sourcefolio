// Client components get raw "one"/"other" templates (no ICU parser in the browser): this picks
// the locale's form and fills {count}.
export interface PluralTemplates {
  one: string;
  other: string;
}

export function plural(templates: PluralTemplates, count: number, locale: string): string {
  const form = new Intl.PluralRules(locale).select(count) === "one" ? "one" : "other";
  return templates[form].replace("{count}", String(count));
}

export function compactNumber(count: number, locale: string): string {
  return new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(count);
}

// "3 days ago", in the reader's language and in full words ("2 m." could be months or
// minutes), from the largest unit that fits.
export function sinceLabel(iso: string, locale: string): string {
  const seconds = (new Date(iso).getTime() - Date.now()) / 1000;
  const units: [Intl.RelativeTimeFormatUnit, number][] = [["year", 31536000], ["month", 2592000], ["week", 604800], ["day", 86400], ["hour", 3600], ["minute", 60]];
  const format = new Intl.RelativeTimeFormat(locale, { numeric: "auto", style: "long" });
  for (const [unit, size] of units) {
    if (Math.abs(seconds) >= size) return format.format(Math.round(seconds / size), unit);
  }
  // "now" rather than "this minute", which reads oddly ("cette minute-ci").
  return format.format(0, "second");
}

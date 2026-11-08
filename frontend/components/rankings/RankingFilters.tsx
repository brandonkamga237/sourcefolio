"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";

interface RankingFiltersProps {
  country: string | null;
  language: string | null;
  countries: { value: string; label: string }[];
  languages: { value: string; label: string }[];
  labels: { country: string; allCountries: string; language: string; allLanguages: string };
}

const label = "flex flex-col gap-1.5 text-[13px] text-ink-muted";

/** Country and language: each change reloads the ranking from its first page. */
export function RankingFilters({ country, language, countries, languages, labels }: RankingFiltersProps) {
  const router = useRouter();
  const pathname = usePathname();
  const params = useSearchParams();

  function set(key: "country" | "language", value: string) {
    const next = new URLSearchParams(params.toString());
    if (value) next.set(key, value);
    else next.delete(key);
    next.delete("cursor");
    const query = next.toString();
    router.push(query ? `${pathname}?${query}` : pathname);
  }

  return (
    <div className="grid grid-cols-2 gap-3 lg:w-[400px]">
      <label className={label}>
        {labels.country}
        <select value={country ?? ""} onChange={(event) => set("country", event.target.value)} className="field">
          <option value="">{labels.allCountries}</option>
          {countries.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </label>
      <label className={label}>
        {labels.language}
        <select value={language ?? ""} onChange={(event) => set("language", event.target.value)} className="field">
          <option value="">{labels.allLanguages}</option>
          {languages.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </label>
    </div>
  );
}

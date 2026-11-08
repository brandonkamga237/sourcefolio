"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

export interface Talent {
  login: string;
  city: string | null;
  languages: string[];
  activeMonths: number;
}

interface TalentFieldProps {
  locale: Locale;
  talents: Talent[];
  labels: {
    language: string;
    allLanguages: string;
    city: string;
    allCities: string;
    rhythm: string;
    allRhythms: string;
    steady: string;
    regular: string;
    occasional: string;
    shown: string;
    noMatch: string;
    months: string;
  };
}

type Rhythm = "" | "steady" | "regular" | "occasional";

const LEVEL = ["bg-level-0", "bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;
const label = "flex flex-col gap-1.5 text-[13px] text-ink-muted";

function levelOf(activeMonths: number): number {
  if (activeMonths >= 10) return 4;
  if (activeMonths >= 6) return 3;
  if (activeMonths >= 3) return 2;
  return activeMonths > 0 ? 1 : 0;
}

function matchesRhythm(rhythm: Rhythm, activeMonths: number): boolean {
  if (rhythm === "steady") return activeMonths >= 10;
  if (rhythm === "regular") return activeMonths >= 6 && activeMonths < 10;
  if (rhythm === "occasional") return activeMonths < 6;
  return true;
}

/** One square a profile, as dark as its rhythm. Filters work on the profiles already on the page. */
export function TalentField({ locale, talents, labels }: TalentFieldProps) {
  const [language, setLanguage] = useState("");
  const [city, setCity] = useState("");
  const [rhythm, setRhythm] = useState<Rhythm>("");

  const languages = useMemo(() => [...new Set(talents.flatMap((talent) => talent.languages))].sort((a, b) => a.localeCompare(b)), [talents]);
  const cities = useMemo(() => [...new Set(talents.flatMap((talent) => (talent.city ? [talent.city] : [])))].sort((a, b) => a.localeCompare(b, locale)), [talents, locale]);
  const shown = talents.filter(
    (talent) => (!language || talent.languages.includes(language)) && (!city || talent.city === city) && matchesRhythm(rhythm, talent.activeMonths),
  );

  return (
    <div className="flex flex-col gap-4">
      <div className="grid gap-2 sm:grid-cols-3">
        <label className={label}>
          {labels.language}
          <select value={language} onChange={(event) => setLanguage(event.target.value)} className="field">
            <option value="">{labels.allLanguages}</option>
            {languages.map((name) => <option key={name}>{name}</option>)}
          </select>
        </label>
        <label className={label}>
          {labels.city}
          <select value={city} onChange={(event) => setCity(event.target.value)} className="field">
            <option value="">{labels.allCities}</option>
            {cities.map((name) => <option key={name}>{name}</option>)}
          </select>
        </label>
        <label className={label}>
          {labels.rhythm}
          <select value={rhythm} onChange={(event) => setRhythm(event.target.value as Rhythm)} className="field">
            <option value="">{labels.allRhythms}</option>
            <option value="steady">{labels.steady}</option>
            <option value="regular">{labels.regular}</option>
            <option value="occasional">{labels.occasional}</option>
          </select>
        </label>
      </div>
      <p aria-live="polite" className="text-[13px] text-ink-muted">
        {labels.shown.replace("{count}", String(shown.length)).replace("{total}", String(talents.length))}
      </p>
      {shown.length === 0 ? (
        <p className="text-[14px] text-ink-soft">{labels.noMatch}</p>
      ) : (
        <ul className="flex flex-wrap gap-1.5">
          {shown.map((talent) => {
            const name = `${talent.login} · ${labels.months.replace("{active}", String(talent.activeMonths))}`;
            return (
              <li key={talent.login}>
                <Link href={localizedPath(locale, `/u/${talent.login}`)} aria-label={name} title={name} className={`block size-11 rounded-[6px] outline-offset-1 transition-transform duration-150 hover:-translate-y-0.5 ${LEVEL[levelOf(talent.activeMonths)]}`} />
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

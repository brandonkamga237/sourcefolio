import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { GlobeLazy } from "@/components/globe/GlobeLazy";
import { AfricaMosaic } from "@/components/map/AfricaMosaic";
import { RankingFilters } from "@/components/rankings/RankingFilters";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { globeCities } from "@/lib/globe";
import { levelOf, placeCounts } from "@/lib/map";
import { getCityCounts } from "@/services/map";
import { getRankingFacets, getRankings } from "@/services/rankings";

export async function generateMetadata({ params }: PageProps<"/[locale]/map">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "map" });
  return {
    title: t("title"),
    description: t("lead"),
    alternates: { canonical: `/${locale}/map`, languages: { fr: "/fr/map", en: "/en/map", "x-default": "/en/map" } },
  };
}

const SWATCH = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;

async function CityMap({ locale, searchParams }: { locale: Locale; searchParams: PageProps<"/[locale]/map">["searchParams"] }) {
  // Read at request time: at build the API is not there, and a cached failure would ship an empty map.
  await connection();
  const [t, home, rankings, raw] = await Promise.all([getTranslations({ locale, namespace: "map" }), getTranslations({ locale, namespace: "home" }), getTranslations({ locale, namespace: "rankings" }), searchParams]);
  const single = (value: string | string[] | undefined) => (Array.isArray(value) ? value[0] : value)?.trim() || null;
  const country = single(raw.country)?.toUpperCase() ?? null;
  const language = single(raw.language);
  let counts;
  let facets;
  let people;
  try {
    [counts, facets, people] = await Promise.all([getCityCounts(country, language), getRankingFacets(country, language), getRankings({ sort: "regularity", country, language, cursor: null })]);
  } catch (error) {
    console.error("The map could not be loaded", error);
    return <p role="alert" className="rounded-card border border-line bg-surface p-4 text-[14px]">{t("unavailable")}</p>;
  }
  const { cities, unplaced, total } = placeCounts(counts);

  return (
    <div className="flex flex-col gap-6">
      <RankingFilters
        country={country}
        language={language}
        countries={facets.countries.map((count) => ({ value: count.value, label: `${countryName(count.value, locale)} (${count.count})` }))}
        languages={facets.languages.map((count) => ({ value: count.value, label: `${count.value} (${count.count})` }))}
        labels={{ country: rankings("country"), allCountries: rankings("allCountries"), language: rankings("language"), allLanguages: rankings("allLanguages") }}
      />
    <div className="grid items-center gap-8 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)] lg:gap-14">
      <div className="flex flex-col gap-3">
        <GlobeLazy
          locale={locale}
          cities={globeCities(cities, people.items, locale, (count) => home("globe.profiles", { count }))}
          example={false}
          label={t("picture", { count: total })}
          open={home("globe.open")}
          fallback={<AfricaMosaic locale={locale} cities={cities} label={t("picture", { count: total })} />}
        />
      </div>

      <div className="flex flex-col gap-3">
        <p className="t-h3">{t("count", { count: total })}</p>
        {cities.length === 0 ? (
          <div className="flex flex-col items-start gap-3 rounded-card border border-line bg-surface p-5">
            <p className="text-[14px] text-ink-soft">{t("emptyLead")}</p>
            <Link href={localizedPath(locale, "/me")} className="press flex h-11 items-center rounded-control bg-cta px-4 text-[14px] font-semibold text-cta-ink">
              {t("publishMine")}
            </Link>
          </div>
        ) : (
          <ul className="divide-y divide-line-soft rounded-card border border-line bg-surface">
            {cities.map(({ city, count }) => (
              <li key={`${city.country}:${city.name}`}>
                <Link href={localizedPath(locale, `/rankings?country=${city.country}`)} className="flex min-h-12 items-center gap-3 px-4 py-2 text-[16px] hover:bg-surface-raised">
                  <span aria-hidden="true" className={`size-3 shrink-0 rounded-[2px] ${SWATCH[levelOf(count) - 1]}`} />
                  <span className="min-w-0 flex-1 truncate">
                    <span className="font-semibold">{locale === "en" ? (city.nameEn ?? city.name) : city.name}</span>
                    <span className="text-ink-muted"> · {countryName(city.country, locale)}</span>
                  </span>
                  <span className="tabular-nums text-ink-soft">{count}</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
        {unplaced > 0 ? <p className="text-[14px] text-ink-muted">{t("unplaced", { count: unplaced })}</p> : null}
        <p className="text-[14px] text-ink-muted">{t("tapHint")}</p>
      </div>
    </div>
    </div>
  );
}

export default async function MapPage({ params, searchParams }: PageProps<"/[locale]/map">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "map" });
  return (
    <div className="wrap flex flex-col gap-8 py-8 lg:py-12">
      <header className="flex flex-col gap-2">
        <h1 className="t-h2">{t("title")}</h1>
        <p className="max-w-3xl text-[18px] leading-relaxed text-ink-soft lg:text-[20px]">{t("lead")}</p>
      </header>
      <Suspense fallback={<div className="h-96 animate-pulse rounded-card bg-surface-sunken" />}>
        <CityMap locale={locale} searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

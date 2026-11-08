import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { AfricaFieldLazy as AfricaField } from "@/components/map/AfricaFieldLazy";
import { ExploreHeader } from "@/components/rankings/ExploreHeader";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { fieldCities, levelOf, placeCounts } from "@/lib/map";
import { getCityCounts } from "@/services/map";

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

async function CityMap({ locale }: { locale: Locale }) {
  // Read at request time: at build the API is not there, and a cached failure would ship an empty map.
  await connection();
  const t = await getTranslations({ locale, namespace: "map" });
  let counts;
  try {
    counts = await getCityCounts();
  } catch (error) {
    console.error("The map could not be loaded", error);
    return <p role="alert" className="rounded-card border border-line bg-surface p-4 text-[14px]">{t("unavailable")}</p>;
  }
  const { cities, unplaced, total } = placeCounts(counts);

  return (
    <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,1fr)_400px] lg:gap-12">
      <div className="dark-band hero-lights flex flex-col gap-4 rounded-[24px] p-5 lg:p-10">
        <AfricaField locale={locale} cities={fieldCities(cities)} label={t("picture", { count: total })} />
        <div className="flex items-center gap-2 text-[15px] text-ink-muted">
          <span>{t("legendFew")}</span>
          {SWATCH.map((swatch) => <span key={swatch} aria-hidden="true" className={`size-4 rounded-[3px] ${swatch}`} />)}
          <span>{t("legendMany")}</span>
        </div>
      </div>

      <div className="flex flex-col gap-3">
        <p className="text-[32px] leading-tight font-semibold tracking-[-0.03em]">{t("count", { count: total })}</p>
        {cities.length === 0 ? (
          <div className="flex flex-col items-start gap-3 rounded-card bg-surface-sunken p-6">
            <p className="text-[17px] leading-relaxed text-ink-soft">{t("emptyLead")}</p>
            <Link href={localizedPath(locale, "/me")} className="btn btn-primary">
              {t("publishMine")}
            </Link>
          </div>
        ) : (
          <ul className="divide-y divide-line-soft border-y border-line-soft">
            {cities.map(({ city, count }) => (
              <li key={`${city.country}:${city.name}`}>
                <Link href={localizedPath(locale, `/rankings?country=${city.country}`)} className="flex min-h-14 items-center gap-3 py-2 text-[17px] transition-colors duration-150 hover:bg-surface-raised">
                  <span aria-hidden="true" className={`size-4 shrink-0 rounded-[3px] ${SWATCH[levelOf(count) - 1]}`} />
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
        {unplaced > 0 ? <p className="text-[15px] text-ink-muted">{t("unplaced", { count: unplaced })}</p> : null}
        <p className="text-[14px] leading-relaxed text-ink-muted">{t("tapHint")}</p>
      </div>
    </div>
  );
}

export default async function MapPage({ params }: PageProps<"/[locale]/map">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "map" });
  return (
    <div className="wrap flex flex-col gap-10 py-10 lg:gap-14 lg:py-20">
      <ExploreHeader locale={locale} current="map" lead={t("lead")} />
      <Suspense fallback={<div className="h-96 animate-pulse rounded-card bg-surface-sunken" />}>
        <CityMap locale={locale} />
      </Suspense>
    </div>
  );
}

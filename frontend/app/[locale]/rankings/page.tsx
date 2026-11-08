import type { Metadata } from "next";
import Link from "next/link";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { ExploreHeader } from "@/components/rankings/ExploreHeader";
import { RankingFilters } from "@/components/rankings/RankingFilters";
import { RankingTable } from "@/components/rankings/RankingTable";
import { JsonLd } from "@/components/seo/JsonLd";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { getRankingFacets, getRankings, RANKING_SORTS, type RankingSort } from "@/services/rankings";

export async function generateMetadata({ params }: PageProps<"/[locale]/rankings">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "rankings" });
  return {
    title: t("title"),
    description: t("lead"),
    alternates: { canonical: `/${locale}/rankings`, languages: { fr: "/fr/rankings", en: "/en/rankings", "x-default": "/en/rankings" } },
  };
}

function single(value: string | string[] | undefined): string | null {
  const first = Array.isArray(value) ? value[0] : value;
  return first && first.trim() ? first.trim() : null;
}

function href(locale: Locale, values: Record<string, string | null>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(values)) if (value) search.set(key, value);
  const query = search.toString();
  return localizedPath(locale, query ? `/rankings?${query}` : "/rankings");
}

async function Ranking({ locale, searchParams }: { locale: Locale; searchParams: PageProps<"/[locale]/rankings">["searchParams"] }) {
  const t = await getTranslations({ locale, namespace: "rankings" });
  const raw = await searchParams;
  const sortParam = single(raw.sort);
  const sort: RankingSort = RANKING_SORTS.includes(sortParam as RankingSort) ? (sortParam as RankingSort) : "regularity";
  const country = single(raw.country)?.toUpperCase() ?? null;
  const language = single(raw.language);
  const cursor = single(raw.cursor);

  let page;
  let facets;
  try {
    [page, facets] = await Promise.all([getRankings({ sort, country, language, cursor }), getRankingFacets(country, language)]);
  } catch (error) {
    console.error("The ranking could not be loaded", error);
    return <p role="alert" className="rounded-card border border-line bg-surface p-4 text-[14px]">{t("unavailable")}</p>;
  }

  const site = process.env.SITE_URL ?? "http://localhost:3000";
  return (
    <div className="flex flex-col gap-5">
      <JsonLd data={{ "@context": "https://schema.org", "@type": "CollectionPage", name: t("title"), description: t("lead"), url: `${site}/${locale}/rankings`, inLanguage: locale }} />
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        {/* Links, not buttons: each ranking has its own address, shareable and indexable. */}
        <nav aria-label={t("sortBy")} className="-mx-4 overflow-x-auto px-4 lg:mx-0 lg:px-0">
          <ul className="flex w-max gap-5 border-b border-line">
            {RANKING_SORTS.map((option) => (
              <li key={option}>
                <Link
                  href={href(locale, { sort: option === "regularity" ? null : option, country, language })}
                  aria-current={option === sort ? "page" : undefined}
                  className={`-mb-px flex min-h-11 items-center border-b-2 text-[14px] whitespace-nowrap transition-colors duration-150 ${
                    option === sort ? "border-tab-active font-medium text-ink" : "border-transparent text-ink-muted hover:text-ink"
                  }`}
                >
                  {t(`sort.${option}`)}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <RankingFilters
          country={country}
          language={language}
          countries={facets.countries.map((count) => ({ value: count.value, label: `${countryName(count.value, locale)} (${count.count})` }))}
          languages={facets.languages.map((count) => ({ value: count.value, label: `${count.value} (${count.count})` }))}
          labels={{ country: t("country"), allCountries: t("allCountries"), language: t("language"), allLanguages: t("allLanguages") }}
        />
      </div>

      <p className="text-[13px] text-ink-muted"><span className="font-medium text-ink">{t("count", { count: page.total })}</span> · {t(`measures.${sort}`)}</p>

      {page.items.length === 0 ? (
        <div className="flex flex-col items-start gap-3 rounded-card bg-surface-sunken p-6">
          <p className="text-[16px] font-medium">{t("empty")}</p>
          <p className="text-[14px] text-ink-soft">{t("emptyLead")}</p>
          <Link href={localizedPath(locale, "/me")} className="btn btn-primary">
            {t("publishMine")}
          </Link>
        </div>
      ) : (
        <RankingTable rows={page.items} sort={sort} />
      )}

      <div className="flex flex-wrap items-center gap-3">
        {page.nextCursor ? (
          <Link href={href(locale, { sort: sort === "regularity" ? null : sort, country, language, cursor: page.nextCursor })} className="btn btn-secondary">
            {t("more")}
          </Link>
        ) : null}
        {cursor ? (
          <Link href={href(locale, { sort: sort === "regularity" ? null : sort, country, language })} className="link text-[14px]">
            {t("backToTop")}
          </Link>
        ) : null}
      </div>
      <p className="text-[12px] text-ink-muted">{t("honest")}</p>
    </div>
  );
}

export default async function RankingsPage({ params, searchParams }: PageProps<"/[locale]/rankings">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "rankings" });
  return (
    <div className="wrap flex flex-col gap-8 py-8 lg:gap-10 lg:py-14">
      <ExploreHeader locale={locale} current="rankings" lead={t("lead")} />
      <Suspense fallback={<div className="h-96 animate-pulse rounded-card bg-surface-sunken" />}>
        <Ranking locale={locale} searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

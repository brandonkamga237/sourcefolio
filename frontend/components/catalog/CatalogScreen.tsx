import Link from "next/link";
import { connection } from "next/server";
import { getTranslations } from "next-intl/server";
import { Suspense } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { getCatalogFacets, getCatalogFirstPage, type CatalogPage, type CatalogSort } from "@/services/catalog";
import { filterParams, hasFilters, readFilters } from "@/services/catalog-types";
import { CatalogSearch } from "./CatalogSearch";
import { SortTabs, StaticSortTabs } from "./SortTabs";
import { CatalogFeed } from "./CatalogFeed";
import { DiscoverIntro } from "./DiscoverIntro";
import { memberSheetLabels } from "@/components/members/labels";
import { ROLES } from "@/services/profile-types";

interface CatalogScreenProps {
  locale: Locale;
  sort: CatalogSort;
  /** The page's query, read inside the feed's boundary: request data never blocks the shell. */
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}

const sorts: { sort: CatalogSort; path: string }[] = [
  { sort: "discover", path: "/" },
  { sort: "recent", path: "/recent" },
];

// The two orders of the spec, as links: each has its own address, so a shared or reloaded page
// keeps its order (and the visitor's filters). No popularity order, ever.
async function SortNav({ locale, sort }: { locale: Locale; sort: CatalogSort }) {
  const t = await getTranslations({ locale, namespace: "catalog.sort" });
  const tabs = sorts.map((item) => ({ key: item.sort, href: localizedPath(locale, item.path), label: t(item.sort), current: item.sort === sort }));
  return (
    <Suspense fallback={<StaticSortTabs label={t("label")} tabs={tabs} />}>
      <SortTabs label={t("label")} tabs={tabs} />
    </Suspense>
  );
}

// The search bar offers only the countries, source languages and roles some portfolio carries. Read per
// request (then cached a few minutes): the build has no API, and the facets grow with the catalogue.
async function Search({ locale }: { locale: Locale }) {
  await connection();
  const [t, facets] = await Promise.all([getTranslations({ locale }), getCatalogFacets()]);
  return (
    <CatalogSearch
      locale={locale}
      facets={facets}
      labels={{
        search: t("catalog.search.placeholder"),
        country: t("catalog.search.country"),
        anyCountry: t("catalog.search.anyCountry"),
        language: t("catalog.search.language"),
        anyLanguage: t("catalog.search.anyLanguage"),
        role: t("catalog.search.role"),
        anyRole: t("catalog.search.anyRole"),
        roleNames: Object.fromEntries(ROLES.map((slug) => [slug, t(`role.${slug}`)])),
        clear: t("catalog.search.clear"),
        filters: t("catalog.search.filters"),
      }}
    />
  );
}

async function Feed({ locale, sort, searchParams }: CatalogScreenProps) {
  // Read per request: the catalogue changes as members publish, and the build has no API.
  await connection();
  const filters = readFilters(await searchParams);
  const [t, tl, ts] = await Promise.all([
    getTranslations({ locale, namespace: "catalog" }),
    getTranslations({ locale, namespace: "likes" }),
    getTranslations({ locale, namespace: "stars" }),
  ]);
  const memberLabels = await memberSheetLabels(locale);
  let firstPage: CatalogPage;
  try {
    firstPage = await getCatalogFirstPage(sort, filters);
  } catch (error) {
    console.error("The catalogue could not be loaded", error);
    return (
      <p role="alert" className="rounded-card border border-line bg-surface p-4 text-sm text-ink-body">
        {t("unavailable")}
      </p>
    );
  }

  if (firstPage.items.length === 0 && hasFilters(filters)) {
    return (
      <section aria-labelledby="catalog-no-match" className="flex flex-col items-start gap-2 rounded-card border border-line bg-surface p-5">
        <h2 id="catalog-no-match" className="text-[15px] font-semibold">{t("search.noMatchTitle")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("search.noMatchLead")}</p>
        <Link href={localizedPath(locale, sort === "recent" ? "/recent" : "/")} className="press flex h-11 items-center rounded-control border border-ink px-4 text-sm font-semibold text-ink">
          {t("search.clear")}
        </Link>
      </section>
    );
  }

  if (firstPage.items.length === 0) {
    return (
      <section aria-labelledby="catalog-empty" className="flex flex-col gap-2 rounded-card border border-line bg-surface p-4">
        <h2 id="catalog-empty" className="text-[15px] font-semibold">{t("emptyTitle")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("emptyLead")}</p>
        <Link href={localizedPath(locale, "/me")} className="press flex h-11 w-fit items-center rounded-control bg-accent px-4 text-sm font-semibold text-white">
          {t("emptyLink")}
        </Link>
      </section>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      <p className="-mt-1 font-mono text-xs text-ink-muted sm:text-[13px]">
        {t("count", { count: firstPage.total })} · {t(sort === "discover" ? "orderShuffled" : "orderRecent")}
      </p>
      <CatalogFeed
      key={filterParams(filters).join("&")}
      filters={filters}
      locale={locale}
      sort={sort}
      firstPage={firstPage}
      labels={{
        loadMore: t("loadMore"),
        loading: t("loading"),
        loadFailed: t("loadFailed"),
        card: {
          member: memberLabels,
          share: t("panel.share"),
          shareTitle: t.raw("panel.shareTitle") as string,
          copied: t("panel.copied"),
          likes: { one: tl.raw("countOne") as string, other: tl.raw("countOther") as string },
          stars: { one: ts.raw("one") as string, other: ts.raw("other") as string },
        },
        panel: {
          close: t("panel.close"),
          previous: t("panel.previous"),
          next: t("panel.next"),
          position: t.raw("panel.position") as string,
          visitSite: t("panel.visitSite"),
          viewCode: t("panel.viewCode"),
          openPage: t("panel.openPage"),
          sourceRepo: t("panel.sourceRepo"),
          publishedOn: t.raw("panel.publishedOn") as string,
          stats: { likes: t("panel.statLikes"), stars: t("panel.statStars"), published: t("panel.statPublished") },
          share: t("panel.share"),
          shareTitle: t.raw("panel.shareTitle") as string,
          clone: t("panel.clone"),
          copied: t("panel.copied"),
          githubProfile: t("panel.githubProfile"),
          member: memberLabels,
          project: { title: t("panel.projectTitle"), languages: t("panel.projectLanguages"), updated: t.raw("panel.projectUpdated") as string },
          likes: { like: tl.raw("like") as string, unlike: tl.raw("unlike") as string, own: tl.raw("own") as string, failed: tl("failed") },
        },
      }}
      />
    </div>
  );
}

function FeedSkeleton() {
  return (
    <div aria-hidden="true" className="grid grid-cols-2 gap-3 sm:gap-5 md:[grid-template-columns:repeat(auto-fill,minmax(220px,1fr))] lg:gap-6">
      {[0, 1, 2, 3, 4, 5].map((index) => (
        <div key={index} className="aspect-[4/3] animate-pulse rounded-card bg-surface-sunken" />
      ))}
    </div>
  );
}

export async function CatalogScreen({ locale, sort, searchParams }: CatalogScreenProps) {
  const t = await getTranslations({ locale });
  return (
    <div className="mx-auto flex w-full max-w-[1500px] flex-col gap-4 px-4 py-4 sm:gap-5 lg:px-8 lg:py-7">
      {sort === "discover" ? <DiscoverIntro locale={locale} /> : null}
      {/* Stays at the top while the cards scroll: the order, the search and the filters are
          always one tap away, under the phone's top bar or at the top of the desktop window. */}
      <div className="sticky top-14 z-20 -mx-4 flex flex-wrap items-center justify-between gap-x-3 gap-y-2 bg-canvas/85 px-4 py-2 backdrop-blur-md lg:top-0 lg:-mx-8 lg:px-8 lg:py-3 xl:flex-nowrap">
        {sort === "discover" ? (
          <h2 className="font-display text-[20px] leading-none font-bold tracking-[-0.03em] sm:text-[24px]">{t("catalog.feedTitle")}</h2>
        ) : (
          <h1 className="font-display text-[22px] leading-none font-bold tracking-[-0.035em] sm:text-[26px] lg:text-[30px]">{t("catalog.recentTitle")}</h1>
        )}
        <SortNav locale={locale} sort={sort} />
        <Suspense fallback={<div aria-hidden="true" className="h-11 w-full animate-pulse rounded-control bg-surface-sunken xl:order-1 xl:w-auto xl:flex-1" />}>
          <Search locale={locale} />
        </Suspense>
      </div>
      <Suspense fallback={<FeedSkeleton />}>
        <Feed locale={locale} sort={sort} searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

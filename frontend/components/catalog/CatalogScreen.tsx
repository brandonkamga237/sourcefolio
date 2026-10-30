import Link from "next/link";
import { connection } from "next/server";
import { getTranslations } from "next-intl/server";
import { Suspense } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { getCatalogFirstPage, type CatalogPage, type CatalogSort } from "@/services/catalog";
import { CatalogFeed } from "./CatalogFeed";

interface CatalogScreenProps {
  locale: Locale;
  sort: CatalogSort;
}

const sorts: { sort: CatalogSort; path: string }[] = [
  { sort: "discover", path: "/" },
  { sort: "recent", path: "/recent" },
];

// The two orders of the spec, as links: each has its own address, so a shared or reloaded page
// keeps its order. No popularity order, ever.
async function SortTabs({ locale, sort }: CatalogScreenProps) {
  const t = await getTranslations({ locale, namespace: "catalog.sort" });
  return (
    <nav aria-label={t("label")} className="flex gap-2">
      {sorts.map((item) => (
        <Link
          key={item.sort}
          href={localizedPath(locale, item.path)}
          aria-current={item.sort === sort ? "page" : undefined}
          className={`flex h-11 items-center rounded-full px-4 text-sm font-semibold ${
            item.sort === sort ? "bg-ink text-canvas" : "border border-line bg-surface text-ink"
          }`}
        >
          {t(item.sort)}
        </Link>
      ))}
    </nav>
  );
}

async function Feed({ locale, sort }: CatalogScreenProps) {
  // Read per request: the catalogue changes as members publish, and the build has no API.
  await connection();
  const t = await getTranslations({ locale, namespace: "catalog" });
  let firstPage: CatalogPage;
  try {
    firstPage = await getCatalogFirstPage(sort);
  } catch (error) {
    console.error("The catalogue could not be loaded", error);
    return (
      <p role="alert" className="rounded-card border border-line bg-surface p-4 text-sm text-ink-body">
        {t("unavailable")}
      </p>
    );
  }

  if (firstPage.items.length === 0) {
    return (
      <section aria-labelledby="catalog-empty" className="flex flex-col gap-2 rounded-card border border-line bg-surface p-4">
        <h2 id="catalog-empty" className="text-[15px] font-semibold">{t("emptyTitle")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("emptyLead")}</p>
        <Link href={localizedPath(locale, "/me/portfolio")} className="flex h-11 w-fit items-center rounded-control bg-accent px-4 text-sm font-semibold text-white">
          {t("emptyLink")}
        </Link>
      </section>
    );
  }

  return (
    <CatalogFeed
      locale={locale}
      sort={sort}
      firstPage={firstPage}
      labels={{ by: t.raw("by") as string, loadMore: t("loadMore"), loading: t("loading"), loadFailed: t("loadFailed") }}
    />
  );
}

function FeedSkeleton() {
  return (
    <div aria-hidden="true" className="grid gap-3 sm:grid-cols-2">
      {[0, 1, 2, 3].map((index) => (
        <div key={index} className="h-[104px] animate-pulse rounded-card bg-surface-sunken" />
      ))}
    </div>
  );
}

export async function CatalogScreen({ locale, sort }: CatalogScreenProps) {
  const t = await getTranslations({ locale });
  return (
    <div className="flex flex-col gap-5 px-4 py-6 lg:px-10 lg:py-8">
      <div className="flex flex-col gap-2">
        <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">{t("brand.tagline")}</h1>
        <p className="text-[15px] leading-normal text-ink-body">{t("catalog.lead")}</p>
      </div>
      <SortTabs locale={locale} sort={sort} />
      <Suspense fallback={<FeedSkeleton />}>
        <Feed locale={locale} sort={sort} />
      </Suspense>
    </div>
  );
}

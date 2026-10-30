import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { Preview } from "@/components/catalog/Preview";
import { IconHeart } from "@/components/icons";
import { Podium } from "@/components/illustrations/Podium";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import { getRanking, type RankedCard } from "@/services/ranking";

export async function generateMetadata({ params }: PageProps<"/[locale]/ranking">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "ranking" });
  return {
    title: t("title"),
    description: t("lead"),
    alternates: { canonical: `/${locale}/ranking`, languages: { fr: "/fr/ranking", en: "/en/ranking", "x-default": "/en/ranking" } },
  };
}

async function RankingList({ locale }: { locale: Locale }) {
  // Read per request: the build has no API, and likes keep coming.
  await connection();
  const t = await getTranslations({ locale, namespace: "ranking" });
  let rows: RankedCard[];
  try {
    rows = await getRanking();
  } catch (error) {
    console.error("The ranking could not be loaded", error);
    return <p role="alert" className="rounded-card border border-line bg-surface p-4 text-sm text-ink-body">{t("unavailable")}</p>;
  }
  if (rows.length === 0) {
    return <p className="rounded-card border border-line bg-surface p-4 text-sm leading-normal text-ink-body">{t("empty")}</p>;
  }

  return (
    <ol className="flex flex-col gap-2">
      {rows.map((row) => (
        <li key={row.slug}>
          <Link
            href={localizedPath(locale, `/p/${row.slug}`)}
            className="group rise-in flex items-center gap-3 rounded-card border border-line bg-surface p-2 pr-4 transition-[transform,box-shadow,border-color] duration-300 ease-out-soft active:scale-[0.99] pointer-fine:hover:-translate-y-0.5 pointer-fine:hover:border-ink-muted pointer-fine:hover:shadow-[0_12px_28px_-16px_rgba(18,20,18,0.3)]"
          >
            <span className={`w-8 shrink-0 text-center font-mono text-lg font-semibold tabular-nums ${row.rank <= 3 ? "text-accent-ink" : "text-ink-muted"}`}>
              {row.rank}
            </span>
            <span className="w-[92px] shrink-0 overflow-hidden rounded-control sm:w-[128px]">
              <Preview slug={row.slug} siteUrl={row.siteUrl} imageUrl={row.previewImageUrl} size="thumb" sizes="128px" />
            </span>
            <span className="flex min-w-0 flex-1 flex-col gap-0.5">
              <span className="truncate text-[15px] font-semibold">{row.ownerLogin}</span>
              <span className="truncate font-mono text-xs text-ink-muted">{siteLabel(row.siteUrl)}</span>
            </span>
            <span className="flex shrink-0 items-center gap-1.5 text-sm font-semibold" aria-label={t("likes", { count: row.likes })}>
              <IconHeart className="size-[18px] fill-current text-accent" />
              <span className="font-mono tabular-nums">{row.likes}</span>
            </span>
          </Link>
        </li>
      ))}
    </ol>
  );
}

function RankingSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-2">
      {[0, 1, 2, 3, 4].map((index) => (
        <div key={index} className="h-[74px] animate-pulse rounded-card bg-surface-sunken sm:h-[96px]" />
      ))}
    </div>
  );
}

export default async function RankingPage({ params }: PageProps<"/[locale]/ranking">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "ranking" });
  return (
    <div className="flex max-w-4xl flex-col gap-4 px-4 py-5 lg:px-8 lg:py-7">
      <section className="relative isolate flex items-center justify-between gap-4 overflow-hidden rounded-panel border border-line bg-surface/80 px-4 py-4 backdrop-blur-sm sm:px-6 sm:py-5 lg:px-8">
        <div className="flex min-w-0 flex-col gap-1.5">
          <h1 className="font-display text-[24px] leading-none font-bold tracking-[-0.035em] sm:text-[30px] lg:text-[40px]">{t("title")}</h1>
          <p className="text-sm leading-snug text-ink-muted sm:text-[15px]">{t("lead")}</p>
        </div>
        <Podium className="h-[84px] w-auto shrink-0 sm:h-[120px] lg:h-[140px]" />
      </section>
      <Suspense fallback={<RankingSkeleton />}>
        <RankingList locale={locale} />
      </Suspense>
    </div>
  );
}

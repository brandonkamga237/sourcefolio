"use client";

import Link from "next/link";
import { Suspense } from "react";
import { IconClose, IconCode, IconExternal } from "@/components/icons";
import { LikeButton, type LikeButtonLabels } from "@/components/likes/LikeButton";
import type { CatalogCard } from "@/services/catalog-types";
import { StarCount } from "@/components/stars/StarCount";
import { Preview } from "./Preview";

export interface ShowcasePanelLabels {
  close: string;
  visitSite: string;
  viewCode: string;
  openPage: string;
  sourceRepo: string;
  /** Contains {date}. */
  publishedOn: string;
  likes: LikeButtonLabels;
  /** Contains {count}; picked by the plural rule of the locale. */
  stars: { one: string; other: string };
}

interface ShowcasePanelProps {
  card: CatalogCard;
  href: string;
  locale: string;
  labels: ShowcasePanelLabels;
  onClose: () => void;
}

// The showcase opened beside the catalogue on a wide screen (spec), so browsing goes on without
// losing the place in the feed. External sites open in a new tab.
export function ShowcasePanel({ card, href, locale, labels, onClose }: ShowcasePanelProps) {
  const published = new Intl.DateTimeFormat(locale, { dateStyle: "long" }).format(new Date(card.publishedAt));
  return (
    <section aria-labelledby="panel-name" className="overflow-hidden rounded-panel border border-line bg-surface">
      <div className="relative">
        <Preview
          slug={card.slug}
          siteUrl={card.siteUrl}
          imageUrl={card.previewImageUrl}
          videoUrl={card.previewVideoUrl}
          size="large"
          sizes="400px"
        />
        <button
          type="button"
          onClick={onClose}
          aria-label={labels.close}
          className="press absolute top-3 right-3 flex size-11 items-center justify-center rounded-full bg-surface/95 text-ink shadow-sm transition-colors duration-150 hover:bg-surface"
        >
          <IconClose className="size-[18px]" />
        </button>
      </div>

      <div className="flex flex-col gap-4 p-5">
        <div className="flex items-center gap-3">
          <span aria-hidden="true" className="flex size-11 shrink-0 items-center justify-center rounded-full bg-accent-soft text-sm font-semibold text-accent-ink uppercase">
            {card.ownerLogin.slice(0, 2)}
          </span>
          <div className="flex min-w-0 flex-col">
            <h2 id="panel-name" className="truncate font-display text-[22px] leading-tight font-bold tracking-[-0.025em]">
              {card.ownerLogin}
            </h2>
            <span className="text-[13px] text-ink-muted">
              <span className="font-mono">@{card.ownerLogin}</span> · {labels.publishedOn.replace("{date}", published)}
            </span>
          </div>
        </div>

        <div className="flex gap-2">
          <a
            href={card.siteUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="press flex h-[46px] flex-1 items-center justify-center gap-1.5 rounded-control bg-accent text-sm font-semibold text-white transition-colors duration-150 hover:bg-accent-hover"
          >
            {labels.visitSite}
            <IconExternal className="size-4" />
          </a>
          <a
            href={card.repositoryUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="press flex h-[46px] items-center gap-1.5 rounded-control border border-ink px-4 text-sm font-semibold text-ink transition-colors duration-150 hover:bg-ink hover:text-ink-inverse"
          >
            <IconCode className="size-4" />
            {labels.viewCode}
          </a>
          <Suspense fallback={<div className="size-[46px] rounded-control border border-line" />}>
            <LikeButton key={card.slug} slug={card.slug} labels={labels.likes} showCount />
          </Suspense>
        </div>

        <div className="flex flex-col gap-1 border-t border-line-soft pt-4">
          <span className="text-xs text-ink-muted">{labels.sourceRepo}</span>
          <div className="flex items-start justify-between gap-3">
            <span className="font-mono text-[13px] break-all">{card.repository}</span>
            {card.repositoryStars != null ? (
              <a href={card.repositoryUrl} target="_blank" rel="noopener noreferrer" className="shrink-0 text-[13px] font-semibold text-ink hover:text-accent-ink">
                <StarCount
                  count={card.repositoryStars}
                  locale={locale}
                  label={labels.stars[new Intl.PluralRules(locale).select(card.repositoryStars) === "one" ? "one" : "other"].replace("{count}", String(card.repositoryStars))}
                />
              </a>
            ) : null}
          </div>
        </div>

        <Link href={href} className="flex h-11 items-center text-sm font-semibold text-accent-ink">
          {labels.openPage}
        </Link>
      </div>
    </section>
  );
}

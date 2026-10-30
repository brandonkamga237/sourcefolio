"use client";

import Link from "next/link";
import { IconClose, IconCode, IconExternal } from "@/components/icons";
import type { CatalogCard } from "@/services/catalog-types";
import { PreviewArt } from "./PreviewArt";

export interface ShowcasePanelLabels {
  close: string;
  visitSite: string;
  viewCode: string;
  openPage: string;
  sourceRepo: string;
  /** Contains {date}. */
  publishedOn: string;
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
        <PreviewArt slug={card.slug} siteUrl={card.siteUrl} large />
        <button
          type="button"
          onClick={onClose}
          aria-label={labels.close}
          className="absolute top-3 right-3 flex size-10 items-center justify-center rounded-full bg-surface/95 text-ink"
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
            className="flex h-[46px] flex-1 items-center justify-center gap-1.5 rounded-control bg-accent text-sm font-semibold text-white"
          >
            {labels.visitSite}
            <IconExternal className="size-4" />
          </a>
          <a
            href={card.repositoryUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex h-[46px] items-center gap-1.5 rounded-control border border-ink px-4 text-sm font-semibold text-ink"
          >
            <IconCode className="size-4" />
            {labels.viewCode}
          </a>
        </div>

        <div className="flex flex-col gap-1 border-t border-line-soft pt-4">
          <span className="text-xs text-ink-muted">{labels.sourceRepo}</span>
          <span className="font-mono text-[13px] break-all">{card.repository}</span>
        </div>

        <Link href={href} className="flex h-11 items-center text-sm font-semibold text-accent-ink">
          {labels.openPage}
        </Link>
      </div>
    </section>
  );
}

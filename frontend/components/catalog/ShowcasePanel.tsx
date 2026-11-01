"use client";

import Link from "next/link";
import { Suspense } from "react";
import { IconChevronLeft, IconChevronRight, IconClose, IconCode, IconCopy, IconExternal, IconGithub, IconHeart, IconStar } from "@/components/icons";
import { CopyAction } from "@/components/actions/CopyAction";
import { ShareAction } from "@/components/actions/ShareAction";
import { MemberSheet, type MemberSheetLabels } from "@/components/members/MemberSheet";
import type { Locale } from "@/i18n/routing";
import { countryName, flagOf } from "@/lib/countries";
import { compactNumber } from "@/lib/plural";
import { Avatar } from "@/components/members/Avatar";
import { LikeButton, type LikeButtonLabels } from "@/components/likes/LikeButton";
import type { CatalogCard } from "@/services/catalog-types";
import { Preview } from "./Preview";

export interface ShowcasePanelLabels {
  close: string;
  visitSite: string;
  viewCode: string;
  openPage: string;
  sourceRepo: string;
  /** Contains {date}. */
  publishedOn: string;
  previous: string;
  next: string;
  /** Contains {current} and {total}. */
  position: string;
  likes: LikeButtonLabels;
  stats: { likes: string; stars: string; published: string };
  share: string;
  /** Contains {login}. */
  shareTitle: string;
  clone: string;
  copied: string;
  githubProfile: string;
  member: MemberSheetLabels;
}

// "3 days ago", in the reader's language, from the largest unit that fits.
function sinceLabel(iso: string, locale: string): string {
  const seconds = (new Date(iso).getTime() - Date.now()) / 1000;
  const units: [Intl.RelativeTimeFormatUnit, number][] = [["year", 31536000], ["month", 2592000], ["week", 604800], ["day", 86400], ["hour", 3600], ["minute", 60]];
  const format = new Intl.RelativeTimeFormat(locale, { numeric: "auto", style: "short" });
  for (const [unit, size] of units) {
    if (Math.abs(seconds) >= size) return format.format(Math.round(seconds / size), unit);
  }
  return format.format(0, "minute");
}

interface ShowcasePanelProps {
  card: CatalogCard;
  /** Where the open card sits among those loaded; more: further pages exist. */
  position: { index: number; total: number; more: boolean };
  /** Which way the reader moved, so the content slides from that side. */
  direction: "next" | "prev" | "none";
  onPrev: (() => void) | null;
  onNext: (() => void) | null;
  href: string;
  locale: Locale;
  labels: ShowcasePanelLabels;
  onClose: () => void;
}

const entrance = {
  next: "animate-[slide-from-right_260ms_var(--ease-out-soft)]",
  prev: "animate-[slide-from-left_260ms_var(--ease-out-soft)]",
  none: "animate-rise",
};

const tileAction =
  "press flex min-h-[60px] flex-col items-center justify-center gap-1 rounded-control bg-surface-sunken px-1 text-center text-[11px] leading-tight font-semibold text-ink transition-colors duration-150 hover:bg-line-soft";

const navButton =
  "press flex size-11 items-center justify-center rounded-full text-ink transition-colors duration-150 hover:bg-surface-sunken disabled:pointer-events-none disabled:text-line";

// The showcase opened beside the catalogue on a wide screen (spec), so browsing goes on without
// losing the place in the feed. External sites open in a new tab.
export function ShowcasePanel({ card, position, direction, onPrev, onNext, href, locale, labels, onClose }: ShowcasePanelProps) {
  const published = new Intl.DateTimeFormat(locale, { dateStyle: "long" }).format(new Date(card.publishedAt));
  return (
    <section aria-labelledby="panel-name" className="overflow-hidden rounded-panel border border-line bg-surface shadow-[0_18px_44px_-28px_rgba(18,20,18,0.45)]">
      {/* Stays put while the content changes: browse, see where you are, close. */}
      <div className="flex items-center justify-between gap-2 border-b border-line-soft px-2 py-1.5">
        <div className="flex items-center gap-1">
          <button type="button" onClick={onPrev ?? undefined} disabled={!onPrev} aria-label={labels.previous} title={labels.previous} className={navButton}>
            <IconChevronLeft className="size-5" />
          </button>
          <button type="button" onClick={onNext ?? undefined} disabled={!onNext} aria-label={labels.next} title={labels.next} className={navButton}>
            <IconChevronRight className="size-5" />
          </button>
          <span className="px-1 font-mono text-xs text-ink-muted tabular-nums" aria-live="polite">
            {labels.position.replace("{current}", String(position.index + 1)).replace("{total}", `${position.total}${position.more ? "+" : ""}`)}
          </span>
        </div>
        <button type="button" onClick={onClose} aria-label={labels.close} title={labels.close} className={navButton}>
          <IconClose className="size-[18px]" />
        </button>
      </div>

      {/* Keyed on the card, so the content enters again on each switch: from the side the reader
          moved towards, or rising when a card was picked in the grid. */}
      <div key={card.slug} className={entrance[direction]}>
      <Preview
        slug={card.slug}
        siteUrl={card.siteUrl}
        imageUrl={card.previewImageUrl}
        videoUrl={card.previewVideoUrl}
        size="large"
        aspectClass="aspect-[2/1]"
        sizes="400px"
      />

      <div className="flex flex-col gap-3 p-4">
        {/* The author, one tap from their public profile: who made this, from where, doing what. */}
        <MemberSheet
          login={card.ownerLogin}
          locale={locale}
          labels={labels.member}
          triggerClassName="press -mx-2 flex items-center gap-3 rounded-control px-2 py-1 text-left transition-colors duration-150 hover:bg-surface-sunken"
        >
          <Avatar login={card.ownerLogin} size={44} />
          <span className="flex min-w-0 flex-1 flex-col">
            <span id="panel-name" className="truncate font-display text-[22px] leading-tight font-bold tracking-[-0.025em]">
              {card.ownerLogin}
            </span>
            <span className="truncate text-xs text-ink-muted">
              {[
                card.countryCode ? `${flagOf(card.countryCode)} ${countryName(card.countryCode, locale)}` : null,
                ...card.specialties.map((slug) => labels.member.specialtyNames[slug] ?? slug),
              ]
                .filter(Boolean)
                .join(" · ") || card.repository}
            </span>
          </span>
        </MemberSheet>

        {/* The numbers a visitor weighs first, side by side. */}
        <dl className="grid grid-cols-3 divide-x divide-line-soft rounded-control border border-line-soft bg-surface-raised">
          <div className="flex flex-col items-center gap-0.5 px-2 py-2.5">
            <dt className="order-2 text-[11px] text-ink-muted">{labels.stats.likes}</dt>
            <dd className="order-1 flex items-center gap-1 font-mono text-base font-semibold tabular-nums">
              <IconHeart aria-hidden="true" className={`size-4 ${card.likes > 0 ? "fill-accent text-accent" : "text-ink-muted"}`} />
              {compactNumber(card.likes, locale)}
            </dd>
          </div>
          <div className="flex flex-col items-center gap-0.5 px-2 py-2.5">
            <dt className="order-2 flex items-center gap-1 text-[11px] text-ink-muted">
              <IconGithub aria-hidden="true" className="size-3" />
              {labels.stats.stars}
            </dt>
            <dd className="order-1 flex items-center gap-1 font-mono text-base font-semibold tabular-nums">
              <IconStar aria-hidden="true" className="size-4 fill-accent text-accent" />
              {card.repositoryStars != null ? compactNumber(card.repositoryStars, locale) : "–"}
            </dd>
          </div>
          <div className="flex flex-col items-center gap-0.5 px-2 py-2.5">
            <dt className="order-2 text-[11px] text-ink-muted">{labels.stats.published}</dt>
            <dd className="order-1 text-sm font-semibold" title={labels.publishedOn.replace("{date}", published)} suppressHydrationWarning>
              {sinceLabel(card.publishedAt, locale)}
            </dd>
          </div>
        </dl>

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

        {/* Everything else a visitor may want to do with this portfolio, one tap each. */}
        <div className="grid grid-cols-3 gap-2">
          <ShareAction
            path={href}
            title={labels.shareTitle.replace("{login}", card.ownerLogin)}
            label={labels.share}
            copiedLabel={labels.copied}
            className={tileAction}
          />
          <CopyAction
            text={`git clone ${card.repositoryUrl}.git`}
            label={labels.clone}
            copiedLabel={labels.copied}
            icon={<IconCopy aria-hidden="true" className="size-4" />}
            className={tileAction}
          />
          <a href={`https://github.com/${card.ownerLogin}`} target="_blank" rel="noopener noreferrer" className={tileAction}>
            <IconGithub aria-hidden="true" className="size-4" />
            <span>{labels.githubProfile}</span>
          </a>
        </div>

        <Link
          href={href}
          className="press flex h-11 items-center justify-center rounded-control border border-line text-sm font-semibold text-ink transition-colors duration-150 hover:border-ink"
        >
          {labels.openPage}
        </Link>
      </div>
      </div>
    </section>
  );
}

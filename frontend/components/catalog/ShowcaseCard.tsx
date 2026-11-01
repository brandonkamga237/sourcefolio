import Link from "next/link";
import type { CSSProperties, MouseEvent } from "react";
import { IconHeart, IconStar } from "@/components/icons";
import { MemberSheet, type MemberSheetLabels } from "@/components/members/MemberSheet";
import type { Locale } from "@/i18n/routing";
import { flagOf } from "@/lib/countries";
import { compactNumber, plural, type PluralTemplates } from "@/lib/plural";
import { siteLabel } from "@/lib/site-label";
import { ShareAction } from "@/components/actions/ShareAction";
import { Avatar } from "@/components/members/Avatar";
import type { CatalogCard } from "@/services/catalog-types";
import { Preview } from "./Preview";

interface ShowcaseCardProps {
  card: CatalogCard;
  href: string;
  /** Position in the feed, which staggers the entrance. */
  index: number;
  selected: boolean;
  onOpen: (event: MouseEvent<HTMLAnchorElement>) => void;
  locale: Locale;
  labels: ShowcaseCardLabels;
}

export interface ShowcaseCardLabels {
  likes: PluralTemplates;
  stars: PluralTemplates;
  member: MemberSheetLabels;
  share: string;
  /** Contains {login}. */
  shareTitle: string;
  copied: string;
}

// A real link to the showcase page (phones open it; crawlers follow it). On a wide screen the
// feed intercepts the click and shows the side panel instead, as the mockup does.
export function ShowcaseCard({ card, href, index, selected, onOpen, locale, labels }: ShowcaseCardProps) {
  return (
    <article
      style={{ "--index": index % 24 } as CSSProperties}
      className={`group rise-in relative overflow-hidden rounded-card border bg-surface transition-[transform,box-shadow,border-color] duration-300 ease-out-soft pointer-fine:hover:-translate-y-1 pointer-fine:hover:shadow-[0_16px_34px_-16px_rgba(18,20,18,0.35)] ${
        selected ? "border-line lg:border-accent lg:ring-1 lg:ring-accent" : "border-line pointer-fine:hover:border-ink-muted"
      }`}
    >
      {/* The top opens the portfolio; the bottom, the person who made it. */}
      <Link href={href} onClick={onOpen} aria-current={selected ? "true" : undefined} aria-label={siteLabel(card.siteUrl)} className="relative block active:opacity-90">
      <Preview
        slug={card.slug}
        siteUrl={card.siteUrl}
        imageUrl={card.previewImageUrl}
        videoUrl={card.previewVideoUrl}
        sizes="(min-width: 1024px) 260px, 50vw"
      />
      {/* The basics at a glance, over the preview: how liked, how starred on GitHub. */}
      <span className="pointer-events-none absolute top-2 right-2 flex items-center gap-2 rounded-full bg-surface/90 px-2 py-1 font-mono text-[11px] font-semibold text-ink tabular-nums shadow-sm backdrop-blur-sm sm:top-2.5 sm:right-2.5 sm:text-xs">
        <span className="flex items-center gap-1" title={plural(labels.likes, card.likes, locale)}>
          <IconHeart aria-hidden="true" className={`size-3.5 ${card.likes > 0 ? "fill-accent text-accent" : ""}`} />
          {compactNumber(card.likes, locale)}
        </span>
        {card.repositoryStars != null ? (
          <span className="flex items-center gap-1" title={plural(labels.stars, card.repositoryStars, locale)}>
            <IconStar aria-hidden="true" className="size-3.5 fill-current" />
            {compactNumber(card.repositoryStars, locale)}
          </span>
        ) : null}
        <span className="sr-only">
          {plural(labels.likes, card.likes, locale)}
          {card.repositoryStars != null ? `, ${plural(labels.stars, card.repositoryStars, locale)}` : ""}
        </span>
      </span>
      </Link>
      <div className="flex items-center">
        <MemberSheet
          login={card.ownerLogin}
          locale={locale}
          labels={labels.member}
          triggerClassName="flex min-w-0 flex-1 items-center gap-2.5 py-2.5 pr-1 pl-3 text-left transition-colors duration-150 hover:bg-surface-raised sm:py-3 sm:pl-3.5"
        >
          <Avatar login={card.ownerLogin} size={30} className="ring-2 ring-surface transition-[box-shadow] duration-200 group-hover:ring-accent-soft" />
          <span className="flex min-w-0 flex-col gap-0.5">
            <span className="truncate text-sm font-semibold sm:text-[15px]">{card.ownerLogin}</span>
            <span className="truncate text-[11px] text-ink-muted sm:text-xs">
              {card.countryCode ? `${flagOf(card.countryCode)} ` : ""}
              {card.specialties.length > 0 ? labels.member.specialtyNames[card.specialties[0]] : card.repository}
            </span>
          </span>
        </MemberSheet>
        {/* Sharing a portfolio is what brings visitors: one tap from every card. */}
        <ShareAction
          path={href}
          title={labels.shareTitle.replace("{login}", card.ownerLogin)}
          label={labels.share}
          copiedLabel={labels.copied}
          iconOnly
          className="press mr-1.5 flex size-11 shrink-0 items-center justify-center rounded-full text-ink-muted transition-colors duration-150 hover:bg-surface-sunken hover:text-ink"
        />
      </div>
    </article>
  );
}
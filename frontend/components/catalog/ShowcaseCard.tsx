import Link from "next/link";
import type { CSSProperties, MouseEvent } from "react";
import { IconHeart, IconStar } from "@/components/icons";
import { compactNumber, plural, type PluralTemplates } from "@/lib/plural";
import type { CatalogCard } from "@/services/catalog-types";
import { Preview } from "./Preview";

interface ShowcaseCardProps {
  card: CatalogCard;
  href: string;
  /** Position in the feed, which staggers the entrance. */
  index: number;
  selected: boolean;
  onOpen: (event: MouseEvent<HTMLAnchorElement>) => void;
  locale: string;
  labels: ShowcaseCardLabels;
}

export interface ShowcaseCardLabels {
  likes: PluralTemplates;
  stars: PluralTemplates;
}

// A real link to the showcase page (phones open it; crawlers follow it). On a wide screen the
// feed intercepts the click and shows the side panel instead, as the mockup does.
export function ShowcaseCard({ card, href, index, selected, onOpen, locale, labels }: ShowcaseCardProps) {
  return (
    <Link
      href={href}
      onClick={onOpen}
      aria-current={selected ? "true" : undefined}
      style={{ "--index": index % 24 } as CSSProperties}
      className={`group rise-in relative block overflow-hidden rounded-card border bg-surface transition-[transform,box-shadow,border-color] duration-300 ease-out-soft active:scale-[0.98] pointer-fine:hover:-translate-y-1 pointer-fine:hover:shadow-[0_16px_34px_-16px_rgba(18,20,18,0.35)] ${
        selected ? "border-line lg:border-accent lg:ring-1 lg:ring-accent" : "border-line pointer-fine:hover:border-ink-muted"
      }`}
    >
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
      <span className="flex items-center gap-2.5 px-3 pt-2.5 pb-3 sm:px-3.5 sm:pt-3 sm:pb-3.5">
        <span
          aria-hidden="true"
          className="hidden size-8 shrink-0 items-center justify-center rounded-full bg-accent-soft text-[11px] font-semibold text-accent-ink uppercase transition-colors duration-200 group-hover:bg-accent group-hover:text-white sm:flex"
        >
          {card.ownerLogin.slice(0, 2)}
        </span>
        <span className="flex min-w-0 flex-col gap-0.5">
          <span className="truncate text-sm font-semibold sm:text-[15px]">{card.ownerLogin}</span>
          <span className="truncate font-mono text-[11px] text-ink-muted sm:text-xs">{card.repository}</span>
        </span>
      </span>
    </Link>
  );
}

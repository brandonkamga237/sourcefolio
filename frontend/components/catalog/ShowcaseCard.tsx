import Link from "next/link";
import type { MouseEvent } from "react";
import type { CatalogCard } from "@/services/catalog-types";
import { PreviewArt } from "./PreviewArt";

interface ShowcaseCardProps {
  card: CatalogCard;
  href: string;
  selected: boolean;
  onOpen: (event: MouseEvent<HTMLAnchorElement>) => void;
}

// A real link to the showcase page (phones open it; crawlers follow it). On a wide screen the
// feed intercepts the click and shows the side panel instead, as the mockup does.
export function ShowcaseCard({ card, href, selected, onOpen }: ShowcaseCardProps) {
  return (
    <Link
      href={href}
      onClick={onOpen}
      aria-current={selected ? "true" : undefined}
      className={`block overflow-hidden rounded-card bg-surface transition-transform active:scale-[0.98] ${
        selected ? "border border-line lg:border-2 lg:border-accent" : "border border-line hover:border-ink-muted"
      }`}
    >
      <PreviewArt slug={card.slug} siteUrl={card.siteUrl} />
      <span className="flex flex-col gap-0.5 px-3 pt-2.5 pb-3 sm:px-3.5 sm:pt-3 sm:pb-3.5">
        <span className="truncate text-sm font-semibold sm:text-[15px]">{card.ownerLogin}</span>
        <span className="truncate font-mono text-[11px] text-ink-muted sm:text-xs">{card.repository}</span>
      </span>
    </Link>
  );
}

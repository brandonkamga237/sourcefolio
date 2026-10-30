import Link from "next/link";
import { siteLabel } from "@/lib/site-label";
import type { CatalogCard } from "@/services/catalog-types";

interface ShowcaseCardProps {
  card: CatalogCard;
  href: string;
  byLabel: string;
}

// The whole card opens the showcase page, where the visitor chooses the site or the code.
export function ShowcaseCard({ card, href, byLabel }: ShowcaseCardProps) {
  return (
    <Link href={href} className="flex flex-col gap-1.5 rounded-card border border-line bg-surface p-4 active:bg-surface-sunken">
      <span className="font-display text-[22px] leading-tight font-bold tracking-[-0.025em] break-all">{siteLabel(card.siteUrl)}</span>
      <span className="text-sm text-ink-body">{byLabel}</span>
      <span className="font-mono text-xs break-all text-ink-soft">{card.repository}</span>
    </Link>
  );
}

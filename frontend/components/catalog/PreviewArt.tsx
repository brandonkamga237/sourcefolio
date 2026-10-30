import { siteLabel } from "@/lib/site-label";

interface PreviewArtProps {
  slug: string;
  siteUrl: string;
  large?: boolean;
  /** Overrides the 16:10 frame, e.g. a shorter one on phones. */
  aspectClass?: string;
}

// Stands in for the preview until the member uploads one or the screenshot exists: the site's
// own name on a tint derived from the slug, so a card is recognisable without inventing data.
const tints = [
  "bg-ink text-ink-inverse",
  "bg-accent text-white",
  "bg-accent-soft text-accent-ink",
  "bg-surface-sunken text-ink",
  "bg-success text-white",
];

function tintOf(slug: string): string {
  let hash = 0;
  for (const char of slug) hash = (hash * 31 + char.charCodeAt(0)) >>> 0;
  return tints[hash % tints.length];
}

export function PreviewArt({ slug, siteUrl, large, aspectClass = "aspect-[16/10]" }: PreviewArtProps) {
  const label = siteLabel(siteUrl);
  const name = new URL(siteUrl).hostname.split(".")[0];
  return (
    <div aria-hidden="true" className={`flex ${aspectClass} flex-col justify-between overflow-hidden ${large ? "p-5" : "p-3 sm:p-4"} ${tintOf(slug)}`}>
      <span className="flex items-center gap-2">
        <span className="size-2.5 rounded-full bg-current opacity-60" />
        <span className="truncate font-mono text-[10px] opacity-70 sm:text-[11px]">{label}</span>
      </span>
      <span className={`line-clamp-2 font-display leading-[0.92] font-bold tracking-[-0.045em] break-words ${large ? "text-[40px]" : "text-[19px] sm:text-[26px]"}`}>
        {name}
      </span>
    </div>
  );
}

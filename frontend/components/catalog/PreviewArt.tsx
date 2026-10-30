import { siteLabel } from "@/lib/site-label";

interface PreviewArtProps {
  slug: string;
  siteUrl: string;
  /** thumb: a small square in lists; card: the feed; large: the side panel. */
  size?: "thumb" | "card" | "large";
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

const nameSize = {
  thumb: "text-[15px] truncate sm:text-[18px]",
  card: "line-clamp-2 break-words text-[19px] sm:text-[26px]",
  large: "line-clamp-2 break-words text-[40px]",
};

export function PreviewArt({ slug, siteUrl, size = "card", aspectClass = "aspect-[16/10]" }: PreviewArtProps) {
  const name = new URL(siteUrl).hostname.split(".")[0];
  return (
    <div
      aria-hidden="true"
      className={`flex ${aspectClass} flex-col overflow-hidden ${size === "thumb" ? "justify-end p-2.5" : "justify-between"} ${
        size === "large" ? "p-5" : size === "card" ? "p-3 sm:p-4" : ""
      } ${tintOf(slug)}`}
    >
      {size === "thumb" ? null : (
        <span className="flex items-center gap-2">
          <span className="size-2.5 shrink-0 rounded-full bg-current opacity-60" />
          <span className="truncate font-mono text-[10px] opacity-70 sm:text-[11px]">{siteLabel(siteUrl)}</span>
        </span>
      )}
      <span className={`font-display leading-[0.92] font-bold tracking-[-0.045em] ${nameSize[size]}`}>{name}</span>
    </div>
  );
}

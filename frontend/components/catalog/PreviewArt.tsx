import { siteLabel } from "@/lib/site-label";

interface PreviewArtProps {
  slug: string;
  siteUrl: string;
  /** thumb: a small square in lists; card: the feed; large: the side panel. */
  size?: "thumb" | "card" | "large";
  /** Overrides the 16:9 frame shared by every card, e.g. a shorter one on phones. */
  aspectClass?: string;
}

const nameSize = {
  thumb: "text-[13px] sm:text-[15px]",
  card: "text-[17px] sm:text-[22px]",
  large: "text-[30px]",
};

// Stands in for the preview until the member uploads one or the screenshot exists: the same
// quiet browser window for everyone, the site's own name in it, so cards stay comparable and
// nothing about the site is invented.
export function PreviewArt({ slug, siteUrl, size = "card", aspectClass = "aspect-video" }: PreviewArtProps) {
  const name = new URL(siteUrl).hostname.split(".")[0];
  return (
    <div aria-hidden="true" data-slug={slug} className={`relative flex ${aspectClass} flex-col overflow-hidden bg-surface-sunken`}>
      {size === "thumb" ? null : (
        <div className="flex items-center gap-1.5 border-b border-line-soft bg-surface/70 px-3 py-2">
          <span className="size-2 rounded-full bg-line" />
          <span className="size-2 rounded-full bg-line" />
          <span className="size-2 rounded-full bg-line" />
          <span className="ml-2 min-w-0 truncate rounded-full bg-surface px-2 py-0.5 font-mono text-[10px] text-ink-muted sm:text-[11px]">
            {siteLabel(siteUrl)}
          </span>
        </div>
      )}
      <div className="flex flex-1 items-center justify-center px-4">
        <span
          className={`line-clamp-2 text-center font-display leading-tight font-bold tracking-[-0.03em] break-words text-ink-soft transition-transform duration-500 ease-out-soft group-hover:scale-[1.03] ${nameSize[size]}`}
        >
          {name}
        </span>
      </div>
    </div>
  );
}

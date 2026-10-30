import { getTranslations } from "next-intl/server";
import { IconGithub, IconStar } from "@/components/icons";
import type { Locale } from "@/i18n/routing";
import { getProjectStars, PROJECT_REPOSITORY_URL } from "@/services/project";

interface ProjectStarsProps {
  locale: Locale;
  /** compact: the GitHub mark, the star and the number, for the phone's top bar. */
  compact?: boolean;
}

// SourceFolio practises what it shows: its own code is public, one tap from every page. Shaped
// like GitHub's own star button (the mark, then the count) so nobody wonders what it counts.
export async function ProjectStars({ locale, compact }: ProjectStarsProps) {
  const [t, stars] = await Promise.all([getTranslations({ locale, namespace: "stars" }), getProjectStars()]);
  const count = stars === null ? null : new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(stars);
  const label = stars === null ? t("projectShort") : t("project", { count: stars });

  return (
    <a
      href={PROJECT_REPOSITORY_URL}
      target="_blank"
      rel="noopener noreferrer"
      aria-label={label}
      title={label}
      className="press flex h-11 items-stretch overflow-hidden rounded-full border border-line bg-surface text-sm font-semibold text-ink transition-colors duration-150 hover:border-ink-muted"
    >
      <span aria-hidden="true" className={`flex min-w-0 items-center gap-2 ${compact ? "pr-2 pl-3" : "flex-1 pr-2 pl-4"}`}>
        <IconGithub className="size-[18px] shrink-0" />
        {compact ? null : <span className="truncate">{t("badge")}</span>}
      </span>
      {count === null ? null : (
        <span aria-hidden="true" className="flex shrink-0 items-center gap-1 border-l border-line bg-surface-sunken px-3 font-mono tabular-nums">
          <IconStar className="size-4 fill-accent text-accent" />
          {count}
        </span>
      )}
    </a>
  );
}

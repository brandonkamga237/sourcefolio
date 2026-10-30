import { getTranslations } from "next-intl/server";
import { IconStar } from "@/components/icons";
import type { Locale } from "@/i18n/routing";
import { getProjectStars, PROJECT_REPOSITORY_URL } from "@/services/project";

interface ProjectStarsProps {
  locale: Locale;
  /** compact: the icon and the number only, for the phone's top bar. */
  compact?: boolean;
}

// SourceFolio practises what it shows: its own code is public, one tap from every page.
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
      className={`press flex h-11 items-center gap-2 rounded-full border border-line bg-surface text-sm font-semibold text-ink transition-colors duration-150 hover:border-ink-muted ${
        compact ? "px-3" : "justify-between px-4"
      }`}
    >
      {compact ? null : <span aria-hidden="true">{t("projectShort")}</span>}
      <span aria-hidden="true" className="flex items-center gap-1 font-mono tabular-nums">
        <IconStar className="size-4 fill-accent text-accent" />
        {count ?? (compact ? "GitHub" : null)}
      </span>
    </a>
  );
}

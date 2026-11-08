import type { ComponentType, SVGProps } from "react";
import { getFormatter, getTranslations } from "next-intl/server";
import { IconCalendar, IconCommit, IconMerge, IconPulse, IconStar } from "@/components/icons";
import type { Indicators } from "@/services/contribution-types";

interface IndicatorCardsProps {
  indicators: Indicators;
}

interface Card {
  label: string;
  value: string;
  hint: string;
  proof: string;
  Icon: ComponentType<SVGProps<SVGSVGElement>>;
}

/** Five of the six indicators as figures, each one a link to the GitHub page that proves it. */
export async function IndicatorCards({ indicators }: IndicatorCardsProps) {
  const [t, format] = await Promise.all([getTranslations("profileCard"), getFormatter()]);
  const created = new Date(indicators.accountCreatedAt);
  const cards: Card[] = [
    {
      label: t("seniority"),
      Icon: IconCalendar,
      value: t("seniorityValue", { year: created.getUTCFullYear() }),
      hint: t("seniorityHint", { month: format.dateTime(created, { month: "long", year: "numeric", timeZone: "UTC" }) }),
      proof: indicators.proofs.profile,
    },
    {
      label: t("regularity"),
      Icon: IconPulse,
      value: t("regularityValue", { active: indicators.activeMonths }),
      hint: t("regularityHint"),
      proof: indicators.proofs.profile,
    },
    {
      label: t("volume"),
      Icon: IconCommit,
      value: format.number(indicators.publicContributions),
      hint: t("volumeHint"),
      proof: indicators.proofs.profile,
    },
    {
      label: t("stars"),
      Icon: IconStar,
      value: format.number(indicators.stars),
      hint: t("starsHint", { followers: indicators.followers }),
      proof: indicators.proofs.repositories,
    },
    {
      label: t("elsewhere"),
      Icon: IconMerge,
      value: format.number(indicators.mergedPullRequestsElsewhere),
      hint: t("elsewhereHint", { projects: indicators.externalProjects.length }),
      proof: indicators.proofs.mergedElsewhere,
    },
  ];

  return (
    <ul data-stagger="" className="grid grid-cols-2 gap-3 @3xl:grid-cols-5">
      {cards.map(({ label, value, hint, proof, Icon }) => (
        <li key={label} className="last:col-span-2 @3xl:last:col-span-1">
          <a href={proof} target="_blank" rel="noopener noreferrer" aria-label={`${label} ${value}. ${t("proof")}`} className="glow-card flex h-full flex-col gap-3 p-5">
            <Icon aria-hidden="true" className="size-6 text-level-3" />
            <span className="text-[15px] text-ink-muted">{label}</span>
            <span className="text-[32px] leading-none font-semibold tracking-[-0.03em] whitespace-nowrap tabular-nums">{value}</span>
            <span className="text-[14px] leading-snug text-ink-muted">{hint}</span>
          </a>
        </li>
      ))}
    </ul>
  );
}

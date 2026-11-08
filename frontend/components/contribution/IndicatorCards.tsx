import { getFormatter, getTranslations } from "next-intl/server";
import type { Indicators } from "@/services/contribution-types";

interface IndicatorCardsProps {
  indicators: Indicators;
}

interface Card {
  label: string;
  value: string;
  hint: string;
  proof: string;
}

/** Five of the six indicators as figures, each one a link to the GitHub page that proves it. */
export async function IndicatorCards({ indicators }: IndicatorCardsProps) {
  const [t, format] = await Promise.all([getTranslations("profileCard"), getFormatter()]);
  const created = new Date(indicators.accountCreatedAt);
  const cards: Card[] = [
    {
      label: t("seniority"),
      value: t("seniorityValue", { year: created.getUTCFullYear() }),
      hint: t("seniorityHint", { month: format.dateTime(created, { month: "long", year: "numeric", timeZone: "UTC" }) }),
      proof: indicators.proofs.profile,
    },
    {
      label: t("regularity"),
      value: t("regularityValue", { active: indicators.activeMonths }),
      hint: t("regularityHint"),
      proof: indicators.proofs.profile,
    },
    {
      label: t("volume"),
      value: format.number(indicators.publicContributions),
      hint: t("volumeHint"),
      proof: indicators.proofs.profile,
    },
    {
      label: t("stars"),
      value: format.number(indicators.stars),
      hint: t("starsHint", { followers: indicators.followers }),
      proof: indicators.proofs.repositories,
    },
    {
      label: t("elsewhere"),
      value: format.number(indicators.mergedPullRequestsElsewhere),
      hint: t("elsewhereHint", { projects: indicators.externalProjects.length }),
      proof: indicators.proofs.mergedElsewhere,
    },
  ];

  return (
    <ul className="grid grid-cols-2 gap-px overflow-hidden rounded-card border border-line bg-line @3xl:grid-cols-5">
      {cards.map((card) => (
        <li key={card.label} className="last:col-span-2 @3xl:last:col-span-1">
          <a
            href={card.proof}
            target="_blank"
            rel="noopener noreferrer"
            aria-label={`${card.label} ${card.value}. ${t("proof")}`}
            className="flex h-full min-h-11 flex-col gap-1 bg-canvas p-4 transition-colors duration-150 hover:bg-surface-raised"
          >
            <span className="text-[13px] text-ink-muted">{card.label}</span>
            <span className="text-[24px] leading-tight font-semibold tracking-[-0.02em] whitespace-nowrap tabular-nums">{card.value}</span>
            <span className="text-[12px] leading-snug text-ink-muted">{card.hint}</span>
          </a>
        </li>
      ))}
    </ul>
  );
}

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
    <ul className="grid grid-cols-2 gap-3 @3xl:grid-cols-5">
      {cards.map((card) => (
        <li key={card.label} className="min-w-0 last:col-span-2 @3xl:last:col-span-1">
          <a href={card.proof} target="_blank" rel="noopener noreferrer" aria-label={`${card.label} ${card.value}. ${t("proof")}`} className="spotlight flex h-full min-h-11 flex-col gap-1.5 rounded-card border border-line bg-surface p-4 transition-colors duration-(--dur-2) hover:border-accent">
            <span className="text-[14px] text-ink-muted">{card.label}</span>
            <span className="font-mono text-[20px] leading-tight font-semibold tabular-nums @3xl:text-[26px]">{card.value}</span>
            <span className="text-[13px] leading-snug text-ink-muted">{card.hint}</span>
            <span className="proof mt-auto truncate pt-1">{card.proof.replace(/^https?:\/\//, "").split("?")[0]}</span>
          </a>
        </li>
      ))}
    </ul>
  );
}

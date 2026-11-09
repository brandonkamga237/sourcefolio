import type { ProfileCard } from "@/services/contribution-types";

export type IndicatorKey = "regularity" | "volume" | "stars" | "external" | "languages" | "seniority";
export type IndicatorLabels = Record<IndicatorKey, string>;

export interface IndicatorView {
  key: IndicatorKey;
  label: string;
  /** Counted up when it appears; absent for a value that reads as text. */
  count?: number;
  suffix?: string;
  text?: string;
  /** The GitHub page proving it; empty on the example. */
  proof: string;
  /** What the link shows, so the proof reads without opening it. */
  proofLabel: string;
}

const shortUrl = (url: string, fallback: string) => (url ? url.replace(/^https?:\/\//, "").split("?")[0] : fallback);

/** The six indicators of a profile, in reading order, each with its proof. */
export function indicatorViews(card: ProfileCard, labels: IndicatorLabels): IndicatorView[] {
  const { indicators, login } = card;
  const home = `github.com/${login}`;
  return [
    { key: "regularity", label: labels.regularity, count: indicators.activeMonths, suffix: "/12", proof: indicators.proofs.profile, proofLabel: shortUrl(indicators.proofs.profile, home) },
    { key: "volume", label: labels.volume, count: indicators.publicContributions, proof: indicators.proofs.profile, proofLabel: shortUrl(indicators.proofs.profile, home) },
    { key: "stars", label: labels.stars, count: indicators.stars, proof: indicators.proofs.repositories, proofLabel: shortUrl(indicators.proofs.repositories, `${home}?tab=repositories`.split("?")[0]) },
    { key: "external", label: labels.external, count: indicators.mergedPullRequestsElsewhere, proof: indicators.proofs.mergedElsewhere, proofLabel: "github.com/pulls" },
    { key: "languages", label: labels.languages, text: indicators.languages[0]?.name ?? "—", proof: indicators.proofs.repositories, proofLabel: shortUrl(indicators.proofs.repositories, home) },
    { key: "seniority", label: labels.seniority, text: String(new Date(indicators.accountCreatedAt).getUTCFullYear()), proof: indicators.proofs.profile, proofLabel: shortUrl(indicators.proofs.profile, home) },
  ];
}

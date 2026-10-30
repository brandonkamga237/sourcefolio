import { getTranslations } from "next-intl/server";
import { IconCode, IconExternal } from "@/components/icons";
import type { Locale } from "@/i18n/routing";
import { getMyCandidates, type ShowcaseCandidate } from "@/services/showcases";
import { RefreshWhilePending } from "./RefreshWhilePending";

interface CandidatesPanelProps {
  locale: Locale;
}

type Translate = Awaited<ReturnType<typeof getTranslations<"detection">>>;

function siteLabel(siteUrl: string): string {
  const url = new URL(siteUrl);
  return url.pathname === "/" ? url.host : `${url.host}${url.pathname.replace(/\/$/, "")}`;
}

function reasonLabel(t: Translate, candidate: ShowcaseCandidate): string {
  if (candidate.reason === "NAMED_REPOSITORY") {
    return t("reason.NAMED_REPOSITORY", { name: candidate.repository.split("/")[1] ?? candidate.repository });
  }
  return t(`reason.${candidate.reason}`);
}

// External sites open in a new tab: many portfolios refuse to be framed, and the member stays
// on their dashboard.
function CandidateLinks({ t, candidate, compact }: { t: Translate; candidate: ShowcaseCandidate; compact?: boolean }) {
  return (
    <div className="flex flex-wrap gap-2">
      <a
        href={candidate.siteUrl}
        target="_blank"
        rel="noopener noreferrer"
        className={`flex h-11 items-center justify-center gap-1.5 rounded-control px-4 text-sm font-semibold ${
          compact ? "border border-line bg-surface text-ink" : "bg-accent text-white"
        }`}
      >
        {t("visitSite")}
        <IconExternal className="size-4" />
      </a>
      <a
        href={candidate.repositoryUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="flex h-11 items-center justify-center gap-1.5 rounded-control border border-line bg-surface px-4 text-sm font-semibold text-ink"
      >
        <IconCode className="size-4" />
        {t("viewCode")}
      </a>
    </div>
  );
}

export async function CandidatesPanel({ locale }: CandidatesPanelProps) {
  const [t, detection] = await Promise.all([getTranslations({ locale, namespace: "detection" }), getMyCandidates()]);

  if (detection === null) {
    return null;
  }

  if (detection.scannedAt === null) {
    return (
      <section aria-busy="true" className="flex flex-col gap-3">
        <p className="text-[15px] leading-normal text-ink-body">{t("searching")}</p>
        <div aria-hidden="true" className="h-40 animate-pulse rounded-card bg-surface-sunken" />
        <RefreshWhilePending />
      </section>
    );
  }

  if (detection.candidates.length === 0) {
    return (
      <section aria-labelledby="detection-none" className="flex flex-col gap-2 rounded-card border border-line bg-surface p-4">
        <h2 id="detection-none" className="text-[15px] font-semibold">{t("noneTitle")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("noneHint")}</p>
      </section>
    );
  }

  const [best, ...others] = detection.candidates;
  return (
    <section aria-labelledby="detection-found" className="flex flex-col gap-4">
      <h2 id="detection-found" className="text-[15px] font-semibold">{t("foundTitle")}</h2>

      <article className="flex flex-col gap-3 rounded-card border border-line bg-surface p-4">
        <div className="flex flex-col gap-1">
          <p className="font-display text-2xl font-bold tracking-[-0.025em] break-all">{siteLabel(best.siteUrl)}</p>
          <p className="text-[13px] text-ink-muted">{reasonLabel(t, best)}</p>
          <p className="font-mono text-xs text-ink-soft">{best.repository}</p>
        </div>
        <CandidateLinks t={t} candidate={best} />
      </article>

      {others.length > 0 ? (
        <div className="flex flex-col gap-2">
          <h3 className="text-[13px] font-semibold text-ink-muted">{t("othersTitle")}</h3>
          <ul className="flex flex-col gap-2">
            {others.map((candidate) => (
              <li key={candidate.siteUrl} className="flex flex-col gap-2 rounded-card border border-line bg-surface p-3.5">
                <div className="flex flex-col gap-0.5">
                  <p className="text-[15px] font-semibold break-all">{siteLabel(candidate.siteUrl)}</p>
                  <p className="text-[13px] text-ink-muted">
                    {reasonLabel(t, candidate)} · <span className="font-mono text-xs">{candidate.repository}</span>
                  </p>
                </div>
                <CandidateLinks t={t} candidate={candidate} compact />
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </section>
  );
}

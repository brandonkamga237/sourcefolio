import { getTranslations } from "next-intl/server";
import type { Locale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import { getMyCandidates, type ShowcaseCandidate } from "@/services/showcases";
import { PublishButton } from "./PublishButton";
import { RefreshWhilePending } from "./RefreshWhilePending";
import { ShowcaseLinks } from "./ShowcaseLinks";

interface CandidatesPanelProps {
  locale: Locale;
}

type Translate = Awaited<ReturnType<typeof getTranslations<"detection">>>;

function reasonLabel(t: Translate, candidate: ShowcaseCandidate): string {
  if (candidate.reason === "NAMED_REPOSITORY") {
    return t("reason.NAMED_REPOSITORY", { name: candidate.repository.split("/")[1] ?? candidate.repository });
  }
  return t(`reason.${candidate.reason}`);
}

// The publish button is the screen's main action on the best candidate; the others offer it
// too, in a quieter style, for members whose portfolio is not the first guess.
function CandidateActions({ t, candidate, primary }: { t: Translate; candidate: ShowcaseCandidate; primary?: boolean }) {
  return (
    <div className="flex flex-col gap-2">
      <PublishButton
        repository={candidate.repository}
        label={primary ? t("publish") : t("publishThis")}
        pendingLabel={t("publishing")}
        failedMessage={t("publishFailed")}
        primary={primary}
      />
      <ShowcaseLinks
        siteUrl={candidate.siteUrl}
        repositoryUrl={candidate.repositoryUrl}
        visitLabel={t("visitSite")}
        codeLabel={t("viewCode")}
      />
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
        <CandidateActions t={t} candidate={best} primary />
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
                <CandidateActions t={t} candidate={candidate} />
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </section>
  );
}

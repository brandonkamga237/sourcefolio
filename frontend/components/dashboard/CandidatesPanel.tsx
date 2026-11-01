import { getTranslations } from "next-intl/server";
import type { Locale } from "@/i18n/routing";
import { sinceLabel } from "@/lib/relative-time";
import { siteLabel } from "@/lib/site-label";
import { getMyCandidates, type ShowcaseCandidate, type ShowcaseCandidates } from "@/services/showcases";
import { PublishButton } from "./PublishButton";
import { RefreshWhilePending } from "./RefreshWhilePending";
import { RepositoryPicker, type RepositoryPickerLabels } from "./RepositoryPicker";
import { RescanButton, type RescanLabels } from "./RescanButton";
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

function rescanLabels(t: Translate): RescanLabels {
  return { rescan: t("rescan"), rescanning: t("rescanning"), failed: t("rescanFailed") };
}

function pickerLabels(t: Translate, found: boolean): RepositoryPickerLabels {
  return {
    title: found ? t("missingTitle") : t("otherRepositories"),
    lead: t("missingLead"),
    search: t("searchRepositories"),
    noMatch: t("noRepositoryMatch"),
    none: t("noRepositories"),
    showAll: t.raw("showAll") as string,
    pick: t("pick"),
    stepsTitle: t.raw("stepsTitle") as string,
    steps: [t("step1"), t("step2"), t("step3")],
    openOnGithub: t("openOnGithub"),
    rescan: rescanLabels(t),
  };
}

// When the list was made and how to make it again, at the top of the card: the member is
// never stuck with an old search after filling a "Website" field on GitHub.
function SearchStatus({ t, detection, locale }: { t: Translate; detection: ShowcaseCandidates; locale: Locale }) {
  return (
    <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
      <RescanButton scannedAt={detection.scannedAt} labels={rescanLabels(t)} />
      <p className="text-xs text-ink-muted">{t("lastSearch", { ago: sinceLabel(detection.scannedAt ?? "", locale) })}</p>
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

  // One card, one question: which repository is the portfolio. The detected sites come first,
  // publishable in one tap; every other repository follows, with the way to make it publishable.
  const found = detection.candidates.length > 0;
  const [best, ...others] = detection.candidates;
  return (
    <section aria-labelledby="detection-title" className="flex flex-col gap-4 rounded-panel border border-line bg-surface p-4 sm:p-5">
      <div className="flex flex-col gap-3">
        <div className="flex flex-col gap-1">
          <h3 id="detection-title" className="font-display text-[22px] leading-tight font-bold tracking-[-0.025em]">{t("pickTitle")}</h3>
          <p className="text-sm leading-normal text-ink-body">{found ? t("pickLeadFound") : t("pickLeadNone")}</p>
        </div>
        <SearchStatus t={t} detection={detection} locale={locale} />
      </div>

      {best ? (
        <article className="flex flex-col gap-3 rounded-card border border-accent-line bg-accent-soft/40 p-4">
          <div className="flex flex-col gap-1">
            <p className="font-display text-2xl font-bold tracking-[-0.025em] break-all">{siteLabel(best.siteUrl)}</p>
            <p className="text-[13px] text-ink-muted">{reasonLabel(t, best)}</p>
            <p className="font-mono text-xs text-ink-soft">{best.repository}</p>
          </div>
          <CandidateActions t={t} candidate={best} primary />
        </article>
      ) : null}

      {others.length > 0 ? (
        <ul className="flex flex-col gap-2">
          {others.map((candidate) => (
            <li key={candidate.siteUrl} className="flex flex-col gap-2 rounded-card border border-line p-3.5">
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
      ) : null}

      <RepositoryPicker repositories={detection.withoutSite} scannedAt={detection.scannedAt} labels={pickerLabels(t, found)} open={!found} />
    </section>
  );
}

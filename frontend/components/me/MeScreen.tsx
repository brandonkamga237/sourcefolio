import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { SignOutButton } from "@/components/auth/SignOutButton";
import { CandidatesPanel } from "@/components/dashboard/CandidatesPanel";
import { PreviewEditor } from "@/components/dashboard/PreviewEditor";
import { PublishedShowcase } from "@/components/dashboard/PublishedShowcase";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryOptions } from "@/lib/countries";
import { getMe } from "@/services/me";
import { getMyAnswers } from "@/services/profile";
import { getMyShowcase } from "@/services/showcases";
import { ManageShowcase } from "@/components/dashboard/ManageShowcase";
import { JOURNEY_COOKIE } from "@/lib/journey";
import { cookies } from "next/headers";
import { DeleteAccount } from "./DeleteAccount";
import { JourneyCard } from "./JourneyCard";
import { ForgetJourneyDismissal } from "./JourneyDismissal";
import { MePreferences } from "./MePreferences";

interface MeScreenProps {
  locale: Locale;
}

const sectionTitle = "text-[13px] font-semibold tracking-wide text-ink-muted uppercase";
// Two columns on a wide screen: the portfolio, what members come for, then account matters.
const columns = "grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_340px]";

// Everything about the member in one place (spec: the Me tab): where they stand and what to do
// next, their portfolio and its preview, their profile answers, then account matters folded
// away. A visitor gets one action, signing in with GitHub,
// which starts the detection of their portfolio. Reads the session, so it renders inside a
// Suspense boundary.
export async function MeScreen({ locale }: MeScreenProps) {
  const [t, member] = await Promise.all([getTranslations({ locale }), getMe()]);

  if (!member) {
    const steps = [t("pages.portfolio.step1"), t("pages.portfolio.step2"), t("pages.portfolio.step3")];
    return (
      <div className={columns}>
        <section aria-labelledby="me-visitor" className="flex flex-col gap-4 rounded-panel border border-line bg-surface p-5 lg:p-6">
          <div className="flex flex-col gap-2">
            <h1 id="me-visitor" className="font-display text-[26px] leading-tight font-bold tracking-[-0.03em] lg:text-[32px]">
              {t("pages.portfolio.visitorTitle")}
            </h1>
            <p className="text-[15px] leading-normal text-ink-body">{t("pages.portfolio.visitorLead")}</p>
          </div>
          <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} />
          <ol className="grid gap-2 sm:grid-cols-3">
            {steps.map((step, index) => (
              <li key={index} className="flex items-start gap-2.5 rounded-control bg-surface-sunken p-3">
                <span aria-hidden="true" className="flex size-6 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent-ink">
                  {index + 1}
                </span>
                <span className="text-[13px] leading-snug text-ink-body">{step}</span>
              </li>
            ))}
          </ol>
        </section>
        <MePreferences locale={locale} />
      </div>
    );
  }

  const [mine, answers, jar] = await Promise.all([getMyShowcase(), getMyAnswers(), cookies()]);
  const showcase = mine?.showcase ?? null;
  const countryName = answers?.country ? countryOptions(locale).find((option) => option.code === answers.country)?.name : null;
  const profileRows = [
    { label: t("profile.countryLabel"), value: countryName ?? null },
    { label: t("profile.specialtyLabel"), value: answers?.specialty ? t(`specialty.${answers.specialty}`) : null },
    { label: t("profile.emailLabel"), value: answers?.email ?? null },
  ];
  const profileIncomplete = answers !== null && (!answers.country || !answers.specialty);
  const allDone = showcase !== null && showcase.previewImageUrl !== null && !profileIncomplete;
  const journeyDismissed = jar.get(JOURNEY_COOKIE)?.value === "dismissed";
  const dialog = (key: "manage.changeDialog" | "manage.withdrawDialog" | "account.deleteDialog", extra?: { typeToConfirm: string }) => ({
    title: t(`${key}.title`),
    description: t(`${key}.description`),
    confirm: t(`${key}.confirm`),
    pending: t(`${key}.pending`),
    cancel: t("common.cancel"),
    ...extra,
  });
  // Scrolled to from the journey card: clear of the sticky top bar on a phone.
  const anchored = "scroll-mt-20";

  return (
    <div className={columns}>
      <div className="flex min-w-0 flex-col gap-6">
        <header className="flex items-center gap-3">
          <span aria-hidden="true" className="flex size-12 shrink-0 items-center justify-center rounded-full bg-accent-soft text-base font-semibold text-accent-ink uppercase">
            {member.login.slice(0, 2)}
          </span>
          <div className="flex min-w-0 flex-1 flex-col">
            <h1 className="truncate font-display text-[24px] leading-tight font-bold tracking-[-0.025em]">{member.login}</h1>
            <a href={`https://github.com/${member.login}`} target="_blank" rel="noopener noreferrer" className="w-fit font-mono text-[13px] text-ink-muted">
              @{member.login}
            </a>
          </div>
        </header>

        {allDone && journeyDismissed ? null : <JourneyCard locale={locale} showcase={showcase} profileComplete={!profileIncomplete} />}
        {!allDone && journeyDismissed ? <ForgetJourneyDismissal /> : null}

        <section id="me-portfolio" aria-labelledby="me-portfolio-title" className={`flex flex-col gap-3 ${anchored}`}>
          <h2 id="me-portfolio-title" className={sectionTitle}>{t("me.portfolioTitle")}</h2>
          {showcase ? (
            <>
              <PublishedShowcase locale={locale} showcase={showcase} />
              <div id="me-preview" className={anchored}>
                <PreviewEditor
                  hasImage={showcase.previewImageUrl !== null}
                  hasVideo={showcase.previewVideoUrl !== null}
                  labels={{
                    title: t("preview.title"),
                    lead: t("preview.lead"),
                    imageLabel: t("preview.imageLabel"),
                    imageHint: t("preview.imageHint"),
                    videoLabel: t("preview.videoLabel"),
                    videoHint: t("preview.videoHint"),
                    choose: t("preview.choose"),
                    change: t("preview.change"),
                    remove: t("preview.remove"),
                    sending: t("preview.sending"),
                    tooBig: t("preview.tooBig"),
                    failed: t("preview.failed"),
                  }}
                />
              </div>
              <ManageShowcase
                labels={{
                  title: t("manage.title"),
                  lead: t("manage.lead"),
                  change: t("manage.change"),
                  withdraw: t("manage.withdraw"),
                  failed: t("manage.failed"),
                  changeDialog: dialog("manage.changeDialog"),
                  withdrawDialog: dialog("manage.withdrawDialog"),
                }}
              />
            </>
          ) : (
            <>
              <p className="text-sm leading-normal text-ink-body">{t("pages.portfolio.memberEmpty")}</p>
              <CandidatesPanel locale={locale} />
            </>
          )}
        </section>
      </div>

      <div className="flex flex-col gap-6">
        <section aria-labelledby="me-profile" className="flex flex-col gap-3">
          <div className="flex items-center justify-between gap-3">
            <h2 id="me-profile" className={sectionTitle}>{t("profile.summaryTitle")}</h2>
            <Link href={localizedPath(locale, "/me/profile")} className="press flex h-11 items-center text-sm font-semibold text-accent-ink">
              {profileIncomplete ? t("profile.reminderLink") : t("profile.edit")}
            </Link>
          </div>
          <dl className="grid divide-y divide-line-soft rounded-card border border-line bg-surface">
            {profileRows.map((row) => (
              <div key={row.label} className="flex min-w-0 items-center justify-between gap-3 px-4 py-3">
                <dt className="text-[13px] text-ink-muted">{row.label}</dt>
                <dd className={`truncate text-sm font-semibold ${row.value ? "" : "text-ink-muted"}`}>{row.value ?? t("profile.notGiven")}</dd>
              </div>
            ))}
          </dl>
        </section>
        {/* Settings are visited rarely: folded, so the portfolio keeps the attention. */}
        <details className="group/account rounded-card border border-line bg-surface [&_summary::-webkit-details-marker]:hidden">
          <summary className="flex h-12 cursor-pointer list-none items-center justify-between gap-3 px-4 text-sm font-semibold select-none">
            {t("me.accountTitle")}
            <span aria-hidden="true" className="size-2 rotate-45 border-r-2 border-b-2 border-ink-muted transition-transform duration-200 group-open/account:-rotate-135" />
          </summary>
          <div className="flex flex-col gap-4 border-t border-line-soft p-4">
            <MePreferences locale={locale} />
            <SignOutButton label={t("account.signOut")} failedMessage={t("account.signOutFailed")} />
            <div className="flex flex-col gap-2 border-t border-line-soft pt-4">
              <p className="text-[13px] leading-normal text-ink-muted">{t("account.deleteLead")}</p>
              <DeleteAccount
                login={member.login}
                homeHref={localizedPath(locale, "/")}
                label={t("account.delete")}
                failed={t("account.deleteFailed")}
                dialog={dialog("account.deleteDialog", { typeToConfirm: t.raw("account.deleteDialog.typeToConfirm") as string })}
              />
            </div>
          </div>
        </details>
      </div>
    </div>
  );
}

export function MeScreenSkeleton() {
  return (
    <div aria-hidden="true" className={columns}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center gap-3">
          <div className="size-12 animate-pulse rounded-full bg-surface-sunken" />
          <div className="h-6 w-40 animate-pulse rounded bg-surface-sunken" />
        </div>
        <div className="h-48 animate-pulse rounded-card bg-surface-sunken" />
      </div>
      <div className="h-40 animate-pulse rounded-card bg-surface-sunken" />
    </div>
  );
}

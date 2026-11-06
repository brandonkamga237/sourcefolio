import { getFormatter, getTranslations } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { AccountSettings } from "@/components/me/AccountSettings";
import { ProfileView } from "@/components/contribution/ProfileView";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryOptions } from "@/lib/countries";
import { getMyAnalysis } from "@/services/analysis";
import { getMe } from "@/services/me";
import { getMyAnswers } from "@/services/profile";
import { AnalysisWaiter } from "./AnalysisWaiter";
import { CompletionForm } from "./CompletionForm";
import { PublicationPanel } from "./PublicationPanel";
import { Steps } from "./Steps";

interface MyProfileScreenProps {
  locale: Locale;
}

/**
 * The member's own profile, in four steps: GitHub sign-in, the reading of their public data, the
 * few fields only they can give, and their consent to publish. The preview beside the form is
 * the very page visitors will see. Reads the session, so it renders inside a Suspense boundary.
 */
export async function MyProfileScreen({ locale }: MyProfileScreenProps) {
  const [t, m, format, member] = await Promise.all([
    getTranslations({ locale }),
    getTranslations({ locale, namespace: "myProfile" }),
    getFormatter({ locale }),
    getMe(),
  ]);

  if (!member) {
    return (
      <section aria-labelledby="my-profile-visitor" className="mx-auto flex max-w-xl flex-col gap-5 py-6">
        <h1 id="my-profile-visitor" className="font-display text-[28px] leading-tight font-semibold lg:text-[36px]">
          {t("myProfile.visitorTitle")}
        </h1>
        <p className="text-[16px] leading-relaxed text-ink-soft">{t("myProfile.visitorLead")}</p>
        <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} returnTo={localizedPath(locale, "/me")} />
      </section>
    );
  }

  const [analysis, answers] = await Promise.all([getMyAnalysis(), getMyAnswers()]);
  const indicators = analysis?.indicators ?? null;
  const ready = analysis?.status === "READY" && indicators !== null;
  const published = answers?.publishedAt != null;
  const login = analysis?.login ?? member.login;
  const site = process.env.SITE_URL ?? "http://localhost:3000";

  return (
    <div className="flex flex-col gap-6">
      <Steps
        label={m("stepsLabel")}
        steps={[
          { label: m("stepSignIn"), done: true },
          { label: m("stepAnalysis"), done: ready },
          { label: m("stepComplete"), done: ready && answers?.country != null },
          { label: m("stepPublish"), done: published },
        ]}
      />

      <AnalysisWaiter
        status={analysis?.status ?? "NONE"}
        refreshing={analysis?.refreshing ?? false}
        labels={{
          title: m("analysingTitle"), lead: m("analysingLead"), failedTitle: m("failedTitle"),
          failedLead: m("failedLead"), retry: m("retry"), refreshing: m("refreshing"),
        }}
      />

      {ready && answers ? (
        <>
          <section className="flex flex-col gap-3 rounded-card border border-success bg-surface p-5 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-col gap-1">
              <h1 className="font-display text-[20px] font-semibold">{t("myProfile.readyTitle", { login })}</h1>
              <p className="text-[14px] text-ink-muted">{m("readyLead")}</p>
            </div>
            <dl className="flex flex-wrap gap-x-5 gap-y-1 text-[13px] text-ink-muted">
              {[
                [t("profileCard.seniorityValue", { year: new Date(indicators.accountCreatedAt).getUTCFullYear() }), t("profileCard.seniority")],
                [t("profileCard.regularityValue", { active: indicators.activeMonths }), t("profileCard.regularity")],
                [format.number(indicators.publicContributions), t("profileCard.volume")],
                [format.number(indicators.stars), t("profileCard.stars")],
              ].map(([value, label]) => (
                <div key={label} className="flex gap-1">
                  <dt className="sr-only">{label}</dt>
                  <dd><strong className="text-ink">{value}</strong> {label.toLowerCase()}</dd>
                </div>
              ))}
            </dl>
          </section>

          <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,420px)_1fr]">
            <div className="flex flex-col gap-8">
              <CompletionForm
                country={answers.country}
                city={answers.city}
                bio={answers.bio}
                githubBio={indicators.bio}
                linkedIn={answers.linkedIn}
                countries={countryOptions(locale)}
                labels={{
                  country: m("country"), countryHint: m("countryHint"), countryPlaceholder: m("countryPlaceholder"),
                  city: m("city"), optional: m("optional"), cityHint: m("cityHint"),
                  bio: m("bio"), bioImported: m("bioImported"), bioHint: m.raw("bioHint") as string,
                  linkedIn: m("linkedIn"), linkedInHint: m("linkedInHint"),
                  saving: m("saving"), saved: m("saved"), failed: m("failed"),
                }}
              />
              <PublicationPanel
                published={published}
                canPublish={answers.country !== null}
                publicUrl={`${site}${localizedPath(locale, `/u/${login}`)}`}
                labels={{
                  visibility: m("visibility"), privateTitle: m("privateTitle"), privateLead: m("privateLead"),
                  publicTitle: m("publicTitle"), publicLead: m("publicLead"), publish: m("publish"), publishing: m("publishing"),
                  unpublish: m("unpublish"), unpublishing: m("unpublishing"), needCountry: m("needCountry"),
                  publicLink: m("publicLink"), copy: m("copy"), copied: m("copied"), failed: m("failed"),
                }}
              />
            </div>
            <section aria-label={m("preview")} className="flex min-w-0 flex-col gap-3">
              <p className="text-[13px] font-semibold text-ink-muted">{m("preview")}</p>
              <div className="rounded-panel border border-line p-4 lg:p-6">
                <ProfileView
                  login={login}
                  name={indicators.name}
                  country={answers.country}
                  city={answers.city}
                  bio={answers.bio}
                  linkedInUrl={answers.linkedInUrl}
                  indicators={indicators}
                />
              </div>
              <p className="text-[12px] text-ink-muted">{m("shownNotRanked")}</p>
            </section>
          </div>
        </>
      ) : null}

      <div className="lg:max-w-[420px]">
        <AccountSettings locale={locale} login={member.login} />
      </div>
    </div>
  );
}

import Image from "next/image";
import { getTranslations } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { ProfileBuildCard } from "@/components/landing/ProfileBuildCard";
import { AccountSettings } from "@/components/me/AccountSettings";
import { CopyButton } from "@/components/ui/CopyButton";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryOptions } from "@/lib/countries";
import { SECTORS } from "@/lib/globe";
import { getMyAnalysis } from "@/services/analysis";
import type { ProfileCard } from "@/services/contribution-types";
import { getMe } from "@/services/me";
import { getMyAnswers } from "@/services/profile";
import { AnalysisWaiter } from "./AnalysisWaiter";
import { CompletionForm } from "./CompletionForm";
import { CompletionSteps } from "./CompletionSteps";
import { DashboardActions } from "./DashboardActions";

const FIRST_PROJECT = "https://github.com/topics/good-first-issue";

/**
 * Three moments, one address: reading GitHub, completing and publishing, then managing what is
 * public. Reads the session, so it renders inside a Suspense boundary.
 */
export async function MyProfileScreen({ locale }: { locale: Locale }) {
  const [t, m, card, member] = await Promise.all([getTranslations({ locale }), getTranslations({ locale, namespace: "myProfile" }), getTranslations({ locale, namespace: "profileCard" }), getMe()]);

  if (!member) {
    return (
      <section aria-labelledby="my-profile-visitor" className="flex max-w-2xl flex-col items-start gap-5 py-10 lg:py-20">
        <h1 id="my-profile-visitor" className="t-display">{t("myProfile.visitorTitle")}</h1>
        <p className="text-[18px] leading-relaxed text-ink-soft">{t("myProfile.visitorLead")}</p>
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
  const waiter = (
    <AnalysisWaiter
      status={analysis?.status ?? "NONE"}
      refreshing={analysis?.refreshing ?? false}
      labels={{ title: m("analysingTitle"), lead: m("analysingLead"), failedTitle: m("failedTitle"), failedLead: m("failedLead"), retry: m("retry"), refreshing: m("refreshing"), reads: [card("volume"), card("elsewhere"), card("languages")] }}
    />
  );
  const settings = <div className="max-w-md"><AccountSettings locale={locale} login={member.login} /></div>;
  if (!ready || !answers || !indicators) {
    return (
      <div className="flex flex-col gap-8">
        {waiter}
        {settings}
      </div>
    );
  }

  const sector = answers.specialties.find((slug) => (SECTORS as readonly string[]).includes(slug)) ?? null;
  const sectorNames = Object.fromEntries(SECTORS.map((slug) => [slug, m(`sectors.${slug}`)]));
  const preview: ProfileCard = { login, name: indicators.name, country: answers.country ?? "", city: answers.city, sector, bio: answers.bio, linkedInUrl: answers.linkedInUrl, indicators };
  const indicatorLabels = { regularity: card("regularity"), volume: card("volume"), stars: card("stars"), external: card("elsewhere"), languages: card("languages"), seniority: card("seniority") };
  const cardLabels = { indicators: indicatorLabels, example: "" };
  const startingPoint = indicators.publicContributions === 0 ? (
    <aside className="flex flex-col items-start gap-2 rounded-panel border border-line bg-surface-raised p-5">
      <p className="t-h3">{m("startTitle")}</p>
      <p className="text-[15px] text-ink-soft">{m("startLead")}</p>
      <a href={FIRST_PROJECT} target="_blank" rel="noopener noreferrer" className="flex min-h-11 items-center text-[14px] font-semibold text-accent">{m("startLink")}</a>
    </aside>
  ) : null;

  if (!published) {
    return (
      <div className="flex flex-col gap-8">
        <header className="flex flex-col gap-1">
          <h1 className="t-h2">{t("myProfile.readyTitle", { login })}</h1>
          <p className="text-[16px] text-ink-soft">{m("readyLead")}</p>
        </header>
        {waiter}
        <div className="grid items-start gap-10 lg:grid-cols-[minmax(0,1fr)_minmax(0,560px)] lg:gap-16">
          <div className="flex flex-col gap-6">
            <CompletionSteps
              initial={{ country: answers.country, city: answers.city, bio: answers.bio, linkedIn: answers.linkedIn, sector }}
              githubBio={indicators.bio}
              countries={countryOptions(locale)}
              labels={{
                continue: m("continue"), back: m("back"), skip: m("skip"), failed: m("failed"), progress: m("stepsLabel"),
                country: m("country"), countryHint: m("countryHint"), countryPlaceholder: m("countryPlaceholder"),
                city: m("city"), cityHint: m("cityHint"),
                bio: m("bio"), bioHint: m.raw("bioHint") as string,
                linkedIn: m("linkedIn"), linkedInHint: m("linkedInHint"),
                sector: m("sector"), sectorHint: m("sectorHint"), sectorPlaceholder: m("sectorPlaceholder"), sectors: sectorNames,
                publishTitle: m("publishTitle"), publishLead: m("publishLead"), publish: t("nav.publish"), published: m("publishedDone"),
              }}
            />
            {startingPoint}
          </div>
          <ProfileBuildCard locale={locale} card={preview} example={false} labels={cardLabels} sectorName={sector ? sectorNames[sector] : null} />
        </div>
        {settings}
      </div>
    );
  }

  const url = `${site}${localizedPath(locale, `/u/${login}`)}`;
  const badge = `[![SourceFolio](${url}/badge.svg)](${url})`;
  return (
    <div className="flex flex-col gap-10">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <p className="flex items-center gap-2 text-[14px] font-semibold text-success"><span aria-hidden="true" className="size-2.5 rounded-[3px] bg-level-3" />{m("publishedDone")}</p>
          <h1 className="t-h2 break-all">{login}</h1>
        </div>
        <DashboardActions labels={{ unpublish: m("unpublish"), unpublished: m("privateTitle"), refresh: m("refresh"), refreshed: m("refreshDone"), failed: m("failed") }} />
      </header>
      {waiter}
      <div className="grid items-start gap-10 lg:grid-cols-[minmax(0,560px)_minmax(0,1fr)] lg:gap-16">
        <div className="flex flex-col gap-3">
          <ProfileBuildCard instant locale={locale} card={preview} example={false} labels={cardLabels} sectorName={sector ? sectorNames[sector] : null} />
          <a href={url} className="flex min-h-11 w-fit items-center font-mono text-[13px] text-accent">{url.replace(/^https?:\/\//, "")}</a>
        </div>
        <section aria-labelledby="share-title" className="flex flex-col gap-4">
          <h2 id="share-title" className="t-h3">{m("share")}</h2>
          <div className="flex flex-wrap gap-2">
            <CopyButton value={url} label={m("copy")} copied={m("copied")} />
            <CopyButton value={badge} label={m("copyBadge")} copied={m("badgeCopied")} />
          </div>
          <Image src={localizedPath(locale, `/u/${login}/opengraph-image`)} alt={m("ogAlt")} width={480} height={252} unoptimized className="h-auto w-full max-w-md rounded-card border border-line" />
          {startingPoint}
          <details className="group/edit border-t border-line [&_summary::-webkit-details-marker]:hidden">
            <summary className="flex min-h-12 cursor-pointer list-none items-center text-[14px] font-semibold">{m("edit")}</summary>
            <div className="pb-4">
              <CompletionForm
                country={answers.country}
                city={answers.city}
                bio={answers.bio}
                githubBio={indicators.bio}
                linkedIn={answers.linkedIn}
                countries={countryOptions(locale)}
                labels={{ country: m("country"), countryHint: m("countryHint"), countryPlaceholder: m("countryPlaceholder"), city: m("city"), optional: m("optional"), cityHint: m("cityHint"), bio: m("bio"), bioImported: m("bioImported"), bioHint: m.raw("bioHint") as string, linkedIn: m("linkedIn"), linkedInHint: m("linkedInHint"), saving: m("saving"), saved: m("saved"), failed: m("failed") }}
              />
            </div>
          </details>
        </section>
      </div>
      {settings}
    </div>
  );
}

export function MyProfileScreenSkeleton() {
  return (
    <div aria-hidden="true" className="grid items-start gap-10 lg:grid-cols-[minmax(0,1fr)_minmax(0,560px)]">
      <div className="flex flex-col gap-4">
        <div className="h-2 animate-pulse rounded bg-surface-sunken" />
        <div className="h-40 animate-pulse rounded-card bg-surface-sunken" />
      </div>
      <div className="h-96 animate-pulse rounded-panel bg-surface-sunken" />
    </div>
  );
}

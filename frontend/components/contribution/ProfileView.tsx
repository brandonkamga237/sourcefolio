import { ViewTransition, type ReactNode } from "react";
import { getFormatter, getLocale, getTranslations } from "next-intl/server";
import { Avatar } from "@/components/members/Avatar";
import { countryName } from "@/lib/countries";
import type { Indicators } from "@/services/contribution-types";
import { ContributionCalendar } from "./ContributionCalendar";
import { ExternalProjects } from "./ExternalProjects";
import { IndicatorCards } from "./IndicatorCards";
import { LanguageBar } from "./LanguageBar";
import { TopRepositories } from "./TopRepositories";

interface ProfileViewProps {
  login: string;
  name: string | null;
  /** ISO 3166-1 alpha-2; null in a preview before the member chose one. */
  country: string | null;
  city: string | null;
  sector?: string | null;
  bio: string | null;
  linkedInUrl: string | null;
  indicators: Indicators;
  actions?: ReactNode;
}

/** Built for a recruiter's ten seconds: who, then the figures, then the year; the detail below. */
export async function ProfileView({ login, name, country, city, sector, bio, linkedInUrl, indicators, actions }: ProfileViewProps) {
  const [t, format, locale] = await Promise.all([getTranslations("profileCard"), getFormatter(), getLocale()]);
  const place = [city, country ? countryName(country, locale) : null, sector].filter(Boolean).join(" · ");
  const link = "flex min-h-11 items-center font-mono text-[14px] text-accent hover:underline";

  return (
    <article className="@container flex flex-col gap-8">
      <header className="flex flex-col gap-5 @xl:flex-row @xl:items-start">
        <ViewTransition name={`avatar-${login}`}>
          <Avatar login={login} size={104} />
        </ViewTransition>
        <div className="flex min-w-0 flex-1 flex-col gap-3">
          <div className="flex flex-col gap-1">
            <h1 className="t-h2 break-words">{name ?? login}</h1>
            <p className="text-[17px] text-ink-muted">{[name ? login : null, place].filter(Boolean).join(" · ")}</p>
          </div>
          {bio ? <p className="max-w-2xl text-[18px] leading-relaxed text-ink-body">{bio}</p> : null}
          <ul className="flex flex-wrap gap-x-5">
            <li><a href={indicators.proofs.profile} target="_blank" rel="noopener noreferrer" className={link}>{t("github", { login })}</a></li>
            {linkedInUrl ? <li><a href={linkedInUrl} target="_blank" rel="noopener noreferrer" className={link}>{linkedInUrl.replace("https://www.", "")}</a></li> : null}
          </ul>
        </div>
        {actions}
      </header>
      <IndicatorCards indicators={indicators} />
      <ContributionCalendar calendar={indicators.calendar} total={indicators.publicContributions} proofUrl={indicators.proofs.profile} />
      <div className="grid gap-8 @3xl:grid-cols-2">
        <LanguageBar languages={indicators.languages} proofUrl={indicators.proofs.repositories} />
        <ExternalProjects projects={indicators.externalProjects} />
      </div>
      <TopRepositories repositories={indicators.topRepositories} />
      <footer className="flex flex-col gap-1 border-t border-line pt-4 text-[14px] leading-relaxed text-ink-muted">
        <p>{t("analyzedOn", { date: format.dateTime(new Date(indicators.analyzedAt), { dateStyle: "long" }) })}</p>
        <p>{t("publicOnly")}</p>
      </footer>
    </article>
  );
}

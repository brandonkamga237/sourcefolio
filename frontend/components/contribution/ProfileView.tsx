import type { ReactNode } from "react";
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
  bio: string | null;
  linkedInUrl: string | null;
  indicators: Indicators;
  /** Share buttons on the public page, nothing in a preview. */
  actions?: ReactNode;
}

const external = "link text-[14px]";

/**
 * A profile reads like one document: who the person is in their own words, then what GitHub
 * proves. The same view serves the public page and the member's private preview.
 */
export async function ProfileView({ login, name, country, city, bio, linkedInUrl, indicators, actions }: ProfileViewProps) {
  const [t, format, locale] = await Promise.all([getTranslations("profileCard"), getFormatter(), getLocale()]);
  const place = [city, country ? countryName(country, locale) : null].filter(Boolean).join(", ");

  return (
    <article className="@container flex flex-col gap-10">
      <header className="flex flex-col gap-5 @xl:flex-row @xl:items-start">
        <Avatar login={login} size={80} />
        <div className="flex min-w-0 flex-1 flex-col gap-3">
          <div className="flex flex-col gap-1">
            <h1 className="text-[32px] leading-[1.1] font-semibold tracking-[-0.03em] @xl:text-[40px]">{name ?? login}</h1>
            <p className="text-[15px] text-ink-muted">{[name ? login : null, place].filter(Boolean).join(" · ")}</p>
          </div>
          {bio ? <p className="max-w-2xl text-[16px] leading-relaxed text-ink-body">{bio}</p> : null}
          <ul className="flex flex-wrap gap-x-5 gap-y-1">
            <li>
              <a href={indicators.proofs.profile} target="_blank" rel="noopener noreferrer" className={external}>{t("github", { login })}</a>
            </li>
            {linkedInUrl ? (
              <li>
                <a href={linkedInUrl} target="_blank" rel="noopener noreferrer" className={external}>{linkedInUrl.replace("https://www.", "")}</a>
              </li>
            ) : null}
          </ul>
          {actions}
        </div>
      </header>
      <IndicatorCards indicators={indicators} />
      <ContributionCalendar calendar={indicators.calendar} total={indicators.publicContributions} proofUrl={indicators.proofs.profile} />
      <div className="grid gap-10 @3xl:grid-cols-2">
        <LanguageBar languages={indicators.languages} proofUrl={indicators.proofs.repositories} />
        <ExternalProjects projects={indicators.externalProjects} />
      </div>
      <TopRepositories repositories={indicators.topRepositories} />
      <footer className="flex flex-col gap-1 border-t border-line-soft pt-4 text-[13px] leading-relaxed text-ink-muted">
        <p>{t("analyzedOn", { date: format.dateTime(new Date(indicators.analyzedAt), { dateStyle: "long" }) })}</p>
        <p>{t("publicOnly")}</p>
      </footer>
    </article>
  );
}

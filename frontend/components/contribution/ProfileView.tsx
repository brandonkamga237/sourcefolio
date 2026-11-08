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

const external = "link flex min-h-11 items-center text-[16px]";

/**
 * A profile reads like one document: who the person is in their own words, then what GitHub
 * proves. The same view serves the public page and the member's private preview.
 */
export async function ProfileView({ login, name, country, city, bio, linkedInUrl, indicators, actions }: ProfileViewProps) {
  const [t, format, locale] = await Promise.all([getTranslations("profileCard"), getFormatter(), getLocale()]);
  const place = [city, country ? countryName(country, locale) : null].filter(Boolean).join(", ");

  return (
    <article className="@container flex flex-col gap-14">
      <header className="dark-band hero-lights animate-rise flex flex-col gap-6 rounded-[24px] p-7 @xl:flex-row @xl:items-center @xl:gap-10 @xl:p-12">
        <Avatar login={login} size={128} />
        <div className="flex min-w-0 flex-1 flex-col gap-4">
          <div className="flex flex-col gap-2">
            <h1 className="text-[44px] leading-[1.02] font-semibold tracking-[-0.04em] @xl:text-[72px]">{name ?? login}</h1>
            <p className="text-[18px] text-ink-muted @xl:text-[20px]">{[name ? login : null, place].filter(Boolean).join(" · ")}</p>
          </div>
          {bio ? <p className="max-w-2xl text-[18px] leading-relaxed text-ink-body @xl:text-[20px]">{bio}</p> : null}
          <ul className="flex flex-wrap gap-x-6 gap-y-1">
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
      <footer className="flex flex-col gap-1 border-t border-line-soft pt-6 text-[15px] leading-relaxed text-ink-muted">
        <p>{t("analyzedOn", { date: format.dateTime(new Date(indicators.analyzedAt), { dateStyle: "long" }) })}</p>
        <p>{t("publicOnly")}</p>
      </footer>
    </article>
  );
}

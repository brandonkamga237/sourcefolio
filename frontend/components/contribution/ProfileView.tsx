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

/**
 * A profile: who the person is in their own words on the left, what GitHub proves on the right.
 * The same view serves the public page and the member's private preview.
 */
export async function ProfileView({ login, name, country, city, bio, linkedInUrl, indicators, actions }: ProfileViewProps) {
  const [t, format, locale] = await Promise.all([getTranslations("profileCard"), getFormatter(), getLocale()]);
  const place = [city, country ? countryName(country, locale) : null].filter(Boolean).join(", ");

  return (
    <div className="grid gap-6 lg:grid-cols-[280px_1fr] lg:gap-8">
      <aside className="flex flex-col gap-4">
        <div className="flex items-center gap-4 lg:flex-col lg:items-start">
          <Avatar login={login} size={96} className="lg:size-[200px]! lg:text-[48px]!" />
          <div className="flex min-w-0 flex-col">
            {name ? <h1 className="font-display text-[24px] leading-tight font-semibold">{name}</h1> : null}
            <p className={name ? "text-[18px] text-ink-muted" : "font-display text-[24px] font-semibold"}>{login}</p>
          </div>
        </div>
        {bio ? <p className="text-[15px] leading-relaxed text-ink-body">{bio}</p> : null}
        <ul className="flex flex-col gap-2 text-[14px]">
          {place ? <li className="text-ink-soft">{place}</li> : null}
          {linkedInUrl ? (
            <li>
              <a href={linkedInUrl} target="_blank" rel="noopener noreferrer" className="text-accent hover:underline">
                {linkedInUrl.replace("https://www.", "")}
              </a>
            </li>
          ) : null}
          <li>
            <a href={indicators.proofs.profile} target="_blank" rel="noopener noreferrer" className="text-accent hover:underline">
              {t("github", { login })}
            </a>
          </li>
        </ul>
        {actions}
        <div className="flex flex-col gap-1 border-t border-line pt-3 text-[12px] leading-snug text-ink-muted">
          <p>{t("analyzedOn", { date: format.dateTime(new Date(indicators.analyzedAt), { dateStyle: "long" }) })}</p>
          <p>{t("publicOnly")}</p>
        </div>
      </aside>
      <div className="flex min-w-0 flex-col gap-4">
        <IndicatorCards indicators={indicators} />
        <ContributionCalendar calendar={indicators.calendar} total={indicators.publicContributions} proofUrl={indicators.proofs.profile} />
        <div className="grid gap-4 xl:grid-cols-2">
          <LanguageBar languages={indicators.languages} proofUrl={indicators.proofs.repositories} />
          <ExternalProjects projects={indicators.externalProjects} />
        </div>
        <TopRepositories repositories={indicators.topRepositories} />
      </div>
    </div>
  );
}

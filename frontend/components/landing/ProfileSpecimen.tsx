import Link from "next/link";
import { connection } from "next/server";
import { getFormatter, getTranslations } from "next-intl/server";
import { CalendarArt } from "@/components/contribution/CalendarArt";
import { Avatar } from "@/components/members/Avatar";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import type { CalendarCell } from "@/lib/calendar";
import { countryName } from "@/lib/countries";
import { getPublicProfile } from "@/services/public-profiles";
import { getRankings } from "@/services/rankings";
import type { ProfileCard } from "@/services/contribution-types";

const frame = "flex flex-col gap-5 rounded-panel border border-line bg-surface p-5 shadow-[0_24px_60px_-32px_rgba(0,0,0,0.28)] sm:p-6";

// A year that reads as someone finding their rhythm. Only ever drawn under the "Example" label.
function exampleCalendar(): CalendarCell[] {
  const start = Date.UTC(2025, 10, 2);
  return Array.from({ length: 364 }, (_, day) => {
    const week = Math.floor(day / 7);
    const wave = (day * 7 + week * 3) % 11;
    const level = wave > 7 + (40 - Math.min(40, week)) / 12 ? 0 : Math.min(4, Math.floor(wave / 2 + week / 30)) as CalendarCell["level"];
    return { date: new Date(start + day * 86_400_000).toISOString().slice(0, 10), level: level as CalendarCell["level"] };
  });
}

async function topProfile(): Promise<ProfileCard | null> {
  try {
    const first = (await getRankings({ sort: "regularity", country: null, language: null, cursor: null })).items[0];
    return first ? await getPublicProfile(first.login) : null;
  } catch (error) {
    console.error("The profile specimen could not be loaded", error);
    return null;
  }
}

/** The product itself in the first screen: a real published profile, or a labelled example until one exists. */
export async function ProfileSpecimen({ locale }: { locale: Locale }) {
  await connection();
  const [t, card, profile, format] = await Promise.all([
    getTranslations({ locale, namespace: "landing.profile" }),
    getTranslations({ locale, namespace: "profileCard" }),
    topProfile(),
    getFormatter({ locale }),
  ]);

  const figures = profile
    ? [
        [card("regularityValue", { active: profile.indicators.activeMonths }), card("regularity")],
        [format.number(profile.indicators.publicContributions), card("volume")],
        [format.number(profile.indicators.stars), card("stars")],
        [format.number(profile.indicators.mergedPullRequestsElsewhere), card("elsewhere")],
      ]
    : [
        [card("regularityValue", { active: 9 }), card("regularity")],
        [format.number(412), card("volume")],
        [format.number(12), card("stars")],
        [format.number(3), card("elsewhere")],
      ];

  const body = (
    <>
      <div className="flex items-center gap-3">
        {profile ? (
          <Avatar login={profile.login} size={44} />
        ) : (
          <span aria-hidden="true" className="size-11 rounded-full bg-surface-sunken" />
        )}
        <div className="flex min-w-0 flex-1 flex-col">
          <span className="truncate text-[15px] font-medium">{profile ? (profile.name ?? profile.login) : t("exampleLogin")}</span>
          <span className="truncate text-[13px] text-ink-muted">
            {profile ? [profile.city, countryName(profile.country, locale)].filter(Boolean).join(" · ") : `github.com/${t("exampleLogin")}`}
          </span>
        </div>
        {profile ? null : <span className="rounded-tag border border-line px-2 py-0.5 text-[12px] text-ink-muted">{t("example")}</span>}
      </div>
      <CalendarArt calendar={profile ? profile.indicators.calendar : exampleCalendar()} />
      <dl className="grid grid-cols-2 gap-x-4 gap-y-3 border-t border-line-soft pt-4 sm:grid-cols-4">
        {figures.map(([value, label]) => (
          <div key={label} className="flex flex-col-reverse gap-0.5">
            <dt className="text-[12px] text-ink-muted">{label}</dt>
            <dd className="text-[17px] font-medium tracking-[-0.01em] tabular-nums">{value}</dd>
          </div>
        ))}
      </dl>
    </>
  );

  return profile ? (
    <Link href={localizedPath(locale, `/u/${profile.login}`)} className={`${frame} transition-transform duration-200 hover:-translate-y-0.5`}>
      {body}
    </Link>
  ) : (
    <div className={frame}>{body}</div>
  );
}

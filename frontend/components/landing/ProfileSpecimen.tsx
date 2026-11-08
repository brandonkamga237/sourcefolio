import Link from "next/link";
import { getFormatter, getTranslations } from "next-intl/server";
import { connection } from "next/server";
import { CalendarArt } from "@/components/contribution/CalendarArt";
import { Avatar } from "@/components/members/Avatar";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import type { CalendarCell } from "@/lib/calendar";
import { countryName } from "@/lib/countries";
import type { ProfileCard } from "@/services/contribution-types";
import { getPublicProfile } from "@/services/public-profiles";
import { getRankings } from "@/services/rankings";

const frame = "block overflow-hidden rounded-panel border border-line bg-surface shadow-[0_40px_120px_-40px_rgba(38,166,65,0.45)]";

// A year that reads as someone finding their rhythm. Only ever drawn under the "Example" label.
function exampleCalendar(): CalendarCell[] {
  const start = Date.UTC(2025, 10, 2);
  return Array.from({ length: 364 }, (_, day) => {
    const week = Math.floor(day / 7);
    const wave = (day * 7 + week * 3) % 11;
    const level = (wave > 7 + (40 - Math.min(40, week)) / 12 ? 0 : Math.min(4, Math.floor(wave / 2 + week / 30))) as CalendarCell["level"];
    return { date: new Date(start + day * 86_400_000).toISOString().slice(0, 10), level };
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

/** The product itself: a real published profile, or a labelled example until one exists. */
export async function ProfileSpecimen({ locale }: { locale: Locale }) {
  await connection();
  const [t, card, profile, format] = await Promise.all([
    getTranslations({ locale, namespace: "landing.profile" }),
    getTranslations({ locale, namespace: "profileCard" }),
    topProfile(),
    getFormatter({ locale }),
  ]);
  const handle = profile?.login ?? t("exampleLogin");
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
      <div className="flex items-center gap-2 border-b border-line-soft bg-surface-raised px-4 py-3">
        <span aria-hidden="true" className="flex gap-1.5">
          <span className="size-3 rounded-full bg-level-0" />
          <span className="size-3 rounded-full bg-level-0" />
          <span className="size-3 rounded-full bg-level-0" />
        </span>
        <span className="ml-3 truncate rounded-control bg-surface-sunken px-3 py-1 text-[13px] text-ink-muted">sourcefolio/u/{handle}</span>
        {profile ? null : <span className="ml-auto rounded-tag border border-line px-2 py-0.5 text-[13px] text-ink-muted">{t("example")}</span>}
      </div>
      <div className="flex flex-col gap-8 p-6 lg:p-10">
        <div className="flex items-center gap-5">
          {profile ? <Avatar login={profile.login} size={72} /> : <span aria-hidden="true" className="size-[72px] rounded-full bg-surface-sunken" />}
          <div className="flex min-w-0 flex-col gap-1">
            <span className="truncate text-[26px] font-semibold tracking-[-0.02em] lg:text-[34px]">{profile ? (profile.name ?? profile.login) : handle}</span>
            <span className="truncate text-[16px] text-ink-muted">{profile ? [profile.city, countryName(profile.country, locale)].filter(Boolean).join(" · ") : `github.com/${handle}`}</span>
          </div>
        </div>
        <div data-wipe="">
          <CalendarArt calendar={profile ? profile.indicators.calendar : exampleCalendar()} />
        </div>
        <dl data-stagger="" className="grid grid-cols-2 gap-6 lg:grid-cols-4">
          {figures.map(([value, label]) => (
            <div key={label} className="flex flex-col-reverse gap-1">
              <dt className="text-[15px] text-ink-muted">{label}</dt>
              <dd className="text-[32px] leading-none font-semibold tracking-[-0.03em] tabular-nums lg:text-[44px]">{value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </>
  );

  return profile ? (
    <Link href={localizedPath(locale, `/u/${profile.login}`)} className={`${frame} transition-transform duration-300 hover:-translate-y-1`}>
      {body}
    </Link>
  ) : (
    <div className={frame}>{body}</div>
  );
}

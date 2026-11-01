import { getTranslations } from "next-intl/server";
import { IconGithub, IconHeart, IconRanking, IconStar } from "@/components/icons";
import { Avatar } from "@/components/members/Avatar";
import { memberSheetLabels } from "@/components/members/labels";
import { MemberSheet } from "@/components/members/MemberSheet";
import type { Locale } from "@/i18n/routing";
import { countryName, flagOf } from "@/lib/countries";
import { compactNumber } from "@/lib/plural";
import type { Specialty } from "@/services/profile-types";
import { getPublicMember } from "@/services/public-members";
import { getRanking } from "@/services/ranking";
import type { MyShowcase } from "@/services/showcases";

interface MeOverviewProps {
  locale: Locale;
  login: string;
  countryCode: string | null;
  specialties: Specialty[];
  headline: string | null;
  showcase: MyShowcase | null;
}

// Counted from the first day, so a portfolio published this morning reads "1 day".
function daysSince(iso: string): number {
  return Math.max(1, Math.round((Date.now() - new Date(iso).getTime()) / 86_400_000));
}

// Who the member is to the community, and how their portfolio is doing: the numbers that make
// coming back worth it. Read from the same public data visitors see, so nothing is invented.
export async function MeOverview({ locale, login, countryCode, specialties, headline, showcase }: MeOverviewProps) {
  const published = showcase?.status === "PUBLISHED";
  const [t, ts, labels, member, ranking] = await Promise.all([
    getTranslations({ locale, namespace: "me.overview" }),
    getTranslations({ locale }),
    memberSheetLabels(locale),
    published ? getPublicMember(login).catch(() => null) : Promise.resolve(null),
    published ? getRanking().catch(() => []) : Promise.resolve([]),
  ]);
  const rank = showcase ? ranking.find((row) => row.slug === showcase.slug)?.rank ?? null : null;
  const days = showcase?.publishedAt ? daysSince(showcase.publishedAt) : null;

  const stats = [
    { key: "likes", icon: <IconHeart className="size-4 fill-accent text-accent" />, value: member ? compactNumber(member.likesReceived, locale) : "–", label: t("likes") },
    {
      key: "stars",
      icon: <IconStar className="size-4 fill-accent text-accent" />,
      value: member?.showcase.repositoryStars != null ? compactNumber(member.showcase.repositoryStars, locale) : "–",
      label: t("stars"),
    },
    { key: "rank", icon: <IconRanking className="size-4 text-accent" />, value: rank ? `#${rank}` : "–", label: rank ? t("rank") : t("unranked") },
    { key: "days", icon: null, value: days ? compactNumber(days, locale) : "–", label: t("days") },
  ];

  return (
    <section aria-labelledby="me-name" className="overflow-hidden rounded-panel border border-line bg-surface">
      <div aria-hidden="true" className="h-16 bg-[linear-gradient(120deg,var(--color-accent-soft),var(--color-surface-sunken)_70%)] sm:h-20" />
      <div className="relative -mt-10 flex flex-col gap-4 px-4 pb-4 sm:px-5 sm:pb-5">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div className="flex min-w-0 items-end gap-3">
            <Avatar login={login} size={72} className="border-4 border-surface" />
            <div className="flex min-w-0 flex-col pb-1">
              <h1 id="me-name" className="truncate font-display text-[24px] leading-tight font-bold tracking-[-0.025em]">{login}</h1>
              <a
                href={`https://github.com/${login}`}
                target="_blank"
                rel="noopener noreferrer"
                className="flex w-fit items-center gap-1 font-mono text-[13px] text-ink-muted hover:text-ink"
              >
                <IconGithub className="size-3.5" />@{login}
              </a>
            </div>
          </div>
          {published ? (
            <MemberSheet
                login={login}
                locale={locale}
                labels={labels}
                triggerClassName="press flex h-11 items-center rounded-control border border-line px-3.5 text-sm font-semibold text-ink transition-colors duration-150 hover:border-ink"
              >
                {t("publicProfile")}
              </MemberSheet>
          ) : null}
        </div>

        {headline ? <p className="text-[15px] leading-snug font-medium text-ink-body">{headline}</p> : null}

        <div className="flex flex-wrap items-center gap-1.5 text-[13px]">
          <span className="flex h-8 items-center rounded-full bg-surface-sunken px-3 text-ink-body">
            {countryCode ? `${flagOf(countryCode)} ${countryName(countryCode, locale)}` : t("noCountry")}
          </span>
          {specialties.map((slug, index) => (
            <span
              key={slug}
              className={`flex h-8 items-center gap-1.5 rounded-full px-3 font-semibold ${index === 0 ? "bg-ink text-ink-inverse" : "border border-line text-ink"}`}
            >
              <span className="font-mono text-[11px] opacity-60">{index + 1}</span>
              {ts(`specialty.${slug}`)}
            </span>
          ))}
        </div>

        {showcase ? (
          <dl className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {stats.map((stat) => (
              <div key={stat.key} className="flex flex-col gap-0.5 rounded-control bg-surface-raised px-3 py-2.5 ring-1 ring-line-soft">
                <dt className="order-2 text-xs leading-tight text-ink-muted">{stat.label}</dt>
                <dd className="order-1 flex items-center gap-1.5 font-mono text-lg font-semibold tabular-nums" suppressHydrationWarning>
                  {stat.icon}
                  {stat.value}
                </dd>
              </div>
            ))}
          </dl>
        ) : null}
      </div>
    </section>
  );
}

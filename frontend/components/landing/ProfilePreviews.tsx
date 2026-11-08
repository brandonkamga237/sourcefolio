import Link from "next/link";
import { connection } from "next/server";
import { getTranslations } from "next-intl/server";
import { Avatar } from "@/components/members/Avatar";
import { RegularityStrip } from "@/components/rankings/RegularityStrip";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { getRankings, type RankingRow } from "@/services/rankings";

/** Published profiles as a short list; nothing at all until there are some. */
export async function ProfilePreviews({ locale }: { locale: Locale }) {
  await connection();
  const t = await getTranslations({ locale, namespace: "landing.profile" });
  let rows: RankingRow[] = [];
  try {
    rows = (await getRankings({ sort: "regularity", country: null, language: null, cursor: null })).items.slice(0, 5);
  } catch (error) {
    console.error("The profile previews could not be loaded", error);
  }
  if (rows.length === 0) return null;

  return (
    <div className="flex flex-col gap-3">
      <ul className="divide-y divide-line-soft border-y border-line-soft">
        {rows.map((row) => (
          <li key={row.login}>
            <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex min-h-16 items-center gap-3 py-3 transition-colors duration-150 hover:bg-surface-raised">
              <Avatar login={row.login} size={36} />
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="truncate text-[15px] font-medium">{row.login}</span>
                <span className="truncate text-[13px] text-ink-muted">{[row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ")}</span>
              </span>
              <span className="hidden sm:block"><RegularityStrip months={row.months} /></span>
              <span className="w-28 shrink-0 text-right text-[13px] text-ink-muted tabular-nums">{t("months", { active: row.activeMonths })}</span>
            </Link>
          </li>
        ))}
      </ul>
      <Link href={localizedPath(locale, "/rankings")} className="link flex min-h-11 w-fit items-center text-[14px]">{t("all")}</Link>
    </div>
  );
}

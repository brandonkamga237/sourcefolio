import Link from "next/link";
import { connection } from "next/server";
import { getFormatter, getTranslations } from "next-intl/server";
import { Avatar } from "@/components/members/Avatar";
import { RegularityStrip } from "@/components/rankings/RegularityStrip";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { getRankings, type RankingRow } from "@/services/rankings";

const EXAMPLE_MONTHS = [0, 1, 3, 0, 2, 5, 4, 0, 6, 8, 3, 7];
const card = "flex flex-col gap-3 rounded-card border border-line bg-surface p-4";

/** Real published profiles; until there is one, a single card plainly labelled as an example. */
export async function ProfilePreviews({ locale }: { locale: Locale }) {
  await connection();
  const [t, format] = await Promise.all([getTranslations({ locale, namespace: "landing.profile" }), getFormatter({ locale })]);
  let rows: RankingRow[] = [];
  try {
    rows = (await getRankings({ sort: "regularity", country: null, language: null, cursor: null })).items.slice(0, 3);
  } catch (error) {
    console.error("The profile previews could not be loaded", error);
  }

  if (rows.length === 0) {
    return (
      <div className={`${card} max-w-sm`}>
        <span className="w-fit rounded-tag border border-line px-2 py-0.5 font-mono text-[12px] text-ink-muted">{t("example")}</span>
        <span className="font-mono text-[15px] font-semibold">{t("exampleLogin")}</span>
        <RegularityStrip months={EXAMPLE_MONTHS} />
        <span className="text-[13px] text-ink-muted">{t("months", { active: 8 })}</span>
      </div>
    );
  }

  return (
    <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {rows.map((row) => (
        <li key={row.login}>
          <Link href={localizedPath(locale, `/u/${row.login}`)} className={`${card} hover:bg-surface-raised`}>
            <span className="flex items-center gap-3">
              <Avatar login={row.login} size={36} />
              <span className="flex min-w-0 flex-col">
                <span className="truncate text-[15px] font-semibold text-accent">{row.login}</span>
                <span className="truncate text-[12px] text-ink-muted">{[row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ")}</span>
              </span>
            </span>
            <RegularityStrip months={row.months} />
            <span className="text-[13px] text-ink-muted">
              {t("months", { active: row.activeMonths })} · {t("contributions", { count: row.publicContributions, formatted: format.number(row.publicContributions) })}
            </span>
          </Link>
        </li>
      ))}
    </ul>
  );
}

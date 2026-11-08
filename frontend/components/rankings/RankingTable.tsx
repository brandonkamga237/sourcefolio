import Link from "next/link";
import { getFormatter, getLocale, getTranslations } from "next-intl/server";
import { Avatar } from "@/components/members/Avatar";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import type { RankingRow, RankingSort } from "@/services/rankings";
import { RegularityStrip } from "./RegularityStrip";

interface RankingTableProps {
  rows: RankingRow[];
  sort: RankingSort;
}

/**
 * A ranking as a table on a wide screen and as a list of cards on a phone. The column sorted on
 * is marked; every row opens the profile, whose figures link to their GitHub proof.
 */
export async function RankingTable({ rows, sort }: RankingTableProps) {
  const [t, format, rawLocale] = await Promise.all([getTranslations("rankings"), getFormatter(), getLocale()]);
  const locale = toLocale(rawLocale);
  const place = (row: RankingRow) => [row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ");
  const year = (row: RankingRow) => new Date(row.accountCreatedAt).getUTCFullYear();
  const sorted = (column: RankingSort) => (column === sort ? "font-semibold text-ink" : "text-ink-soft");

  return (
    <>
      <div className="hidden overflow-hidden rounded-card border border-line lg:block">
        <table className="w-full text-left text-[14px]">
          <thead className="bg-surface-raised text-[13px] text-ink-muted">
            <tr>
              <th scope="col" className="w-12 px-4 py-3 font-semibold">{t("columns.position")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.profile")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.regularity")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.contributions")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.stars")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.external")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.since")}</th>
              <th scope="col" className="px-4 py-3 font-semibold">{t("columns.languages")}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line-soft">
            {rows.map((row) => (
              <tr key={row.login} className="hover:bg-surface-raised">
                <td className="px-4 py-3 text-ink-muted tabular-nums">{row.position}</td>
                <td className="px-4 py-3">
                  <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex items-center gap-3">
                    <Avatar login={row.login} size={32} />
                    <span className="flex min-w-0 flex-col">
                      <span className="truncate font-semibold text-accent">{row.login}</span>
                      <span className="truncate text-[12px] text-ink-muted">{place(row)}</span>
                    </span>
                  </Link>
                </td>
                <td className={`px-4 py-3 ${sorted("regularity")}`}>
                  <span className="flex items-center gap-2">
                    <RegularityStrip months={row.months} />
                    <span className="tabular-nums">{t("monthsActive", { active: row.activeMonths })}</span>
                  </span>
                </td>
                <td className={`px-4 py-3 tabular-nums ${sorted("contributions")}`}>{format.number(row.publicContributions)}</td>
                <td className={`px-4 py-3 tabular-nums ${sorted("stars")}`}>{format.number(row.stars)}</td>
                <td className={`px-4 py-3 tabular-nums ${sorted("external")}`}>{format.number(row.mergedPullRequestsElsewhere)}</td>
                <td className={`px-4 py-3 tabular-nums ${sorted("seniority")}`}>{year(row)}</td>
                <td className="px-4 py-3 text-[13px] text-ink-soft">{row.languages.slice(0, 2).join(" · ")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ol className="flex flex-col gap-2 lg:hidden">
        {rows.map((row) => (
          <li key={row.login}>
            <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex flex-col gap-3 rounded-card border border-line bg-surface p-4">
              <span className="flex items-center gap-3">
                <span className="w-7 shrink-0 text-[14px] text-ink-muted tabular-nums">{row.position}</span>
                <Avatar login={row.login} size={36} />
                <span className="flex min-w-0 flex-col">
                  <span className="truncate text-[15px] font-semibold text-accent">{row.login}</span>
                  <span className="truncate text-[12px] text-ink-muted">{place(row)}</span>
                </span>
              </span>
              <span className="flex items-center gap-2 pl-10 text-[13px]">
                <RegularityStrip months={row.months} />
                <span className={sorted("regularity")}>{t("monthsActive", { active: row.activeMonths })}</span>
              </span>
              <dl className="grid grid-cols-3 gap-2 pl-10 text-[12px]">
                {(
                  [
                    ["contributions", t("columns.contributions"), format.number(row.publicContributions)],
                    ["stars", t("columns.stars"), format.number(row.stars)],
                    ["external", t("columns.external"), format.number(row.mergedPullRequestsElsewhere)],
                  ] as const
                ).map(([key, label, value]) => (
                  <div key={key} className="flex flex-col">
                    <dt className="text-ink-muted">{label}</dt>
                    <dd className={`text-[14px] tabular-nums ${sorted(key)}`}>{value}</dd>
                  </div>
                ))}
              </dl>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}

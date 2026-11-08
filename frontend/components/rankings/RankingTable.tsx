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
 * A ranking as a table on a wide screen; on a phone one line per person, showing only the figure
 * sorted on. Every row opens the profile, whose figures link to their GitHub proof.
 */
export async function RankingTable({ rows, sort }: RankingTableProps) {
  const [t, format, rawLocale] = await Promise.all([getTranslations("rankings"), getFormatter(), getLocale()]);
  const locale = toLocale(rawLocale);
  const place = (row: RankingRow) => [row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ");
  const year = (row: RankingRow) => new Date(row.accountCreatedAt).getUTCFullYear();
  const cell = (column: RankingSort) => `px-3 py-3 tabular-nums ${column === sort ? "font-medium text-ink" : "text-ink-muted"}`;
  const heading = (column: RankingSort | null) => `px-3 py-2.5 font-normal ${column === sort ? "text-ink" : ""}`;
  const sorted: Record<RankingSort, (row: RankingRow) => string> = {
    regularity: (row) => t("monthsActive", { active: row.activeMonths }),
    contributions: (row) => format.number(row.publicContributions),
    stars: (row) => format.number(row.stars),
    external: (row) => format.number(row.mergedPullRequestsElsewhere),
    seniority: (row) => String(year(row)),
  };

  return (
    <>
      <div className="hidden lg:block">
        <table className="w-full text-left text-[14px]">
          <thead className="border-b border-line text-[13px] text-ink-muted">
            <tr>
              <th scope="col" className="w-10 py-2.5 pr-3 font-normal">{t("columns.position")}</th>
              <th scope="col" className={heading(null)}>{t("columns.profile")}</th>
              <th scope="col" className={heading("regularity")}>{t("columns.regularity")}</th>
              <th scope="col" className={heading("contributions")}>{t("columns.contributions")}</th>
              <th scope="col" className={heading("stars")}>{t("columns.stars")}</th>
              <th scope="col" className={heading("external")}>{t("columns.external")}</th>
              <th scope="col" className={heading("seniority")}>{t("columns.since")}</th>
              <th scope="col" className={heading(null)}>{t("columns.languages")}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line-soft">
            {rows.map((row) => (
              <tr key={row.login} className="transition-colors duration-150 hover:bg-surface-raised">
                <td className="py-3 pr-3 text-ink-muted tabular-nums">{row.position}</td>
                <td className="px-3 py-3">
                  <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex items-center gap-3">
                    <Avatar login={row.login} size={32} />
                    <span className="flex min-w-0 flex-col">
                      <span className="truncate font-medium">{row.login}</span>
                      <span className="truncate text-[12px] text-ink-muted">{place(row)}</span>
                    </span>
                  </Link>
                </td>
                <td className={cell("regularity")}>
                  <span className="flex items-center gap-2 whitespace-nowrap">
                    <RegularityStrip months={row.months} />
                    {t("monthsActive", { active: row.activeMonths })}
                  </span>
                </td>
                <td className={cell("contributions")}>{format.number(row.publicContributions)}</td>
                <td className={cell("stars")}>{format.number(row.stars)}</td>
                <td className={cell("external")}>{format.number(row.mergedPullRequestsElsewhere)}</td>
                <td className={cell("seniority")}>{year(row)}</td>
                <td className="px-3 py-3 text-[13px] text-ink-muted">{row.languages.slice(0, 2).join(" · ")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ol className="divide-y divide-line-soft border-y border-line-soft lg:hidden">
        {rows.map((row) => (
          <li key={row.login}>
            <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex min-h-16 items-center gap-3 py-3">
              <span className="w-6 shrink-0 text-[13px] text-ink-muted tabular-nums">{row.position}</span>
              <Avatar login={row.login} size={36} />
              <span className="flex min-w-0 flex-1 flex-col gap-1">
                <span className="truncate text-[15px] font-medium">{row.login}</span>
                <RegularityStrip months={row.months} />
              </span>
              <span className="shrink-0 text-right text-[14px] font-medium tabular-nums">{sorted[sort](row)}</span>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}

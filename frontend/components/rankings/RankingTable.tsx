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
  /** The first page opens on its top three, as cards. */
  podium: boolean;
}

/**
 * A ranking as a table on a wide screen; on a phone one line per person, showing only the figure
 * sorted on. Every row opens the profile, whose figures link to their GitHub proof.
 */
export async function RankingTable({ rows: all, sort, podium }: RankingTableProps) {
  const [t, format, rawLocale] = await Promise.all([getTranslations("rankings"), getFormatter(), getLocale()]);
  const locale = toLocale(rawLocale);
  const place = (row: RankingRow) => [row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ");
  const year = (row: RankingRow) => new Date(row.accountCreatedAt).getUTCFullYear();
  const top = podium && all.length >= 3 ? all.slice(0, 3) : [];
  const rows = all.slice(top.length);
  const cell = (column: RankingSort) => `px-3 py-5 tabular-nums ${column === sort ? "font-medium text-ink" : "text-ink-muted"}`;
  const heading = (column: RankingSort | null) => `px-3 py-3 font-normal ${column === sort ? "text-ink" : ""}`;
  const sorted: Record<RankingSort, (row: RankingRow) => string> = {
    regularity: (row) => t("monthsActive", { active: row.activeMonths }),
    contributions: (row) => format.number(row.publicContributions),
    stars: (row) => format.number(row.stars),
    external: (row) => format.number(row.mergedPullRequestsElsewhere),
    seniority: (row) => String(year(row)),
  };

  return (
    <>
      {top.length > 0 ? (
        <ol data-stagger="" className="grid gap-4 lg:grid-cols-3">
          {top.map((row) => (
            <li key={row.login}>
              <Link href={localizedPath(locale, `/u/${row.login}`)} className="glow-card flex h-full flex-col gap-5 p-7">
                <span className="flex items-center justify-between">
                  <Avatar login={row.login} size={64} />
                  <span className="text-[56px] leading-none font-semibold tracking-[-0.04em] text-ink-muted/40 tabular-nums">{row.position}</span>
                </span>
                <span className="flex min-w-0 flex-col gap-1">
                  <span className="truncate text-[24px] font-semibold tracking-[-0.02em]">{row.login}</span>
                  <span className="truncate text-[16px] text-ink-muted">{place(row)}</span>
                </span>
                <RegularityStrip months={row.months} />
                <span className="text-[32px] leading-none font-semibold tracking-[-0.03em] tabular-nums">{sorted[sort](row)}</span>
              </Link>
            </li>
          ))}
        </ol>
      ) : null}

      <div className="hidden lg:block">
        <table className="w-full text-left text-[16px]">
          <thead className="border-b border-line text-[15px] text-ink-muted">
            <tr>
              <th scope="col" className="w-12 py-3 pr-3 font-normal">{t("columns.position")}</th>
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
                <td className="py-5 pr-3 text-ink-muted tabular-nums">{row.position}</td>
                <td className="px-3 py-5">
                  <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex items-center gap-3">
                    <Avatar login={row.login} size={44} />
                    <span className="flex min-w-0 flex-col">
                      <span className="truncate text-[17px] font-semibold">{row.login}</span>
                      <span className="truncate text-[14px] text-ink-muted">{place(row)}</span>
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
                <td className="px-3 py-5 text-[15px] text-ink-muted">{row.languages.slice(0, 2).join(" · ")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ol className="divide-y divide-line-soft border-y border-line-soft lg:hidden">
        {rows.map((row) => (
          <li key={row.login}>
            <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex min-h-20 items-center gap-3 py-4">
              <span className="w-7 shrink-0 text-[16px] text-ink-muted tabular-nums">{row.position}</span>
              <Avatar login={row.login} size={44} />
              <span className="flex min-w-0 flex-1 flex-col gap-1">
                <span className="truncate text-[18px] font-semibold">{row.login}</span>
                <RegularityStrip months={row.months} />
              </span>
              <span className="shrink-0 text-right text-[17px] font-semibold tabular-nums">{sorted[sort](row)}</span>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}

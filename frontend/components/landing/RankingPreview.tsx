"use client";

import Link from "next/link";
import { useState } from "react";
import { RegularityStrip } from "@/components/rankings/RegularityStrip";
import { Tabs } from "@/components/ui/Tabs";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import type { RankingSort } from "@/services/rankings";
import { ExampleTag } from "./ExampleTag";
import { PersonMark } from "./PersonMark";

export interface PreviewRow {
  login: string;
  name: string | null;
  place: string;
  months: number[];
  value: Record<RankingSort, string>;
  order: Record<RankingSort, number>;
}

interface RankingPreviewProps {
  locale: Locale;
  rows: PreviewRow[];
  example: boolean;
  labels: { tabs: string; example: string; all: string; sort: Record<RankingSort, string>; measures: Record<RankingSort, string> };
}

const SORTS: RankingSort[] = ["regularity", "contributions", "stars", "external", "seniority"];

/** One criterion at a time, and the sentence saying what it measures. */
export function RankingPreview({ locale, rows, example, labels }: RankingPreviewProps) {
  const [sort, setSort] = useState<RankingSort>("regularity");
  const sorted = [...rows].sort((a, b) => b.order[sort] - a.order[sort]).slice(0, 5);
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-end gap-3">
        <div className="min-w-0 flex-1"><Tabs label={labels.tabs} tabs={SORTS.map((key) => ({ key, label: labels.sort[key] }))} current={sort} onChange={(key) => setSort(key as RankingSort)} /></div>
        {example ? <div className="pb-2"><ExampleTag label={labels.example} /></div> : null}
      </div>
      <p className="text-[16px] text-ink-muted">{labels.measures[sort]}</p>
      <div aria-hidden="true" className="hidden grid-cols-[minmax(0,2.4fr)_minmax(0,1.3fr)_repeat(4,minmax(0,1fr))] gap-3 text-[14px] text-ink-muted lg:grid">
        <span />
        {SORTS.map((key) => <span key={key} className={`text-right ${key === sort ? "font-semibold text-ink" : ""}`}>{labels.sort[key]}</span>)}
      </div>
      <ol key={sort} className="divide-y divide-line border-y border-line animate-[toast-in_var(--dur-3)_var(--ease-enter)]">
        {sorted.map((row) => {
          const body = (
            <>
              <span className="flex min-w-0 items-center gap-3 max-lg:flex-1">
                <PersonMark login={row.login} name={row.name} size={40} example={example} />
                <span className="flex min-w-0 flex-col">
                  <span className="truncate text-[16px] font-semibold">{row.name ?? row.login}</span>
                  <span className="truncate text-[14px] text-ink-muted">{row.place}</span>
                </span>
              </span>
              <span className="hidden justify-end lg:flex"><RegularityStrip months={row.months} /></span>
              {SORTS.slice(1).map((key) => <span key={key} className={`hidden text-right font-mono text-[16px] tabular-nums lg:block ${key === sort ? "font-semibold" : "text-ink-soft"}`}>{row.value[key]}</span>)}
              <span className="shrink-0 text-right font-mono text-[16px] font-semibold tabular-nums lg:hidden">{row.value[sort]}</span>
            </>
          );
          const line = "flex min-h-16 items-center gap-3 py-2 lg:grid lg:min-h-[72px] lg:grid-cols-[minmax(0,2.4fr)_minmax(0,1.3fr)_repeat(4,minmax(0,1fr))]";
          return <li key={row.login}>{example ? <div className={line}>{body}</div> : <Link href={localizedPath(locale, `/u/${row.login}`)} className={`${line} transition-colors duration-(--dur-1) hover:bg-surface-raised`}>{body}</Link>}</li>;
        })}
      </ol>
      <Link href={localizedPath(locale, "/rankings")} className="flex min-h-11 w-fit items-center text-[14px] font-semibold text-accent">{labels.all}</Link>
    </div>
  );
}

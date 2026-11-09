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
      <p className="text-[14px] text-ink-muted">{labels.measures[sort]}</p>
      <ol key={sort} className="divide-y divide-line border-y border-line animate-[toast-in_var(--dur-3)_var(--ease-enter)]">
        {sorted.map((row) => {
          const body = (
            <>
              <PersonMark login={row.login} name={row.name} size={36} example={example} />
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="truncate text-[15px] font-semibold">{row.name ?? row.login}</span>
                <span className="truncate text-[13px] text-ink-muted">{row.place}</span>
              </span>
              <span className="hidden sm:block"><RegularityStrip months={row.months} /></span>
              <span className="w-24 shrink-0 text-right font-mono text-[15px] font-semibold tabular-nums">{row.value[sort]}</span>
            </>
          );
          return <li key={row.login}>{example ? <div className="flex min-h-16 items-center gap-3 py-2">{body}</div> : <Link href={localizedPath(locale, `/u/${row.login}`)} className="flex min-h-16 items-center gap-3 py-2 transition-colors duration-(--dur-1) hover:bg-surface-raised">{body}</Link>}</li>;
        })}
      </ol>
      <Link href={localizedPath(locale, "/rankings")} className="flex min-h-11 w-fit items-center text-[14px] font-semibold text-accent">{labels.all}</Link>
    </div>
  );
}

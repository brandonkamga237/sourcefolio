"use client";

import { useState } from "react";
import { CalendarArt } from "@/components/contribution/CalendarArt";
import { IconExternal, IconLock, IconMerge, IconStar } from "@/components/icons";
import type { Locale } from "@/i18n/routing";
import { indicatorViews, type IndicatorKey, type IndicatorLabels } from "@/lib/indicators";
import type { ProfileCard } from "@/services/contribution-types";
import { ExampleTag } from "./ExampleTag";

interface AnatomyProps {
  locale: Locale;
  card: ProfileCard;
  example: boolean;
  labels: { indicators: IndicatorLabels; tablist: string; example: string; merged: string; joined: string };
}

function Proof({ active, card, labels, locale }: { active: IndicatorKey; card: ProfileCard; labels: AnatomyProps["labels"]; locale: Locale }) {
  const { indicators } = card;
  if (active === "regularity" || active === "volume") return <CalendarArt calendar={indicators.calendar} />;
  if (active === "stars") {
    return (
      <ul className="divide-y divide-line-soft">
        {indicators.topRepositories.map((repository) => (
          <li key={repository.name} className="flex items-center justify-between gap-3 py-2.5 text-[14px]">
            <span className="truncate font-semibold text-accent">{repository.name}</span>
            <span className="flex items-center gap-1 tabular-nums text-ink-muted"><IconStar aria-hidden="true" className="size-3.5 text-star" />{repository.stars}</span>
          </li>
        ))}
      </ul>
    );
  }
  if (active === "external") {
    return (
      <ul className="divide-y divide-line-soft">
        {indicators.externalProjects.map((project) => (
          <li key={project.repository} className="flex items-center gap-2.5 py-2.5 text-[14px]">
            <IconMerge aria-hidden="true" className="size-4 shrink-0 text-success" />
            <span className="min-w-0 flex-1 truncate font-mono text-[13px]">{project.repository}</span>
            <span className="text-ink-muted tabular-nums">{project.mergedPullRequests} {labels.merged}</span>
          </li>
        ))}
      </ul>
    );
  }
  if (active === "languages") {
    const total = indicators.languages.reduce((sum, language) => sum + language.bytes, 0) || 1;
    return (
      <div className="flex flex-col gap-3">
        <div className="flex h-2 gap-0.5 overflow-hidden rounded-full">{indicators.languages.map((language) => <span key={language.name} style={{ width: `${(language.bytes / total) * 100}%`, backgroundColor: language.color ?? "var(--color-ink-muted)" }} />)}</div>
        <ul className="flex flex-wrap gap-x-4 gap-y-1 text-[14px]">{indicators.languages.map((language) => <li key={language.name}>{language.name} <span className="text-ink-muted tabular-nums">{Math.round((language.bytes / total) * 100)}%</span></li>)}</ul>
      </div>
    );
  }
  return <p className="text-[15px]">{labels.joined} <time className="font-semibold">{new Intl.DateTimeFormat(locale, { month: "long", year: "numeric", timeZone: "UTC" }).format(new Date(indicators.accountCreatedAt))}</time></p>;
}

/** Choosing a figure opens the GitHub page it comes from, in miniature. */
export function Anatomy({ locale, card, example, labels }: AnatomyProps) {
  const views = indicatorViews(card, labels.indicators);
  const [active, setActive] = useState<IndicatorKey>("regularity");
  const current = views.find((view) => view.key === active) ?? views[0];

  function move(offset: number) {
    const index = views.findIndex((view) => view.key === active);
    setActive(views[(index + offset + views.length) % views.length].key);
  }

  return (
    <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
      <div role="tablist" aria-orientation="vertical" aria-label={labels.tablist} onKeyDown={(event) => { if (event.key === "ArrowDown") { event.preventDefault(); move(1); } if (event.key === "ArrowUp") { event.preventDefault(); move(-1); } }} className="grid grid-cols-2 gap-2">
        {views.map((view) => {
          const selected = view.key === active;
          return (
            <button key={view.key} type="button" role="tab" aria-selected={selected} tabIndex={selected ? 0 : -1} onClick={() => setActive(view.key)} onPointerEnter={(event) => event.pointerType === "mouse" && setActive(view.key)} className={`spotlight flex min-h-20 flex-col justify-between gap-2 rounded-card border p-3 text-left transition-colors duration-(--dur-2) ${selected ? "border-accent bg-surface" : "border-line bg-surface-raised"}`}>
              <span className="text-[13px] text-ink-muted">{view.label}</span>
              <span className="font-mono text-[22px] leading-none font-semibold tabular-nums">{view.count === undefined ? view.text : `${view.count}${view.suffix ?? ""}`}</span>
            </button>
          );
        })}
      </div>
      <div role="tabpanel" aria-live="polite" className="overflow-hidden rounded-panel border border-line bg-surface shadow-[var(--shadow-float)]">
        <div className="flex items-center gap-2 border-b border-line bg-surface-raised px-4 py-2.5">
          <IconLock aria-hidden="true" className="size-3.5 text-ink-muted" />
          <span className="min-w-0 flex-1 truncate font-mono text-[12px] text-ink-soft">{current.proofLabel}</span>
          {example ? <ExampleTag label={labels.example} /> : current.proof ? <a href={current.proof} target="_blank" rel="noopener noreferrer" aria-label={current.proofLabel} className="flex size-11 items-center justify-center text-accent lg:size-6"><IconExternal aria-hidden="true" className="size-3.5" /></a> : null}
        </div>
        <div key={active} className="min-h-40 p-5 animate-[toast-in_var(--dur-3)_var(--ease-enter)]"><Proof active={active} card={card} labels={labels} locale={locale} /></div>
      </div>
    </div>
  );
}

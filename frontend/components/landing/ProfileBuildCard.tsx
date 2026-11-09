"use client";

import { useEffect, useRef, useState } from "react";
import { IconExternal } from "@/components/icons";
import type { Locale } from "@/i18n/routing";
import { weeksOf } from "@/lib/calendar";
import { countryName } from "@/lib/countries";
import { indicatorViews, type IndicatorLabels } from "@/lib/indicators";
import type { ProfileCard } from "@/services/contribution-types";
import { ExampleTag } from "./ExampleTag";
import { PersonMark } from "./PersonMark";

interface ProfileBuildCardProps {
  locale: Locale;
  card: ProfileCard;
  example: boolean;
  /** Changing it builds the card again. */
  run?: number;
  labels: { indicators: IndicatorLabels; example: string };
  sectorName?: string | null;
}

const LEVEL = ["fill-level-0", "fill-level-1", "fill-level-2", "fill-level-3", "fill-level-4"] as const;
const IDENTITY = 1;
const CALENDAR = 2;
const FIRST_INDICATOR = 3;
const PROOFS = FIRST_INDICATOR + 6;
const part = "transition-[opacity,transform] duration-(--dur-3) ease-enter";

function Count({ value, on, suffix, locale }: { value: number; on: boolean; suffix?: string; locale: Locale }) {
  const node = useRef<HTMLSpanElement>(null);
  useEffect(() => {
    const element = node.current;
    if (!element || !on || window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const format = new Intl.NumberFormat(locale);
    const start = performance.now();
    let frame = 0;
    const tick = (time: number) => {
      const progress = Math.min(1, (time - start) / 500);
      element.textContent = `${format.format(Math.round(value * (1 - (1 - progress) ** 3)))}${suffix ?? ""}`;
      if (progress < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [on, value, suffix, locale]);
  return <span ref={node}>{new Intl.NumberFormat(locale).format(value)}{suffix}</span>;
}

/** A profile assembling itself from its public data: identity, year, figures, then their proofs. */
export function ProfileBuildCard({ locale, card, example, run = 0, labels, sectorName }: ProfileBuildCardProps) {
  const [stage, setStage] = useState(0);

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      const timer = window.setTimeout(() => setStage(PROOFS), 0);
      return () => window.clearTimeout(timer);
    }
    const reset = window.setTimeout(() => setStage(0), 0);
    const steps = [[IDENTITY, 180], [CALENDAR, 520], ...Array.from({ length: 6 }, (_, index) => [FIRST_INDICATOR + index, 1300 + index * 190]), [PROOFS, 2600]];
    const timers = steps.map(([value, delay]) => window.setTimeout(() => setStage(value), delay));
    return () => [reset, ...timers].forEach((timer) => window.clearTimeout(timer));
  }, [run]);

  const weeks = weeksOf(card.indicators.calendar);
  const indicators = indicatorViews(card, labels.indicators);
  const place = [card.city, countryName(card.country, locale)].filter(Boolean).join(" · ");
  const seen = (threshold: number) => (stage >= threshold ? "opacity-100" : "translate-y-1 opacity-0");

  return (
    <article data-build="" data-ready={stage > 0 ? "" : undefined} className="rounded-panel border border-line bg-surface p-5 shadow-[var(--shadow-float)] lg:p-6">
      <div data-part="" className={`flex items-center gap-3 ${part} ${seen(IDENTITY)}`}>
        <PersonMark login={card.login} name={card.name} size={48} example={example} />
        <div className="flex min-w-0 flex-1 flex-col">
          <span className="truncate font-display text-[20px] font-semibold">{card.name ?? card.login}</span>
          <span className="truncate text-[14px] text-ink-muted"><span className="font-mono">{card.login}</span> · {[place, sectorName].filter(Boolean).join(" · ")}</span>
        </div>
        {example ? <ExampleTag label={labels.example} /> : null}
      </div>

      <svg aria-hidden="true" viewBox={`0 0 ${weeks.length * 13} 91`} className="mt-5 h-auto w-full">
        {weeks.map((week, column) => (
          <g key={column} style={{ transitionDelay: stage >= CALENDAR ? `${column * 12}ms` : "0ms" }} className={`transition-opacity duration-(--dur-2) ${stage >= CALENDAR ? "opacity-100" : "opacity-20"}`} data-part="">
            {week.map((day, row) => (day ? <rect key={row} x={column * 13 + 1} y={row * 13 + 1} width="11" height="11" rx="2" className={stage >= CALENDAR ? LEVEL[day.level] : LEVEL[0]} /> : null))}
          </g>
        ))}
      </svg>

      <dl className="mt-5 grid grid-cols-2 gap-x-4 gap-y-4 sm:grid-cols-3">
        {indicators.map((indicator, index) => {
          const on = stage >= FIRST_INDICATOR + index;
          return (
            <div key={indicator.key} data-part="" className={`flex min-w-0 flex-col gap-0.5 ${part} ${on ? "opacity-100" : "translate-y-1 opacity-0"}`}>
              <dt className="text-[13px] text-ink-muted">{indicator.label}</dt>
              <dd className="font-mono text-[24px] leading-tight font-semibold tabular-nums">
                {indicator.count === undefined ? indicator.text : <Count value={indicator.count} on={on} suffix={indicator.suffix} locale={locale} />}
              </dd>
              <dd data-part="" className={`${part} ${seen(PROOFS)}`}>
                {indicator.proof ? (
                  <a href={indicator.proof} target="_blank" rel="noopener noreferrer" className="proof inline-flex max-w-full items-center gap-1">
                    <span className="truncate">{indicator.proofLabel}</span>
                    <IconExternal aria-hidden="true" className="size-3 shrink-0" />
                  </a>
                ) : (
                  <span className="block truncate font-mono text-[12px] text-ink-muted">{indicator.proofLabel}</span>
                )}
              </dd>
            </div>
          );
        })}
      </dl>
    </article>
  );
}

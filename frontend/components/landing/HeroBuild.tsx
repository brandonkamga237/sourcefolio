"use client";

import { useState, type ReactNode } from "react";
import type { Locale } from "@/i18n/routing";
import type { IndicatorLabels } from "@/lib/indicators";
import type { ProfileCard } from "@/services/contribution-types";
import { HandleField, type HandleLabels } from "./HandleField";
import { ProfileBuildCard } from "./ProfileBuildCard";

interface HeroBuildProps {
  locale: Locale;
  intro: ReactNode;
  initial: ProfileCard;
  example: boolean;
  labels: { handle: HandleLabels; indicators: IndicatorLabels; example: string };
}

/** The field and the card it drives. */
export function HeroBuild({ locale, intro, initial, example, labels }: HeroBuildProps) {
  const [card, setCard] = useState(initial);
  const [isExample, setIsExample] = useState(example);
  const [run, setRun] = useState(0);

  function build(next: ProfileCard, nextExample: boolean) {
    setCard(next);
    setIsExample(nextExample);
    setRun((value) => value + 1);
  }

  return (
    <section className="wrap grid items-center gap-10 pt-8 pb-14 lg:grid-cols-[minmax(0,1fr)_minmax(0,600px)] lg:gap-16 lg:py-20">
      <div className="flex min-w-0 flex-col gap-6">
        {intro}
        <HandleField locale={locale} labels={labels.handle} initial={initial.login} onFound={(found) => build(found, false)} onExample={() => build(initial, example)} />
      </div>
      <ProfileBuildCard locale={locale} card={card} example={isExample} run={run} labels={{ indicators: labels.indicators, example: labels.example }} />
    </section>
  );
}

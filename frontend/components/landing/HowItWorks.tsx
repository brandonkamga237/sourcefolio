"use client";

import { useEffect, useRef, useState } from "react";
import { IconCheck, IconMerge } from "@/components/icons";

interface HowItWorksProps {
  steps: readonly { title: string; body: string }[];
  merged: string;
  published: string;
}

const LIT = [1, 6, 26, 60];
const SHADES = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;

/** The step in the middle of the screen is the current one: progress belongs to the scroll. */
export function HowItWorks({ steps, merged, published }: HowItWorksProps) {
  const [active, setActive] = useState(0);
  const items = useRef<(HTMLLIElement | null)[]>([]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) if (entry.isIntersecting) setActive(Number((entry.target as HTMLElement).dataset.step));
      },
      { rootMargin: "-45% 0px -45% 0px" },
    );
    items.current.forEach((item) => item && observer.observe(item));
    return () => observer.disconnect();
  }, []);

  return (
    <div className="grid gap-8 lg:grid-cols-2 lg:gap-14">
      <div aria-hidden="true" className="sticky top-16 z-10 self-start rounded-panel border border-line bg-surface p-4 lg:top-24 lg:p-8">
        <div className="grid grid-cols-[repeat(15,minmax(0,1fr))] gap-1 lg:gap-1.5">
          {Array.from({ length: 75 }, (_, index) => {
            const on = index < LIT[active];
            return <span key={index} style={{ transitionDelay: `${index * 5}ms` }} className={`aspect-square rounded-[3px] transition-colors duration-(--dur-3) ${on ? SHADES[(index * 7) % 4] : "bg-level-0"}`} />;
          })}
        </div>
        <div className="mt-4 flex min-h-7 items-center gap-2 text-[14px] font-semibold">
          {active >= 2 ? <span className="flex items-center gap-1.5 rounded-full bg-accent-soft px-2.5 py-1 text-accent"><IconMerge className="size-3.5" />{merged}</span> : null}
          {active >= 3 ? <span className="flex items-center gap-1.5 rounded-full border border-success px-2.5 py-1 text-success"><IconCheck className="size-3.5" />{published}</span> : null}
        </div>
      </div>
      <ol className="relative flex flex-col gap-[8vh] py-[3vh] pl-10 lg:gap-[12vh] lg:py-[6vh]">
        <span aria-hidden="true" className="absolute top-0 bottom-0 left-[9px] w-0.5 bg-line" />
        <span aria-hidden="true" style={{ transform: `scaleY(${(active + 1) / steps.length})` }} className="absolute top-0 bottom-0 left-[9px] w-0.5 origin-top bg-level-3 transition-transform duration-(--dur-4) ease-enter" />
        {steps.map((step, index) => (
          <li key={step.title} ref={(node) => { items.current[index] = node; }} data-step={index} aria-current={index === active ? "step" : undefined} className="relative">
            <span aria-hidden="true" className={`absolute top-1.5 -left-10 size-5 rounded-[4px] border-2 border-canvas transition-colors duration-(--dur-3) ${index <= active ? "bg-level-3" : "bg-level-0"}`} />
            <p className={`t-h3 transition-colors duration-(--dur-3) ${index === active ? "text-ink" : "text-ink-muted"}`}>{step.title}</p>
            <p className="mt-1.5 text-[16px] leading-relaxed text-ink-soft lg:text-[18px]">{step.body}</p>
          </li>
        ))}
      </ol>
    </div>
  );
}

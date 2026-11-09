"use client";

import { useState } from "react";
import { ExampleTag } from "./ExampleTag";

interface ProofBandProps {
  projects: readonly string[];
  example: boolean;
  labels: { title: string; pause: string; play: string; example: string };
}

/** Slow, and stoppable: motion that a visitor cannot pause is motion they cannot read. */
export function ProofBand({ projects, example, labels }: ProofBandProps) {
  const [paused, setPaused] = useState(false);
  // A short list is repeated so the band never shows a gap while it travels.
  const filled = Array.from({ length: Math.ceil(10 / Math.max(1, projects.length)) }, () => projects).flat();
  const list = (hidden: boolean) => (
    <ul aria-hidden={hidden || undefined} className="flex shrink-0 items-center gap-8 pr-8">
      {filled.map((project, index) => <li key={index} className="font-mono text-[14px] whitespace-nowrap text-ink-soft">{project}</li>)}
    </ul>
  );
  return (
    <section aria-label={labels.title} className="border-y border-line bg-surface-raised">
      <div className="wrap flex min-h-14 items-center gap-4">
        <p className="shrink-0 text-[13px] text-ink-muted max-sm:sr-only">{labels.title}</p>
        {example ? <ExampleTag label={labels.example} /> : null}
        <div className="group relative min-w-0 flex-1 overflow-hidden [mask-image:linear-gradient(90deg,transparent,black_6%,black_94%,transparent)]">
          <div className="flex w-max motion-reduce:!animate-none group-hover:[animation-play-state:paused]" style={{ animation: "marquee 48s linear infinite", animationPlayState: paused ? "paused" : "running" }}>
            {list(false)}
            {list(true)}
          </div>
        </div>
        <button type="button" onClick={() => setPaused((value) => !value)} aria-pressed={paused} aria-label={paused ? labels.play : labels.pause} className="flex size-11 shrink-0 items-center justify-center text-ink-muted hover:text-ink motion-reduce:hidden">
          <span aria-hidden="true" className="flex gap-[3px]">
            {paused ? <span className="size-0 border-y-[5px] border-l-[8px] border-y-transparent border-l-current" /> : <><span className="h-2.5 w-[3px] bg-current" /><span className="h-2.5 w-[3px] bg-current" /></>}
          </span>
        </button>
      </div>
    </section>
  );
}

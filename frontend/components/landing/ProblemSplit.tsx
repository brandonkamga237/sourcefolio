import { CalendarArt } from "@/components/contribution/CalendarArt";
import { IconCheck, IconClose } from "@/components/icons";
import type { ProfileCard } from "@/services/contribution-types";
import { SectionHead } from "./SectionHead";

interface ProblemSplitProps {
  card: ProfileCard;
  labels: { title: string; lead: string; cv: string; profile: string; claims: string[]; facts: string[] };
}

const LINES = [92, 78, 86, 60, 88, 72];

/** A claim you must believe beside a record you can check. Revealed by the scroll itself. */
export function ProblemSplit({ card, labels }: ProblemSplitProps) {
  return (
    <section className="wrap flex flex-col gap-8 py-14 lg:gap-10 lg:py-20">
      <SectionHead title={labels.title} lead={labels.lead} />
      <div className="grid gap-4 md:grid-cols-2">
        <figure className="reveal-cv flex flex-col gap-5 rounded-panel border border-line bg-surface-raised p-6 lg:p-8">
          <figcaption className="t-h3">{labels.cv}</figcaption>
          <div aria-hidden="true" className="flex flex-col gap-3 blur-[3px]">
            <span className="h-4 w-1/2 rounded bg-ink-muted/50" />
            {LINES.map((width) => <span key={width} className="h-2.5 rounded bg-line" style={{ width: `${width}%` }} />)}
          </div>
          <ul className="flex flex-col gap-2 text-[16px] text-ink-soft">
            {labels.claims.map((claim) => <li key={claim} className="flex items-center gap-2.5"><IconClose aria-hidden="true" className="size-4 shrink-0 text-ink-muted" />{claim}</li>)}
          </ul>
        </figure>
        <figure className="reveal-profile flex flex-col gap-5 rounded-panel border border-line bg-surface p-6 lg:p-8">
          <figcaption className="t-h3">{labels.profile}</figcaption>
          <CalendarArt calendar={card.indicators.calendar} />
          <ul className="flex flex-col gap-2 text-[16px] text-ink-soft">
            {labels.facts.map((fact) => <li key={fact} className="flex items-center gap-2.5"><IconCheck aria-hidden="true" className="size-4 shrink-0 text-success" />{fact}</li>)}
          </ul>
        </figure>
      </div>
    </section>
  );
}

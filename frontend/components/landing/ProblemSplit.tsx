import { CalendarArt } from "@/components/contribution/CalendarArt";
import type { ProfileCard } from "@/services/contribution-types";

interface ProblemSplitProps {
  card: ProfileCard;
  labels: { title: string; cv: string; profile: string };
}

const LINES = [92, 78, 86, 60, 88, 72, 94, 55, 80, 66];

/** A claim you must believe beside a record you can check. Revealed by the scroll itself. */
export function ProblemSplit({ card, labels }: ProblemSplitProps) {
  return (
    <section className="wrap flex flex-col gap-10 py-16 lg:gap-14 lg:py-28">
      <h2 className="t-h2">{labels.title}</h2>
      <div className="grid gap-4 md:grid-cols-2">
        <figure className="reveal-cv flex flex-col gap-4 rounded-panel border border-line bg-surface-raised p-6">
          <figcaption className="text-[14px] text-ink-muted">{labels.cv}</figcaption>
          <div aria-hidden="true" className="flex flex-col gap-3 blur-[3px]">
            <span className="h-4 w-1/2 rounded bg-ink-muted/50" />
            {LINES.map((width) => <span key={width} className="h-2 rounded bg-line" style={{ width: `${width}%` }} />)}
          </div>
        </figure>
        <figure className="reveal-profile flex flex-col justify-between gap-4 rounded-panel border border-line bg-surface p-6">
          <figcaption className="text-[14px] font-medium">{labels.profile}</figcaption>
          <CalendarArt calendar={card.indicators.calendar} />
        </figure>
      </div>
    </section>
  );
}

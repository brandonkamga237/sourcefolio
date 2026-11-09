import { weeksOf, type CalendarCell } from "@/lib/calendar";

const UNIT = 13;
const LEVEL = ["fill-level-0 stroke-level-0", "fill-level-1 stroke-level-1", "fill-level-2 stroke-level-2", "fill-level-3 stroke-level-3", "fill-level-4 stroke-level-4"] as const;

/** A year of squares that fills its width: one path per shade instead of 365 elements. */
export function CalendarArt({ calendar }: { calendar: readonly CalendarCell[] }) {
  const weeks = weeksOf(calendar);
  const paths: string[] = ["", "", "", "", ""];
  weeks.forEach((week, column) =>
    week.forEach((day, row) => {
      if (day) paths[day.level] += `M${column * UNIT + 1} ${row * UNIT + 1}h9v9h-9z`;
    }),
  );
  return (
    <svg aria-hidden="true" viewBox={`0 0 ${weeks.length * UNIT} ${7 * UNIT}`} className="h-auto w-full">
      {paths.map((d, level) => (d ? <path key={level} d={d} strokeWidth={2} strokeLinejoin="round" className={LEVEL[level]} /> : null))}
    </svg>
  );
}

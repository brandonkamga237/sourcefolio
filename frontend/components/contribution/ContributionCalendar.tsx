import { getFormatter, getTranslations } from "next-intl/server";
import { weeksOf } from "@/lib/calendar";
import { CalendarArt } from "./CalendarArt";
import type { ContributionDay } from "@/services/contribution-types";

interface ContributionCalendarProps {
  calendar: ContributionDay[];
  total: number;
  proofUrl: string;
}

const LEVEL = ["bg-level-0", "bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;

/**
 * The contribution calendar as GitHub draws it: one square a day, five shades. On a phone it
 * scrolls sideways and opens on the latest weeks.
 */
export async function ContributionCalendar({ calendar, total, proofUrl }: ContributionCalendarProps) {
  const [t, format] = await Promise.all([getTranslations("profileCard"), getFormatter()]);
  const weeks = weeksOf(calendar);
  const months = weeks.map((week, index) => {
    const first = week.find((day) => day !== null);
    if (!first) return null;
    const previous = weeks[index - 1]?.find((day) => day !== null);
    const month = first.date.slice(0, 7);
    // A month starting in the last two weeks has no room for its name before the edge.
    return index < weeks.length - 2 && (!previous || previous.date.slice(0, 7) !== month)
      ? format.dateTime(new Date(`${first.date}T00:00:00Z`), { month: "short", timeZone: "UTC" })
      : null;
  });

  return (
    <section aria-labelledby="calendar-title" className="flex flex-col gap-4 rounded-panel border border-line bg-surface p-5 lg:p-6">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 id="calendar-title" className="t-h3">{t("calendarTitle", { count: total })}</h2>
        <a href={proofUrl} target="_blank" rel="noopener noreferrer" className="text-[14px] text-accent hover:underline">
          {t("viewOnGitHub")}
        </a>
      </div>
      {/* direction rtl on the scroller opens it on its right end, the latest weeks */}
      <div className="overflow-x-auto [direction:rtl]">
        <div className="flex min-w-[720px] flex-col gap-1.5 [direction:ltr]" aria-hidden="true">
          <div className="grid text-[13px] text-ink-muted" style={{ gridTemplateColumns: `repeat(${weeks.length}, minmax(0, 1fr))` }}>
            {months.map((label, index) => <span key={index} className="h-5 overflow-visible whitespace-nowrap">{label}</span>)}
          </div>
          <CalendarArt calendar={calendar} />
        </div>
      </div>
      <div className="flex flex-wrap items-center justify-between gap-2 text-[12px] text-ink-muted">
        <span>{total === 0 ? t("calendarEmpty") : null}</span>
        <span className="flex items-center gap-1">
          {t("less")}
          {LEVEL.map((shade) => (
            <span key={shade} aria-hidden="true" className={`size-[11px] rounded-[2px] ${shade}`} />
          ))}
          {t("more")}
        </span>
      </div>
    </section>
  );
}

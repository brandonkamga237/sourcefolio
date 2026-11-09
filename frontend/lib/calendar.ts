export interface CalendarCell {
  date: string;
  level: 0 | 1 | 2 | 3 | 4;
}

// GitHub's weeks start on Sunday; the first one is often partial, padded with empty cells so
// every column is a week.
export function weeksOf<T extends { date: string }>(calendar: readonly T[]): (T | null)[][] {
  if (calendar.length === 0) return [];
  const padding = new Date(`${calendar[0].date}T00:00:00Z`).getUTCDay();
  const cells: (T | null)[] = [...Array<null>(padding).fill(null), ...calendar];
  const weeks: (T | null)[][] = [];
  for (let start = 0; start < cells.length; start += 7) weeks.push(cells.slice(start, start + 7));
  return weeks;
}

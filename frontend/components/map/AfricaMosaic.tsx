import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { landSquares, levelOf, MAP_COLUMNS, MAP_ROWS, type MapLevel, type PlacedCity } from "@/lib/map";

interface AfricaMosaicProps {
  locale: Locale;
  cities: readonly PlacedCity[];
  /** What the picture shows, for screen readers. */
  label: string;
}

const UNIT = 10;
const LEVEL_CLASS: Record<0 | MapLevel, string> = {
  0: "fill-level-0 stroke-level-0",
  1: "fill-level-1 stroke-level-1",
  2: "fill-level-2 stroke-level-2",
  3: "fill-level-3 stroke-level-3",
  4: "fill-level-4 stroke-level-4",
};

// A 7-unit square with a 2-unit round stroke draws as a 9-unit square with soft corners, in one
// path per level: some 700 squares stay a few KB instead of 700 DOM nodes on a low-end phone.
function squaresPath(squares: readonly (readonly [number, number])[]): string {
  return squares.map(([column, row]) => `M${column * UNIT + 1.5} ${row * UNIT + 1.5}h7v7h-7z`).join("");
}

function nameOf({ city }: PlacedCity, locale: Locale): string {
  return locale === "en" ? (city.nameEn ?? city.name) : city.name;
}

/** The continent in contribution squares. On a phone a square is too small to touch: the list next to it is the way in. */
export function AfricaMosaic({ locale, cities, label }: AfricaMosaicProps) {
  const bySquare = new Map<string, PlacedCity[]>();
  for (const placed of cities) {
    const key = `${placed.city.column}:${placed.city.row}`;
    bySquare.set(key, [...(bySquare.get(key) ?? []), placed]);
  }
  const populated = [...bySquare.values()].map((group) => ({
    group,
    column: group[0].city.column,
    row: group[0].city.row,
    level: levelOf(group.reduce((sum, placed) => sum + placed.count, 0)),
  }));
  const empty = landSquares().filter(([column, row]) => !bySquare.has(`${column}:${row}`));

  return (
    <svg viewBox={`0 0 ${MAP_COLUMNS * UNIT} ${MAP_ROWS * UNIT}`} role="img" aria-label={label} className="h-auto w-full">
      <path d={squaresPath(empty)} strokeWidth={2} strokeLinejoin="round" className={LEVEL_CLASS[0]} />
      {populated.map(({ group, column, row, level }) => (
        <a key={`${column}:${row}`} href={localizedPath(locale, `/rankings?country=${group[0].city.country}`)} tabIndex={-1} aria-hidden="true" className="max-lg:pointer-events-none">
          <title>{group.map((placed) => `${nameOf(placed, locale)} · ${placed.count}`).join("\n")}</title>
          <path d={squaresPath([[column, row]])} strokeWidth={2} strokeLinejoin="round" className={`${LEVEL_CLASS[level]} hover:opacity-70`} />
        </a>
      ))}
    </svg>
  );
}

import { AFRICA_ROWS, MAP_CITIES, type MapCity } from "@/lib/africa-map";
import type { CityCount } from "@/services/map";

export type MapLevel = 1 | 2 | 3 | 4;

export interface PlacedCity {
  city: MapCity;
  count: number;
}

export interface PlacedMap {
  /** Cities with at least one profile, most first. */
  cities: PlacedCity[];
  /** Profiles without a city, or whose city is not on the map: counted, never guessed. */
  unplaced: number;
  total: number;
}

/** Cities are typed by hand: "Yaoundé", "YAOUNDE " and "yaounde" are one place. */
export function normalizeCity(raw: string): string {
  return raw
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

const CITY_BY_KEY = new Map<string, MapCity>(
  MAP_CITIES.flatMap((city) =>
    [city.name, city.nameEn, ...(city.aliases ?? [])]
      .filter((name): name is string => Boolean(name))
      .map((name) => [`${city.country}:${normalizeCity(name)}`, city] as const),
  ),
);

export function placeCounts(counts: readonly CityCount[]): PlacedMap {
  const byCity = new Map<MapCity, number>();
  let unplaced = 0;
  let total = 0;
  for (const { country, city, count } of counts) {
    total += count;
    const found = city ? CITY_BY_KEY.get(`${country}:${normalizeCity(city)}`) : undefined;
    if (found) byCity.set(found, (byCity.get(found) ?? 0) + count);
    else unplaced += count;
  }
  const cities = [...byCity].map(([city, count]) => ({ city, count })).sort((a, b) => b.count - a.count || a.city.name.localeCompare(b.city.name));
  return { cities, unplaced, total };
}

// Fixed steps rather than a share of the busiest city, so a square keeps its meaning as the map fills.
export function levelOf(count: number): MapLevel {
  if (count >= 20) return 4;
  if (count >= 5) return 3;
  if (count >= 2) return 2;
  return 1;
}

export function landSquares(): [number, number][] {
  return AFRICA_ROWS.flatMap((line, row) => [...line].flatMap((cell, column): [number, number][] => (cell === "#" ? [[column, row]] : [])));
}

export const MAP_COLUMNS = AFRICA_ROWS[0].length;
export const MAP_ROWS = AFRICA_ROWS.length;

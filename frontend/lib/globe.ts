import { countryName } from "@/lib/countries";
import { normalizeCity, type PlacedCity } from "@/lib/map";
import type { GlobeCity } from "@/components/globe/Globe";
import type { Locale } from "@/i18n/routing";

export const SECTORS = ["fintech", "healthtech", "edtech", "ecommerce", "agritech", "iot", "robotics", "ar-vr"] as const;
export type Sector = (typeof SECTORS)[number];

/** Map squares are two degrees wide: the centre of a square is close enough for a point on a globe. */
export function globeCities(cities: readonly PlacedCity[], people: readonly { login: string; country: string | null; city: string | null }[], locale: Locale, countLabel: (count: number) => string): GlobeCity[] {
  return cities.map(({ city, count }, index) => ({
    id: `city${index}`,
    latitude: 38 - 2 * (city.row + 0.5),
    longitude: -18 + 2 * (city.column + 0.5),
    label: `${locale === "en" ? (city.nameEn ?? city.name) : city.name}, ${countryName(city.country, locale)}`,
    count: countLabel(count),
    size: Math.min(0.09, 0.035 + count * 0.006),
    country: city.country,
    login: people.find((person) => person.country === city.country && person.city && normalizeCity(person.city) === normalizeCity(city.name))?.login ?? null,
  }));
}

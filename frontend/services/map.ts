import { cacheLife } from "next/cache";

// TODO(api-types): replace with components["schemas"]["CityCountResponse"].
export interface CityCount {
  /** ISO 3166-1 alpha-2. */
  country: string;
  /** As the member typed it; null when they gave none. */
  city: string | null;
  count: number;
}

/** Published profiles per city, the same for every visitor: cached briefly. */
export async function getCityCounts(): Promise<CityCount[]> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/rankings/cities`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/rankings/cities answered ${response.status}`);
  return (await response.json()) as CityCount[];
}

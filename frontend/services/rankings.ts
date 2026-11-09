import { cacheLife } from "next/cache";

// TODO(api-types): replace with components["schemas"]["RankingsResponse"] and ["RankingFacetsResponse"].
export const RANKING_SORTS = ["regularity", "contributions", "stars", "external", "seniority"] as const;
export type RankingSort = (typeof RANKING_SORTS)[number];

export interface RankingRow {
  /** Rank in the whole ranking asked for. */
  position: number;
  login: string;
  name: string | null;
  /** ISO 3166-1 alpha-2. */
  country: string | null;
  city: string | null;
  accountCreatedAt: string;
  publicContributions: number;
  activeMonths: number;
  /** Contributions of the last twelve months, oldest first. */
  months: number[];
  stars: number;
  followers: number;
  mergedPullRequestsElsewhere: number;
  languages: string[];
}

export interface RankingPage {
  items: RankingRow[];
  nextCursor: string | null;
  total: number;
}

export interface RankingFacets {
  countries: { value: string; count: number }[];
  languages: { value: string; count: number }[];
}

export interface RankingQuery {
  sort: RankingSort;
  country: string | null;
  language: string | null;
  cursor: string | null;
}

function query(params: Record<string, string | null>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) if (value) search.set(key, value);
  const text = search.toString();
  return text ? `?${text}` : "";
}

/** A page of a ranking, the same for every visitor: cached briefly. */
export async function getRankings({ sort, country, language, cursor }: RankingQuery): Promise<RankingPage> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/rankings${query({ sort, country, language, cursor })}`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/rankings answered ${response.status}`);
  return (await response.json()) as RankingPage;
}

export async function getRankingFacets(country: string | null, language: string | null): Promise<RankingFacets> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/rankings/facets${query({ country, language })}`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/rankings/facets answered ${response.status}`);
  return (await response.json()) as RankingFacets;
}

/** Repositories where published profiles have merged pull requests, the most shared first. */
export async function getAcceptedProjects(): Promise<string[]> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/rankings/projects`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/rankings/projects answered ${response.status}`);
  return ((await response.json()) as { repository: string }[]).map((project) => project.repository);
}

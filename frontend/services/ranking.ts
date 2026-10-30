import { cacheLife } from "next/cache";
import type { CatalogCard } from "./catalog-types";

// TODO(api-types): replace with components["schemas"]["RankingResponse"].
export interface RankedCard extends Omit<CatalogCard, "previewVideoUrl"> {
  rank: number;
  likes: number;
}

/** The Ranking, the same for every visitor: cached briefly, a new like shows within a minute or so. */
export async function getRanking(): Promise<RankedCard[]> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/ranking`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/ranking answered ${response.status}`);
  return ((await response.json()) as { items: RankedCard[] }).items;
}

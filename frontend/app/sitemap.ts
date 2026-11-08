import type { MetadataRoute } from "next";
import { connection } from "next/server";
import type { RankingPage } from "@/services/rankings";

// Bounded: 40 pages of 50. Beyond 2 000 published profiles this needs a sitemap index.
const MAX_PAGES = 40;

type Entry = MetadataRoute.Sitemap[number];

function bothLanguages(site: string, path: string): Entry[] {
  const languages = { fr: `${site}/fr${path}`, en: `${site}/en${path}` };
  return (["fr", "en"] as const).map((locale) => ({ url: languages[locale], alternates: { languages } }));
}

// Public pages only: the rankings and every published profile, each in both languages. Read per
// request, like robots.txt, so it follows new publications; a published profile is always ranked.
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  await connection();
  const site = process.env.SITE_URL ?? "http://localhost:3000";
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const entries: MetadataRoute.Sitemap = bothLanguages(site, "/rankings");

  let cursor: string | null = null;
  for (let page = 0; page < MAX_PAGES; page++) {
    const query = new URLSearchParams({ size: "50", ...(cursor ? { cursor } : {}) });
    const response = await fetch(`${api}/api/rankings?${query}`, { headers: { accept: "application/json" } });
    if (!response.ok) {
      console.error(`Sitemap: GET /api/rankings answered ${response.status}`);
      break;
    }
    const { items, nextCursor } = (await response.json()) as RankingPage;
    items.forEach((row) => entries.push(...bothLanguages(site, `/u/${row.login}`)));
    if (!nextCursor) break;
    cursor = nextCursor;
  }
  return entries;
}

import type { MetadataRoute } from "next";
import { connection } from "next/server";
import type { CatalogPage } from "@/services/catalog-types";

// Bounded: 40 pages of 50. A catalogue beyond 2 000 portfolios needs a sitemap index.
const MAX_PAGES = 40;

type Entry = MetadataRoute.Sitemap[number];

function bothLanguages(site: string, path: string, lastModified?: string): Entry[] {
  const languages = { fr: `${site}/fr${path}`, en: `${site}/en${path}` };
  return (["fr", "en"] as const).map((locale) => ({
    url: languages[locale],
    lastModified,
    alternates: { languages },
  }));
}

// Public pages only: the catalogue in both orders and every published showcase, each in both
// languages. Read per request, like robots.txt, so it follows new publications.
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  await connection();
  const site = process.env.SITE_URL ?? "http://localhost:3000";
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const entries: MetadataRoute.Sitemap = [...bothLanguages(site, ""), ...bothLanguages(site, "/recent")];

  let cursor: string | null = null;
  for (let page = 0; page < MAX_PAGES; page++) {
    const query = new URLSearchParams({ sort: "recent", size: "50", ...(cursor ? { cursor } : {}) });
    const response = await fetch(`${api}/api/catalog?${query}`, { headers: { accept: "application/json" } });
    if (!response.ok) {
      console.error(`Sitemap: GET /api/catalog answered ${response.status}`);
      break;
    }
    const { items, nextCursor } = (await response.json()) as CatalogPage;
    items.forEach((card) => entries.push(...bothLanguages(site, `/p/${card.slug}`, card.publishedAt)));
    if (!nextCursor) break;
    cursor = nextCursor;
  }
  return entries;
}

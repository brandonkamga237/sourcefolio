import { cacheLife } from "next/cache";
import { CATALOG_PAGE_SIZE, type CatalogPage, type CatalogSort } from "./catalog-types";

export type { CatalogCard, CatalogPage, CatalogSort } from "./catalog-types";

/**
 * The first page of the catalogue, the same for every visitor: cached a few minutes, so a busy
 * home page costs the API one call per order. Discover changes order once a day anyway.
 */
export async function getCatalogFirstPage(sort: CatalogSort): Promise<CatalogPage> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/catalog?sort=${sort}&size=${CATALOG_PAGE_SIZE}`, {
    headers: { accept: "application/json" },
  });
  if (!response.ok) throw new Error(`GET /api/catalog?sort=${sort} answered ${response.status}`);
  return (await response.json()) as CatalogPage;
}

import { cacheLife } from "next/cache";
import { CATALOG_PAGE_SIZE, filterParams, type CatalogFacets, type CatalogFilters, type CatalogPage, type CatalogSort } from "./catalog-types";

export type { CatalogCard, CatalogPage, CatalogSort } from "./catalog-types";

/**
 * The first page of the catalogue, the same for every visitor with the same filters: cached a
 * few minutes, so a busy home page costs the API one call per order. Discover changes order
 * once a day anyway.
 */
export async function getCatalogFirstPage(sort: CatalogSort, filters: CatalogFilters = {}): Promise<CatalogPage> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const query = new URLSearchParams([["sort", sort], ["size", String(CATALOG_PAGE_SIZE)], ...filterParams(filters)]);
  const response = await fetch(`${api}/api/catalog?${query}`, { headers: { accept: "application/json" } });
  if (!response.ok) throw new Error(`GET /api/catalog?${query} answered ${response.status}`);
  return (await response.json()) as CatalogPage;
}

/** The countries and interests present, for the filters; empty when the API cannot answer. */
export async function getCatalogFacets(): Promise<CatalogFacets> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${api}/api/catalog/facets`, { headers: { accept: "application/json" } });
    return response.ok ? ((await response.json()) as CatalogFacets) : { countries: [], languages: [] };
  } catch {
    return { countries: [], languages: [] };
  }
}

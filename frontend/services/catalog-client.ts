// Browser side: the next pages, after the server-rendered first one.
import { CATALOG_PAGE_SIZE, filterParams, type CatalogFilters, type CatalogPage, type CatalogSort } from "./catalog-types";

/** The page after {@code cursor}; null when the API could not be reached or refused. */
export async function fetchCatalogPage(sort: CatalogSort, cursor: string, filters: CatalogFilters = {}): Promise<CatalogPage | null> {
  try {
    const query = new URLSearchParams([["sort", sort], ["cursor", cursor], ["size", String(CATALOG_PAGE_SIZE)], ...filterParams(filters)]);
    const response = await fetch(`/api/catalog?${query}`, { headers: { accept: "application/json" } });
    return response.ok ? ((await response.json()) as CatalogPage) : null;
  } catch {
    return null;
  }
}

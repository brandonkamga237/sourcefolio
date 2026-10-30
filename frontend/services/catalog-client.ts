// Browser side: the next pages, after the server-rendered first one.
import { CATALOG_PAGE_SIZE, type CatalogPage, type CatalogSort } from "./catalog-types";

/** The page after {@code cursor}; null when the API could not be reached or refused. */
export async function fetchCatalogPage(sort: CatalogSort, cursor: string): Promise<CatalogPage | null> {
  try {
    const query = new URLSearchParams({ sort, cursor, size: String(CATALOG_PAGE_SIZE) });
    const response = await fetch(`/api/catalog?${query}`, { headers: { accept: "application/json" } });
    return response.ok ? ((await response.json()) as CatalogPage) : null;
  } catch {
    return null;
  }
}

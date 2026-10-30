// Shared by the server and browser sides: services/catalog.ts uses "use cache", which a
// client component must not import.
export type CatalogSort = "discover" | "recent";

// TODO(api-types): replace with components["schemas"]["CatalogResponse"].
export interface CatalogCard {
  slug: string;
  siteUrl: string;
  repository: string;
  repositoryUrl: string;
  ownerLogin: string;
  publishedAt: string;
}

export interface CatalogPage {
  items: CatalogCard[];
  /** null on the last page. */
  nextCursor: string | null;
}

export const CATALOG_PAGE_SIZE = 20;

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
  /** The author's preview, null until they choose one. */
  previewImageUrl: string | null;
  previewVideoUrl: string | null;
  /** GitHub stars of the repository, null until read. Shown, never used to order. */
  repositoryStars: number | null;
}

export interface CatalogPage {
  items: CatalogCard[];
  /** null on the last page. */
  nextCursor: string | null;
  /** How many portfolios the whole catalogue lists. */
  total: number;
}

export const CATALOG_PAGE_SIZE = 20;

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
  /** Public like count, shown on the card; Discover never orders by it. */
  likes: number;
  /** The author's country (ISO code) and specialties, most important first; empty until answered. */
  countryCode: string | null;
  specialties: string[];
  /** The author's own line, null until written. */
  headline: string | null;
  /** What GitHub says about the repository, read daily: its description, main languages, last push. */
  repositoryDescription: string | null;
  repositoryLanguages: string[];
  repositoryPushedAt: string | null;
  /** The role the author declares (a slug), null until answered: their claim, not proven by the code. */
  authorRole: string | null;
  /** How much feedback the showcase shows; never used to order Discover. */
  feedbackCount: number;
}

export interface CatalogPage {
  items: CatalogCard[];
  /** null on the last page. */
  nextCursor: string | null;
  /** How many portfolios the whole catalogue lists. */
  total: number;
}

export const CATALOG_PAGE_SIZE = 20;

/** What a visitor narrows the catalogue to; each part optional. Kept in the address, so a search is shareable. */
export interface CatalogFilters {
  q?: string;
  country?: string;
  /** A language of the source repository, as GitHub names it: what the code is written in, not what the author declares. */
  language?: string;
  /** The role authors declare, as a slug. */
  role?: string;
}

const FILTER_KEYS = ["q", "country", "language", "role"] as const;

/** The filters read from an address's query, blank parts dropped. */
export function readFilters(query: Record<string, string | string[] | undefined> | URLSearchParams): CatalogFilters {
  const get = (key: string) => {
    const value = query instanceof URLSearchParams ? query.get(key) : query[key];
    const single = Array.isArray(value) ? value[0] : value;
    return single && single.trim() ? single.trim() : undefined;
  };
  return Object.fromEntries(FILTER_KEYS.map((key) => [key, get(key)]).filter(([, value]) => value)) as CatalogFilters;
}

/** The filters as query parameters, in a stable order (also a stable cache key). */
export function filterParams(filters: CatalogFilters): [string, string][] {
  return FILTER_KEYS.flatMap((key) => (filters[key] ? [[key, filters[key]] as [string, string]] : []));
}

export function hasFilters(filters: CatalogFilters): boolean {
  return filterParams(filters).length > 0;
}

// TODO(api-types): replace with components["schemas"]["FacetsResponse"].
export interface CatalogFacets {
  countries: { value: string; count: number }[];
  languages: { value: string; count: number }[];
  roles: { value: string; count: number }[];
}

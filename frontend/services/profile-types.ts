// Shared by the server service and the browser components: no server import here.
// TODO(api-types): replace with components["schemas"]["ProfileResponse"] and ["EmailResponse"].

export interface Profile {
  /** ISO 3166-1 alpha-2, null until answered. */
  country: string | null;
  /** Up to four interests, most important first; empty until answered. */
  specialties: string[];
  /** The member's own line, null until written. */
  headline: string | null;
  /** The role the member declares, null until answered. */
  role: string | null;
  /** The member's own words: shown on the profile, never ranked. */
  city: string | null;
  bio: string | null;
  /** The LinkedIn handle, and the link built from it. */
  linkedIn: string | null;
  linkedInUrl: string | null;
  /** null while the profile is private. */
  publishedAt: string | null;
}

export const MAX_SPECIALTIES = 4;
export const MAX_HEADLINE = 80;
export const MAX_CITY = 60;
export const MAX_BIO = 160;

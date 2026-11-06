// Shared by the server service and the browser components (profile questions): no server import here.
// TODO(api-types): replace with components["schemas"]["ProfileResponse"] and ["EmailResponse"].

// The fifty interests the backend accepts, grouped as the profile question shows them.
export const SPECIALTY_GROUPS = {
  dev: ["frontend", "backend", "fullstack", "mobile", "desktop", "api", "game", "embedded", "web3", "low-code"],
  data: ["data-ai", "machine-learning", "data-engineering", "data-analysis", "computer-vision", "nlp", "generative-ai"],
  infra: ["devops", "cloud", "sre", "networking", "database", "security"],
  design: ["ui-design", "ux-design", "product", "design-systems", "motion-design", "accessibility", "creative-coding"],
  craft: ["testing", "performance", "open-source", "documentation"],
  sectors: ["fintech", "healthtech", "edtech", "ecommerce", "agritech", "iot", "robotics", "ar-vr"],
  roles: ["tech-lead", "freelance", "teaching", "devrel", "community", "entrepreneurship", "research", "other"],
} as const;

export type SpecialtyGroup = keyof typeof SPECIALTY_GROUPS;
export type Specialty = (typeof SPECIALTY_GROUPS)[SpecialtyGroup][number];

export const SPECIALTIES: readonly Specialty[] = Object.values(SPECIALTY_GROUPS).flat();

// The twelve roles a member can declare, one at most: their claim, shown as theirs.
export const ROLES = ["frontend", "backend", "fullstack", "mobile", "devops", "data-ai", "security", "design", "qa", "game", "embedded", "learning"] as const;
export type Role = (typeof ROLES)[number];

export interface Profile {
  /** ISO 3166-1 alpha-2, null until answered. */
  country: string | null;
  /** Up to four interests, most important first; empty until answered. */
  specialties: Specialty[];
  /** The member's own line, null until written. */
  headline: string | null;
  /** The role the member declares, null until answered. */
  role: Role | null;
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

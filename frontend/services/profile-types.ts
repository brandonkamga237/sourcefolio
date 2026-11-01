// Shared by the server service and the browser components (profile questions): no server import here.
// TODO(api-types): replace with components["schemas"]["ProfileResponse"] and ["EmailResponse"].
export type Specialty =
  | "frontend"
  | "backend"
  | "fullstack"
  | "mobile"
  | "devops"
  | "data-ai"
  | "game"
  | "embedded"
  | "security"
  | "other";

export const SPECIALTIES: readonly Specialty[] = [
  "frontend",
  "backend",
  "fullstack",
  "mobile",
  "devops",
  "data-ai",
  "game",
  "embedded",
  "security",
  "other",
];

export interface Profile {
  /** ISO 3166-1 alpha-2, null until answered. */
  country: string | null;
  /** Up to three, most important first; empty until answered. */
  specialties: Specialty[];
}

export const MAX_SPECIALTIES = 3;

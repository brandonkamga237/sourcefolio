// Shared by the server services and the browser components: no server import here.
// TODO(api-types): replace with components["schemas"]["AnalysisResponse"] and ["ProfileCardResponse"].

export interface ContributionMonth {
  /** ISO year-month, e.g. 2026-03. */
  month: string;
  contributions: number;
}

export interface ContributionDay {
  /** ISO date. */
  date: string;
  contributions: number;
  /** GitHub's shade, 0 (none) to 4. */
  level: 0 | 1 | 2 | 3 | 4;
}

export interface LanguageShare {
  name: string;
  /** GitHub's colour for the language; null for some. */
  color: string | null;
  bytes: number;
}

export interface ExternalProject {
  /** owner/name */
  repository: string;
  /** The member's merged pull requests there, on GitHub. */
  url: string;
  mergedPullRequests: number;
}

export interface TopRepository {
  name: string;
  description: string | null;
  url: string;
  stars: number;
  primaryLanguage: string | null;
}

/** The six indicators of docs/spec-v2.md, each provable on GitHub. */
export interface Indicators {
  analyzedAt: string;
  name: string | null;
  bio: string | null;
  location: string | null;
  accountCreatedAt: string;
  publicContributions: number;
  activeMonths: number;
  months: ContributionMonth[];
  calendar: ContributionDay[];
  stars: number;
  followers: number;
  languages: LanguageShare[];
  mergedPullRequestsElsewhere: number;
  externalProjects: ExternalProject[];
  topRepositories: TopRepository[];
  proofs: { profile: string; repositories: string; mergedElsewhere: string };
}

export type AnalysisStatus = "NONE" | "PENDING" | "READY" | "FAILED";

export interface Analysis {
  status: AnalysisStatus;
  /** A reading was asked for and has not answered yet. */
  refreshing: boolean;
  login: string | null;
  indicators: Indicators | null;
}

/** A published profile: the member's own words and the indicators. */
export interface ProfileCard {
  login: string;
  name: string | null;
  /** ISO 3166-1 alpha-2. */
  country: string;
  city: string | null;
  /** A slug from the sector list; null until declared. */
  sector?: string | null;
  bio: string | null;
  linkedInUrl: string | null;
  indicators: Indicators;
}

import { serverGet } from "@/lib/api/server";

// TODO(api-types): replace with components["schemas"]["ShowcaseCandidatesResponse"] from
// @/lib/api/schema once `pnpm api:types` has run against a live backend.
export type CandidateReason = "USER_PAGES" | "NAMED_REPOSITORY" | "HOMEPAGE" | "PROJECT_PAGES";

export interface ShowcaseCandidate {
  repository: string;
  repositoryUrl: string;
  siteUrl: string;
  reason: CandidateReason;
}

export interface ShowcaseCandidates {
  /** null until the first detection after sign-in has finished. */
  scannedAt: string | null;
  candidates: ShowcaseCandidate[];
}

/** The signed-in member's last portfolio detection; null when the API cannot answer for them. */
export async function getMyCandidates(): Promise<ShowcaseCandidates | null> {
  const { status, data } = await serverGet<ShowcaseCandidates>("/api/me/showcase-candidates");
  if (status === 200 && data !== null) return data;
  if (status !== 401 && status !== 0) console.error(`GET /api/me/showcase-candidates answered ${status}`);
  return null;
}

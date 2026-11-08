// Browser side: the analysis screen polls while a reading runs.
import { clientGet, clientPost, type ClientResult } from "@/lib/api/client";
import type { Analysis } from "./contribution-types";

export const myAnalysisKey = ["me", "analysis"] as const;

export function fetchMyAnalysis(): Promise<ClientResult<Analysis>> {
  return clientGet<Analysis>("/api/me/analysis");
}

/** Asks for a reading; the API skips it when the last one is fresh. */
export function requestMyAnalysis(): Promise<ClientResult<null>> {
  return clientPost<null>("/api/me/analysis");
}

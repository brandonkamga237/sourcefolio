import { serverGet } from "@/lib/api/server";
import type { Analysis } from "./contribution-types";

/** The member's own analysis; null when the API cannot answer for them (signed out, down). */
export async function getMyAnalysis(): Promise<Analysis | null> {
  const result = await serverGet<Analysis>("/api/me/analysis");
  if (result.status !== 200 || !result.data) {
    if (![401, 0].includes(result.status)) console.error(`GET /api/me/analysis answered ${result.status}`);
    return null;
  }
  return result.data;
}

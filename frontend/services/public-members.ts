import { cacheLife } from "next/cache";
import type { PublicMember } from "./members";

/**
 * A member's public card read on the server, the same for every reader: cached briefly. null
 * when the member has no published portfolio.
 */
export async function getPublicMember(login: string): Promise<PublicMember | null> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/members/${encodeURIComponent(login)}`, { headers: { accept: "application/json" } });
  if (response.status === 200) return (await response.json()) as PublicMember;
  if (response.status === 404) return null;
  throw new Error(`GET /api/members/${login} answered ${response.status}`);
}

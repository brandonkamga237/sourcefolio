import { cacheLife } from "next/cache";
import type { ProfileCard } from "./contribution-types";

/**
 * A published profile read on the server, the same for every reader: cached briefly. null when
 * the login is unknown or the profile is private (the API does not tell them apart).
 */
export async function getPublicProfile(login: string): Promise<ProfileCard | null> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/profiles/${encodeURIComponent(login)}`, { headers: { accept: "application/json" } });
  if (response.status === 200) return (await response.json()) as ProfileCard;
  if (response.status === 404) return null;
  throw new Error(`GET /api/profiles/${login} answered ${response.status}`);
}

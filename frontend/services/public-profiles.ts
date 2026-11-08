import { cacheLife } from "next/cache";
import type { ProfileCard } from "./contribution-types";

/**
 * A published profile as it is right now. null when the login is unknown or the profile is
 * private (the API does not tell them apart). For what the proxy's check does not cover.
 */
export async function readPublicProfile(login: string): Promise<ProfileCard | null> {
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/profiles/${encodeURIComponent(login)}`, { headers: { accept: "application/json" } });
  if (response.status === 200) return (await response.json()) as ProfileCard;
  if (response.status === 404) return null;
  throw new Error(`GET /api/profiles/${login} answered ${response.status}`);
}

/** The same for every reader, so cached briefly; a page only renders it once the proxy has checked. */
export async function getPublicProfile(login: string): Promise<ProfileCard | null> {
  "use cache";
  cacheLife("minutes");
  return readPublicProfile(login);
}

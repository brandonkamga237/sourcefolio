import { clientGet, type ClientResult } from "@/lib/api/client";
import type { ProfileCard } from "./contribution-types";

/** Whether a login has a published profile; 0 when the API could not be reached. */
export async function checkPublicProfile(login: string): Promise<number> {
  return (await clientGet<ProfileCard>(`/api/profiles/${encodeURIComponent(login)}`)).status;
}

/** A published profile for the live card; status 404 when the login has none. */
export function fetchPublicProfile(login: string): Promise<ClientResult<ProfileCard>> {
  return clientGet<ProfileCard>(`/api/profiles/${encodeURIComponent(login)}`);
}

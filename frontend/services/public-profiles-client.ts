import { clientGet } from "@/lib/api/client";
import type { ProfileCard } from "./contribution-types";

/** Whether a login has a published profile; 0 when the API could not be reached. */
export async function checkPublicProfile(login: string): Promise<number> {
  return (await clientGet<ProfileCard>(`/api/profiles/${encodeURIComponent(login)}`)).status;
}

import { serverGet } from "@/lib/api/server";

import type { Profile } from "./profile-types";

export type { Profile, Specialty } from "./profile-types";
export { MAX_SPECIALTIES, SPECIALTIES } from "./profile-types";

export interface MyAnswers extends Profile {
  /** The alert address, null when the member gave none. */
  email: string | null;
}

/** The member's profile answers and alert address; null when the API cannot answer for them. */
export async function getMyAnswers(): Promise<MyAnswers | null> {
  const [profile, email] = await Promise.all([
    serverGet<Profile>("/api/me/profile"),
    serverGet<{ email: string | null }>("/api/me/email"),
  ]);
  if (profile.status !== 200 || !profile.data || email.status !== 200 || !email.data) {
    if (![401, 0].includes(profile.status)) console.error(`GET /api/me/profile answered ${profile.status}`);
    return null;
  }
  return { ...profile.data, email: email.data.email };
}

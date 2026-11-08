// Browser side: each answer is saved as soon as it is given.
import { clientDelete, clientPut, type ClientResult } from "@/lib/api/client";
import type { Profile } from "./profile";

export function answerCountry(country: string): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/country", { country });
}

/** The city for the profile and the map; null or blank removes it. */
export function answerCity(city: string | null): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/city", { city });
}

/** Up to 160 characters; null or blank removes it. */
export function answerBio(bio: string | null): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/bio", { bio });
}

/** A LinkedIn handle or profile link; null or blank removes it. */
export function answerLinkedIn(linkedIn: string | null): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/linkedin", { linkedIn });
}

/** The member's consent: the profile becomes public. Needs a country. */
export function publishProfile(): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/publication", {});
}

/** Takes the profile back at once. */
export function unpublishProfile(): Promise<ClientResult<Profile>> {
  return clientDelete<Profile>("/api/me/profile/publication");
}

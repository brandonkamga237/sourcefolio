// Browser side: each answer is saved as soon as it is given.
import { clientPut, type ClientResult } from "@/lib/api/client";
import type { Profile } from "./profile";

export function answerCountry(country: string): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/country", { country });
}

/** One to three slugs, most important first; replaces the earlier answer. */
export function answerSpecialties(specialties: string[]): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/specialties", { specialties });
}

/** null or blank removes the address. */
export function saveEmail(email: string | null): Promise<ClientResult<{ email: string | null }>> {
  return clientPut<{ email: string | null }>("/api/me/email", { email });
}

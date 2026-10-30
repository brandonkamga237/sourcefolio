// Browser side: each answer is saved as soon as it is given.
import { clientPut, type ClientResult } from "@/lib/api/client";
import type { Profile } from "./profile";

export function answerCountry(country: string): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/country", { country });
}

export function answerSpecialty(specialty: string): Promise<ClientResult<Profile>> {
  return clientPut<Profile>("/api/me/profile/specialty", { specialty });
}

/** null or blank removes the address. */
export function saveEmail(email: string | null): Promise<ClientResult<{ email: string | null }>> {
  return clientPut<{ email: string | null }>("/api/me/email", { email });
}

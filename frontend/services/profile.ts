import { serverGet } from "@/lib/api/server";

// TODO(api-types): replace with components["schemas"]["ProfileResponse"] and ["EmailResponse"].
export type Specialty =
  | "frontend"
  | "backend"
  | "fullstack"
  | "mobile"
  | "devops"
  | "data-ai"
  | "game"
  | "embedded"
  | "security"
  | "other";

export const SPECIALTIES: readonly Specialty[] = [
  "frontend",
  "backend",
  "fullstack",
  "mobile",
  "devops",
  "data-ai",
  "game",
  "embedded",
  "security",
  "other",
];

export interface Profile {
  /** ISO 3166-1 alpha-2, null until answered. */
  country: string | null;
  specialty: Specialty | null;
}

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

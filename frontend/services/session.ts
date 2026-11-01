// Browser side: whether the reader is signed in, for pages served the same to everyone.
import { clientGet } from "@/lib/api/client";

export const sessionKey = ["session"] as const;

/** True for a member, false for a visitor or when the API cannot be reached. */
export async function fetchSignedIn(): Promise<boolean> {
  return (await clientGet("/api/me")).status === 200;
}

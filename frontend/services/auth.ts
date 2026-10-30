import { clientPost } from "@/lib/api/client";

/** Ends the member's session; true once the API has confirmed it. */
export async function signOut(): Promise<boolean> {
  const { status } = await clientPost("/api/auth/logout");
  return status === 204;
}

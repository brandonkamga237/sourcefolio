// Browser side: what a member does to their own account.
import { clientDelete, type ClientResult } from "@/lib/api/client";

/** Deletes the account at once, with the profile, and signs out everywhere. */
export function deleteMyAccount(): Promise<ClientResult<null>> {
  return clientDelete<null>("/api/me");
}

import { clientPost, type ClientResult } from "@/lib/api/client";

export function joinWaitlist(email: string, consent: boolean): Promise<ClientResult<void>> {
  return clientPost<void>("/api/waitlist", { email, consent });
}

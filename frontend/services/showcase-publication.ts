// Browser side: services/showcases.ts reads the session cookie on the server and cannot be
// imported by a client component.
import { clientDelete, clientPost, type ClientResult } from "@/lib/api/client";
import type { MyShowcaseResponse } from "./showcases";

/** "Yes, publish": the API checks that the site answers (a few seconds) before replying. */
export function publishMyShowcase(repository: string): Promise<ClientResult<MyShowcaseResponse>> {
  return clientPost<MyShowcaseResponse>("/api/me/showcase", { repository });
}

/** Takes the portfolio down for good, likes included; also the first step of changing it. */
export function withdrawMyShowcase(): Promise<ClientResult<null>> {
  return clientDelete<null>("/api/me/showcase");
}

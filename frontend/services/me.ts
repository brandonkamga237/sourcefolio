import { serverGet } from "@/lib/api/server";

// TODO(api-types): replace with components["schemas"]["MeResponse"] from @/lib/api/schema once
// `pnpm api:types` has run against a live backend (it needs PostgreSQL, so Docker).
export interface Me {
  id: string;
  login: string;
  language: "fr" | "en";
}

/**
 * The signed-in member, or null for a visitor (the API answers 401). An API that cannot be
 * reached also gives null: the page stays usable in its visitor version instead of failing,
 * and the outage shows in the logs and the monitoring alerts.
 */
export async function getMe(): Promise<Me | null> {
  const { status, data } = await serverGet<Me>("/api/me");
  if (status === 200 && data !== null) return data;
  if (status !== 401 && status !== 0) console.error(`GET /api/me answered ${status}`);
  return null;
}

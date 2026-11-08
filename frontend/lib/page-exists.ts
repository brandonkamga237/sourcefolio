// Asked by the proxy before a profile page renders: the page streams, and a
// not-found found after the first bytes can no longer change the status, so a missing page
// would answer 200. One small call per address, remembered for a minute so a shared link costs
// one call.
const known = new Map<string, { exists: boolean; until: number }>();
const TTL_MS = 60_000;
const MAX_ENTRIES = 2_000;

/** false only when the API answers 404 for this path; true when unsure, so the page decides. */
export async function apiResourceExists(path: string): Promise<boolean> {
  const cached = known.get(path);
  if (cached && cached.until > Date.now()) return cached.exists;
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${api}${path}`, { headers: { accept: "application/json" }, signal: AbortSignal.timeout(2000) });
    if (response.status !== 200 && response.status !== 404) return true;
    const exists = response.status === 200;
    if (known.size >= MAX_ENTRIES) known.delete(known.keys().next().value as string);
    known.set(path, { exists, until: Date.now() + TTL_MS });
    return exists;
  } catch {
    return true;
  }
}

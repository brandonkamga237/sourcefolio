// Asked by the proxy before a profile page renders: the page streams, and a not-found found
// after the first bytes can no longer change the status, so a missing page would answer 200.
// Never remembered: taking a profile down works at once, and a link copied right after
// publishing must open.

/** false only when the API answers 404 for this path; true when unsure, so the page decides. */
export async function apiResourceExists(path: string): Promise<boolean> {
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${api}${path}`, { headers: { accept: "application/json" }, signal: AbortSignal.timeout(2000) });
    return response.status !== 404;
  } catch {
    return true;
  }
}

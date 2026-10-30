import { cacheLife } from "next/cache";

// TODO(api-types): replace with components["schemas"]["PublicShowcaseResponse"].
export interface PublicShowcase {
  slug: string;
  siteUrl: string;
  repository: string;
  repositoryUrl: string;
  status: "PUBLISHED" | "UNAVAILABLE" | "NEEDS_FIX";
  publishedAt: string | null;
  ownerLogin: string;
  ownerUrl: string;
  previewImageUrl: string | null;
  previewVideoUrl: string | null;
}

/**
 * A showcase as visitors see it; null when there is none at this address. No cookie goes
 * along: the answer is the same for everyone, so it is cached for a few minutes, which absorbs
 * a link going round a WhatsApp group. A status change shows within that delay.
 */
export async function getPublicShowcase(slug: string): Promise<PublicShowcase | null> {
  "use cache";
  cacheLife("minutes");
  const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
  const response = await fetch(`${api}/api/showcases/${encodeURIComponent(slug)}`, {
    headers: { accept: "application/json" },
  });
  if (response.status === 200) return (await response.json()) as PublicShowcase;
  if (response.status === 404) return null;
  // Thrown rather than returned: an outage must not be cached as "this portfolio does not exist".
  throw new Error(`GET /api/showcases/${slug} answered ${response.status}`);
}

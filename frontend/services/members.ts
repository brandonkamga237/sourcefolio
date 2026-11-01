// Browser side: a member's public card, read when the visitor opens it.
import { clientGet, type ClientResult } from "@/lib/api/client";

// TODO(api-types): replace with components["schemas"]["MemberResponse"].
export interface PublicMember {
  login: string;
  githubUrl: string;
  countryCode: string | null;
  specialties: string[];
  onSourceFolioSince: string;
  likesReceived: number;
  showcase: {
    slug: string;
    siteUrl: string;
    repository: string;
    repositoryUrl: string;
    previewImageUrl: string | null;
    repositoryStars: number | null;
    likes: number;
    publishedAt: string;
  };
}

export const memberKey = (login: string) => ["member", login.toLowerCase()] as const;

export function fetchMember(login: string): Promise<ClientResult<PublicMember>> {
  return clientGet<PublicMember>(`/api/members/${encodeURIComponent(login)}`);
}

/** GitHub serves every account's public avatar at this address; no token, no extra scope. */
export function avatarUrl(login: string, size = 160): string {
  return `https://github.com/${encodeURIComponent(login)}.png?size=${size}`;
}

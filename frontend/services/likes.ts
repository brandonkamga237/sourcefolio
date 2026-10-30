// Browser side: likes change under the visitor's hands, so they are read and written from the client.
import { clientDelete, clientGet, clientPut, type ClientResult } from "@/lib/api/client";

// TODO(api-types): replace with components["schemas"]["LikeResponse"].
export interface LikeState {
  count: number;
  liked: boolean;
  /** The reader is the author: the count is shown without a button. */
  own: boolean;
}

export const likesKey = (slug: string) => ["likes", slug] as const;

export function fetchLikes(slug: string): Promise<ClientResult<LikeState>> {
  return clientGet<LikeState>(`/api/showcases/${encodeURIComponent(slug)}/likes`);
}

export function setLiked(slug: string, liked: boolean): Promise<ClientResult<LikeState>> {
  const path = `/api/showcases/${encodeURIComponent(slug)}/like`;
  return liked ? clientPut<LikeState>(path, {}) : clientDelete<LikeState>(path);
}

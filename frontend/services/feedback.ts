// Browser side: feedback is written under the reader's hands and read page by page.
import { clientDelete, clientGet, clientPost, clientPut, type ClientResult } from "@/lib/api/client";

export const FEEDBACK_ANGLES = ["DESIGN", "CLARITY", "PERFORMANCE", "MOBILE", "CODE"] as const;
export type FeedbackAngle = (typeof FEEDBACK_ANGLES)[number];
export const FEEDBACK_MIN = 10;
export const FEEDBACK_MAX = 1000;

// TODO(api-types): replace with components["schemas"]["FeedbackResponse"] and ["FeedbackPageResponse"].
export interface Feedback {
  id: string;
  authorLogin: string;
  angle: FeedbackAngle | null;
  body: string;
  /** "fr" or "en": the page it was written on. Never translated. */
  language: string;
  createdAt: string;
  /** The reader wrote it, so they may remove it. */
  mine: boolean;
}

export interface FeedbackPage {
  items: Feedback[];
  nextCursor: string | null;
  total: number;
}

export const feedbackKey = (slug: string) => ["feedback", slug] as const;

export function fetchFeedback(slug: string, cursor: string | null): Promise<ClientResult<FeedbackPage>> {
  const query = new URLSearchParams({ size: "10", ...(cursor ? { cursor } : {}) });
  return clientGet<FeedbackPage>(`/api/showcases/${encodeURIComponent(slug)}/feedback?${query}`);
}

export function postFeedback(slug: string, draft: { angle: FeedbackAngle | null; body: string; language: string }): Promise<ClientResult<Feedback>> {
  return clientPost<Feedback>(`/api/showcases/${encodeURIComponent(slug)}/feedback`, draft);
}

export function deleteFeedback(id: string): Promise<ClientResult<null>> {
  return clientDelete<null>(`/api/feedback/${encodeURIComponent(id)}`);
}

// TODO(api-types): replace with components["schemas"]["ReceivedFeedbackResponse"].
export interface ReceivedFeedbackPage extends FeedbackPage {
  /** When the author last read their feedback; null the first time, when everything is new. */
  seenAt: string | null;
  unread: number;
}

export const receivedKey = ["feedback", "received"] as const;
export const unreadKey = ["feedback", "unread"] as const;

export function fetchReceived(cursor: string | null): Promise<ClientResult<ReceivedFeedbackPage>> {
  const query = new URLSearchParams({ size: "10", ...(cursor ? { cursor } : {}) });
  return clientGet<ReceivedFeedbackPage>(`/api/me/feedback?${query}`);
}

/** The number on the Me tab; 0 for a visitor or when the API cannot answer. */
export async function fetchUnread(): Promise<number> {
  const result = await clientGet<{ count: number }>("/api/me/feedback/unread");
  return result.data?.count ?? 0;
}

export function markFeedbackSeen(): Promise<ClientResult<null>> {
  return clientPut<null>("/api/me/feedback/seen", {});
}

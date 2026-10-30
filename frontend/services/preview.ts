// Browser side: the author changes the preview of their card.
import { clientDelete, clientUpload, type ClientResult } from "@/lib/api/client";
import type { MyShowcaseResponse } from "./showcases";

export type PreviewKind = "image" | "video";

export const PREVIEW_LIMITS: Record<PreviewKind, { bytes: number; accept: string }> = {
  image: { bytes: 5 * 1024 * 1024, accept: "image/jpeg,image/png,image/webp" },
  video: { bytes: 8 * 1024 * 1024, accept: "video/mp4" },
};

export function uploadPreview(kind: PreviewKind, file: File): Promise<ClientResult<MyShowcaseResponse>> {
  return clientUpload<MyShowcaseResponse>(`/api/me/showcase/preview/${kind}`, file);
}

export function removePreview(kind: PreviewKind): Promise<ClientResult<MyShowcaseResponse>> {
  return clientDelete<MyShowcaseResponse>(`/api/me/showcase/preview/${kind}`);
}

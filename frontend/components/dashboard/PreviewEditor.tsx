"use client";

import { useRouter } from "next/navigation";
import { useState, type ChangeEvent } from "react";
import { PREVIEW_LIMITS, removePreview, uploadPreview, type PreviewKind } from "@/services/preview";

export interface PreviewEditorLabels {
  title: string;
  lead: string;
  imageLabel: string;
  imageHint: string;
  videoLabel: string;
  videoHint: string;
  choose: string;
  change: string;
  remove: string;
  sending: string;
  tooBig: string;
  failed: string;
}

interface PreviewEditorProps {
  hasImage: boolean;
  hasVideo: boolean;
  labels: PreviewEditorLabels;
}

// The size is checked here before sending, so a phone does not upload 8 MB to be told no; the
// server still checks the size, the real type and the duration.
export function PreviewEditor({ hasImage, hasVideo, labels }: PreviewEditorProps) {
  const router = useRouter();
  const [busy, setBusy] = useState<PreviewKind | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function run(kind: PreviewKind, request: () => ReturnType<typeof removePreview>) {
    setBusy(kind);
    setError(null);
    const result = await request();
    setBusy(null);
    if (result.data) router.refresh();
    else setError(result.message ?? labels.failed);
  }

  function onFile(kind: PreviewKind) {
    return (event: ChangeEvent<HTMLInputElement>) => {
      const file = event.target.files?.[0];
      event.target.value = "";
      if (!file) return;
      if (file.size > PREVIEW_LIMITS[kind].bytes) {
        setError(labels.tooBig);
        return;
      }
      void run(kind, () => uploadPreview(kind, file));
    };
  }

  const rows: { kind: PreviewKind; label: string; hint: string; present: boolean }[] = [
    { kind: "image", label: labels.imageLabel, hint: labels.imageHint, present: hasImage },
    { kind: "video", label: labels.videoLabel, hint: labels.videoHint, present: hasVideo },
  ];

  return (
    <section aria-labelledby="preview-editor" className="flex flex-col gap-3 rounded-card border border-line bg-surface p-4">
      <div className="flex flex-col gap-0.5">
        <h3 id="preview-editor" className="text-[15px] font-semibold">{labels.title}</h3>
        <p className="text-[13px] leading-snug text-ink-muted">{labels.lead}</p>
      </div>
      <div className="grid gap-3 sm:grid-cols-2">
        {rows.map((row) => (
          <div key={row.kind} className="flex flex-col gap-2 rounded-control bg-surface-sunken p-3">
            <div className="flex flex-col">
              <span className="text-sm font-semibold">{row.label}</span>
              <span className="text-xs text-ink-muted">{row.hint}</span>
            </div>
            <div className="flex flex-wrap gap-2">
              <label
                className={`press flex h-11 cursor-pointer items-center rounded-control px-4 text-sm font-semibold ${
                  row.present ? "border border-ink bg-surface text-ink" : "bg-ink text-ink-inverse"
                } ${busy ? "pointer-events-none opacity-60" : ""}`}
              >
                {busy === row.kind ? labels.sending : row.present ? labels.change : labels.choose}
                <input type="file" accept={PREVIEW_LIMITS[row.kind].accept} onChange={onFile(row.kind)} disabled={busy !== null} className="sr-only" />
              </label>
              {row.present ? (
                <button
                  type="button"
                  onClick={() => void run(row.kind, () => removePreview(row.kind))}
                  disabled={busy !== null}
                  className="press flex h-11 items-center px-2 text-sm font-semibold text-ink-muted disabled:opacity-60"
                >
                  {labels.remove}
                </button>
              ) : null}
            </div>
          </div>
        ))}
      </div>
      {error ? (
        <p role="alert" className="text-[13px] leading-normal text-ink-body">
          {error}
        </p>
      ) : null}
    </section>
  );
}

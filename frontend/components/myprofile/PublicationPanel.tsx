"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { publishProfile, unpublishProfile } from "@/services/profile-answers";

interface PublicationPanelProps {
  published: boolean;
  /** Publishing needs a country: rankings are per country. */
  canPublish: boolean;
  /** The absolute address of the public profile. */
  publicUrl: string;
  labels: {
    visibility: string; privateTitle: string; privateLead: string; publicTitle: string; publicLead: string;
    publish: string; publishing: string; unpublish: string; unpublishing: string; needCountry: string;
    publicLink: string; copy: string; copied: string; failed: string;
  };
}

/** The member's consent, the one main action of the screen. */
export function PublicationPanel({ published, canPublish, publicUrl, labels }: PublicationPanelProps) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  async function toggle() {
    setBusy(true);
    setError(null);
    const result = published ? await unpublishProfile() : await publishProfile();
    setBusy(false);
    if (result.data) router.refresh();
    else setError(result.message ?? labels.failed);
  }

  async function copy() {
    await navigator.clipboard.writeText(publicUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <section aria-labelledby="visibility-title" className="flex flex-col gap-3">
      <h2 id="visibility-title" className="text-[14px] font-semibold">{labels.visibility}</h2>
      <div className={`flex flex-col gap-1 rounded-card border p-4 ${published ? "border-success bg-surface" : "border-line bg-surface-raised"}`}>
        <p className="text-[15px] font-semibold">{published ? labels.publicTitle : labels.privateTitle}</p>
        <p className="text-[13px] text-ink-muted">{published ? labels.publicLead : labels.privateLead}</p>
      </div>
      {published ? (
        <div className="flex flex-col gap-2">
          <p className="text-[13px] text-ink-muted">{labels.publicLink}</p>
          <a href={publicUrl} className="truncate text-[14px] text-accent hover:underline">{publicUrl.replace(/^https?:\/\//, "")}</a>
          <div className="flex flex-wrap gap-2">
            <button type="button" onClick={() => void copy()} className="press h-11 rounded-control bg-cta px-4 text-[14px] font-semibold text-cta-ink hover:bg-cta-hover">
              {copied ? labels.copied : labels.copy}
            </button>
            <button type="button" onClick={() => void toggle()} disabled={busy} className="press h-11 rounded-control border border-line px-4 text-[14px] font-semibold text-ink-muted disabled:opacity-60">
              {busy ? labels.unpublishing : labels.unpublish}
            </button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          <button
            type="button"
            onClick={() => void toggle()}
            disabled={busy || !canPublish}
            className="press h-12 rounded-control bg-cta px-5 text-[15px] font-semibold text-cta-ink hover:bg-cta-hover disabled:opacity-50"
          >
            {busy ? labels.publishing : labels.publish}
          </button>
          {!canPublish ? <p className="text-[13px] text-ink-muted">{labels.needCountry}</p> : null}
        </div>
      )}
      {error ? <p role="alert" className="text-[13px] text-danger">{error}</p> : null}
    </section>
  );
}

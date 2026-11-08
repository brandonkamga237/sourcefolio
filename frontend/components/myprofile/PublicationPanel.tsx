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
      <h2 id="visibility-title" className="text-[13px] text-ink-muted">{labels.visibility}</h2>
      <div className={"flex flex-col gap-1 rounded-card bg-surface-sunken p-4"}>
        <p className="text-[15px] font-medium">{published ? labels.publicTitle : labels.privateTitle}</p>
        <p className="text-[13px] text-ink-muted">{published ? labels.publicLead : labels.privateLead}</p>
      </div>
      {published ? (
        <div className="flex flex-col gap-2">
          <p className="text-[13px] text-ink-muted">{labels.publicLink}</p>
          <a href={publicUrl} className="link truncate text-[14px]">{publicUrl.replace(/^https?:\/\//, "")}</a>
          <div className="flex flex-wrap gap-2">
            <button type="button" onClick={() => void copy()} className="btn btn-primary">
              {copied ? labels.copied : labels.copy}
            </button>
            <button type="button" onClick={() => void toggle()} disabled={busy} className="btn btn-secondary">
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
            className="btn btn-primary min-h-12 text-[15px]"
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

"use client";

import { useState } from "react";
import { IconCheck, IconShare } from "@/components/icons";

interface ShareActionProps {
  path: string;
  title: string;
  label: string;
  copiedLabel: string;
  className?: string;
}

// The phone's share sheet where there is one (WhatsApp first for most members), the link copied
// otherwise. Built from the page's own origin.
export function ShareAction({ path, title, label, copiedLabel, className = "" }: ShareActionProps) {
  const [copied, setCopied] = useState(false);

  async function share() {
    const url = `${window.location.origin}${path}`;
    if (navigator.share) {
      try {
        await navigator.share({ title, url });
        return;
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError") return;
      }
    }
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      // Nothing to copy into: the button stays as it was.
    }
  }

  return (
    <button type="button" onClick={share} className={className}>
      {copied ? <IconCheck aria-hidden="true" className="size-4 text-success" /> : <IconShare aria-hidden="true" className="size-4" />}
      <span role="status">{copied ? copiedLabel : label}</span>
    </button>
  );
}

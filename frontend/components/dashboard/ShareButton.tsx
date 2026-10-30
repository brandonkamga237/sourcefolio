"use client";

import { useState } from "react";

interface ShareButtonProps {
  path: string;
  title: string;
  label: string;
  copiedLabel: string;
}

// Most members share from a phone, where the system sheet offers WhatsApp directly. Elsewhere
// the link is copied. The address is built from the page's origin, the one the member is on.
export function ShareButton({ path, title, label, copiedLabel }: ShareButtonProps) {
  const [copied, setCopied] = useState(false);

  async function handleClick() {
    const url = `${window.location.origin}${path}`;
    if (navigator.share) {
      try {
        await navigator.share({ title, url });
        return;
      } catch (error) {
        // Closing the sheet is a choice, not a failure.
        if (error instanceof DOMException && error.name === "AbortError") return;
      }
    }
    await navigator.clipboard.writeText(url);
    setCopied(true);
  }

  return (
    <div className="flex flex-col gap-1">
      <button
        type="button"
        onClick={handleClick}
        className="flex h-11 items-center justify-center rounded-control bg-accent px-4 text-sm font-semibold text-white"
      >
        {label}
      </button>
      <p role="status" className="text-xs text-ink-muted empty:hidden">
        {copied ? copiedLabel : null}
      </p>
    </div>
  );
}

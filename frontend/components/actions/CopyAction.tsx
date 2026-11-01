"use client";

import { useState, type ReactNode } from "react";
import { IconCheck } from "@/components/icons";

interface CopyActionProps {
  text: string;
  label: string;
  copiedLabel: string;
  icon: ReactNode;
  className?: string;
}

// Copies a value (a link, a git command) and says so for two seconds, the check replacing the
// icon; screen readers hear the confirmation through the status region.
export function CopyAction({ text, label, copiedLabel, icon, className = "" }: CopyActionProps) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      // A browser refusing the clipboard leaves the button as it was.
    }
  }

  return (
    <button type="button" onClick={copy} className={className}>
      {copied ? <IconCheck aria-hidden="true" className="size-4 text-success" /> : icon}
      <span role="status">{copied ? copiedLabel : label}</span>
    </button>
  );
}

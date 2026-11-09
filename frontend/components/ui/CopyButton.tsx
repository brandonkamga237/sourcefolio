"use client";

import { useState } from "react";
import { IconCheck, IconCopy } from "@/components/icons";
import { toast } from "./Toast";

interface CopyButtonProps {
  value: string;
  label: string;
  copied: string;
  className?: string;
}

/** The copy icon turns into the check, and the toast confirms for those not looking at it. */
export function CopyButton({ value, label, copied, className = "" }: CopyButtonProps) {
  const [done, setDone] = useState(false);
  const icon = "col-start-1 row-start-1 size-4 transition-[opacity,transform] duration-(--dur-2) ease-enter";

  async function copy() {
    await navigator.clipboard.writeText(value);
    setDone(true);
    toast(copied);
    window.setTimeout(() => setDone(false), 1800);
  }

  return (
    <button type="button" onClick={() => void copy()} className={`btn btn-secondary ${className}`}>
      <span className="flex items-center gap-2">
        <span aria-hidden="true" className="grid">
          <IconCopy className={`${icon} ${done ? "scale-50 opacity-0" : ""}`} />
          <IconCheck className={`${icon} text-success ${done ? "" : "scale-50 opacity-0"}`} />
        </span>
        {label}
      </span>
    </button>
  );
}

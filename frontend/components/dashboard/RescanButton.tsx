"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { rescanMyRepositories } from "@/services/showcase-publication";

export interface RescanLabels {
  rescan: string;
  rescanning: string;
  failed: string;
}

interface RescanButtonProps {
  /** The last scan the page shows: the search is over once the server answers a newer one. */
  scannedAt: string | null;
  labels: RescanLabels;
  primary?: boolean;
}

const POLL_MS = 2500;
const MAX_POLLS = 12;

// The search runs in the background (a few seconds): the page asks the server again until
// scannedAt moves, then shows the new list. Bounded, so a stuck job never keeps a phone polling.
export function RescanButton({ scannedAt, labels, primary }: RescanButtonProps) {
  const router = useRouter();
  const [waitingFrom, setWaitingFrom] = useState<string | null | undefined>(undefined);
  const [error, setError] = useState<string | null>(null);
  const pending = waitingFrom !== undefined && waitingFrom === scannedAt;

  useEffect(() => {
    if (!pending) return;
    let polls = 0;
    const timer = window.setInterval(() => {
      polls += 1;
      router.refresh();
      if (polls >= MAX_POLLS) {
        window.clearInterval(timer);
        setWaitingFrom(undefined);
      }
    }, POLL_MS);
    return () => window.clearInterval(timer);
  }, [pending, router]);

  async function handleClick() {
    setError(null);
    const result = await rescanMyRepositories();
    if (result.status === 202) setWaitingFrom(scannedAt);
    else setError(result.message ?? labels.failed);
  }

  return (
    <div className="flex flex-col gap-1.5">
      <button
        type="button"
        onClick={handleClick}
        disabled={pending}
        aria-busy={pending}
        className={`press flex h-11 items-center justify-center gap-2 rounded-control px-4 text-sm font-semibold disabled:opacity-70 ${
          primary ? "bg-accent text-white" : "border border-line bg-surface text-ink"
        }`}
      >
        {pending ? <span aria-hidden="true" className="size-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" /> : null}
        {pending ? labels.rescanning : labels.rescan}
      </button>
      {error ? (
        <p role="alert" className="text-[13px] leading-normal text-ink-body">
          {error}
        </p>
      ) : null}
    </div>
  );
}

"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { fetchMyAnalysis, requestMyAnalysis } from "@/services/analysis-client";
import type { AnalysisStatus } from "@/services/contribution-types";

interface AnalysisWaiterProps {
  status: AnalysisStatus;
  /** True when a snapshot is shown and a new reading runs behind it. */
  refreshing: boolean;
  labels: { title: string; lead: string; failedTitle: string; failedLead: string; retry: string; refreshing: string };
}

const POLL_MS = 2_000;
const GIVE_UP_AFTER = 45;

/**
 * Waits for the reading of GitHub: asks for one when none exists, polls while it runs, then
 * re-renders the page on the server so the profile appears. While a snapshot is already shown,
 * it only says that a fresh reading is on its way.
 */
export function AnalysisWaiter({ status, refreshing, labels }: AnalysisWaiterProps) {
  const router = useRouter();
  // From the server's answer, not kept at mount: a reading that fails while the page is open
  // arrives through a refresh, which keeps this component's state.
  const [retrying, setRetrying] = useState(false);
  const failed = status === "FAILED" && !retrying;
  const asked = useRef(false);
  const waiting = status === "NONE" || status === "PENDING" || refreshing;

  useEffect(() => {
    if (!waiting) return;
    let polls = 0;
    let stopped = false;
    const tick = async () => {
      if (stopped) return;
      if (status === "NONE" && !asked.current) {
        asked.current = true;
        await requestMyAnalysis();
      }
      const result = await fetchMyAnalysis();
      polls += 1;
      const answer = result.data;
      if (answer && (answer.status === "READY" || answer.status === "FAILED") && !answer.refreshing) {
        router.refresh();
        return;
      }
      if (polls < GIVE_UP_AFTER) setTimeout(tick, POLL_MS);
    };
    const first = setTimeout(tick, status === "NONE" ? 0 : POLL_MS);
    return () => {
      stopped = true;
      clearTimeout(first);
    };
  }, [waiting, status, router]);

  async function retry() {
    setRetrying(true);
    await requestMyAnalysis();
    router.refresh();
    window.setTimeout(() => setRetrying(false), 3000);
  }

  if (failed) {
    return (
      <div role="alert" className="flex flex-col gap-3 rounded-card border border-line bg-surface p-5">
        <p className="text-[16px] font-semibold">{labels.failedTitle}</p>
        <p className="text-[14px] text-ink-muted">{labels.failedLead}</p>
        <button type="button" onClick={() => void retry()} className="press h-11 self-start rounded-control border border-line bg-surface-raised px-4 text-[14px] font-semibold">
          {labels.retry}
        </button>
      </div>
    );
  }

  if (refreshing && status === "READY") {
    return <p role="status" className="text-[13px] text-ink-muted">{labels.refreshing}</p>;
  }

  if (!waiting) return null;

  return (
    <div role="status" className="flex flex-col gap-4 rounded-card border border-line bg-surface p-5">
      <p className="text-[16px] font-semibold">{labels.title}</p>
      <p className="text-[14px] text-ink-muted">{labels.lead}</p>
      {/* A row of squares lighting up one after the other: the reading in progress. */}
      <div aria-hidden="true" className="flex gap-[3px]">
        {Array.from({ length: 28 }, (_, index) => (
          <span key={index} className="size-[11px] animate-pulse rounded-[2px] bg-level-2" style={{ animationDelay: `${index * 60}ms` }} />
        ))}
      </div>
    </div>
  );
}

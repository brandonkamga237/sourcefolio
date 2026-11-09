"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/Button";
import { fetchMyAnalysis, requestMyAnalysis } from "@/services/analysis-client";
import type { AnalysisStatus } from "@/services/contribution-types";

interface AnalysisWaiterProps {
  status: AnalysisStatus;
  /** True when a snapshot is shown and a new reading runs behind it. */
  refreshing: boolean;
  labels: { title: string; lead: string; failedTitle: string; failedLead: string; retry: string; refreshing: string; reads: string[] };
}

const POLL_MS = 2_000;
const GIVE_UP_AFTER = 45;
const SHADES = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;

/**
 * Waits for the reading of GitHub. The API only says whether it is done, so this shows what is
 * being read, in turn, without pretending to a percentage it does not have.
 */
export function AnalysisWaiter({ status, refreshing, labels }: AnalysisWaiterProps) {
  const router = useRouter();
  // From the server's answer, not kept at mount: a reading that fails while the page is open
  // arrives through a refresh, which keeps this component's state.
  const [retrying, setRetrying] = useState(false);
  const [reading, setReading] = useState(0);
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

  useEffect(() => {
    if (!waiting) return;
    const cycle = window.setInterval(() => setReading((value) => (value + 1) % labels.reads.length), 1400);
    return () => window.clearInterval(cycle);
  }, [waiting, labels.reads.length]);

  async function retry() {
    setRetrying(true);
    await requestMyAnalysis();
    router.refresh();
    window.setTimeout(() => setRetrying(false), 3000);
  }

  if (failed) {
    return (
      <div role="alert" className="flex max-w-xl flex-col items-start gap-3 py-8">
        <h1 className="t-h2">{labels.failedTitle}</h1>
        <p className="text-[16px] text-ink-soft">{labels.failedLead}</p>
        <Button tone="secondary" onClick={() => void retry()}>{labels.retry}</Button>
      </div>
    );
  }
  if (refreshing && status === "READY") return <p role="status" className="text-[13px] text-ink-muted">{labels.refreshing}</p>;
  if (!waiting) return null;

  return (
    <div role="status" className="mx-auto flex max-w-xl flex-col gap-8 py-10 lg:py-16">
      <div className="flex flex-col gap-2">
        <h1 className="t-h2">{labels.title}</h1>
        <p className="text-[16px] text-ink-soft">{labels.lead}</p>
      </div>
      <div aria-hidden="true" className="grid grid-cols-[repeat(26,minmax(0,1fr))] gap-[3px]">
        {Array.from({ length: 182 }, (_, index) => (
          <span key={index} className={`aspect-square rounded-[2px] ${SHADES[(index * 7) % 4]}`} style={{ animation: "square-pulse 1800ms var(--ease-enter) infinite", animationDelay: `${(Math.floor(index / 7) * 60 + (index % 7) * 30) % 1800}ms` }} />
        ))}
      </div>
      <ul className="flex flex-col gap-2 text-[15px]">
        {labels.reads.map((item, index) => (
          <li key={item} className={`flex items-center gap-2 transition-colors duration-(--dur-3) ${index === reading ? "font-medium text-ink" : "text-ink-muted"}`}>
            <span aria-hidden="true" className={`size-2 rounded-[2px] transition-colors duration-(--dur-3) ${index === reading ? "bg-level-3" : "bg-level-0"}`} />
            {item}
          </li>
        ))}
      </ul>
    </div>
  );
}

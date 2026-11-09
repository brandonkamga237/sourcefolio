"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button, type ButtonStatus } from "@/components/ui/Button";
import { requestMyAnalysis } from "@/services/analysis-client";
import { unpublishProfile } from "@/services/profile-answers";

interface DashboardActionsProps {
  labels: { unpublish: string; unpublished: string; refresh: string; refreshed: string; failed: string };
}

export function DashboardActions({ labels }: DashboardActionsProps) {
  const router = useRouter();
  const [refresh, setRefresh] = useState<ButtonStatus>("idle");
  const [hide, setHide] = useState<ButtonStatus>("idle");
  const [error, setError] = useState<string | null>(null);

  async function unpublish() {
    setHide("busy");
    setError(null);
    const result = await unpublishProfile();
    if (!result.data) {
      setHide("idle");
      setError(result.message ?? labels.failed);
      return;
    }
    setHide("done");
    router.refresh();
  }

  async function reread() {
    setRefresh("busy");
    await requestMyAnalysis();
    setRefresh("done");
    router.refresh();
    window.setTimeout(() => setRefresh("idle"), 2400);
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <Button tone="secondary" status={refresh} doneLabel={labels.refreshed} onClick={() => void reread()}>{labels.refresh}</Button>
      <Button tone="secondary" status={hide} doneLabel={labels.unpublished} onClick={() => void unpublish()}>{labels.unpublish}</Button>
      {error ? <p role="alert" className="text-[14px] text-danger">{error}</p> : null}
    </div>
  );
}

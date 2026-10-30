"use client";

import { useRouter } from "next/navigation";
import { ConfirmDialog, type ConfirmDialogLabels } from "@/components/ui/ConfirmDialog";
import { withdrawMyShowcase } from "@/services/showcase-publication";

export interface ManageShowcaseLabels {
  title: string;
  lead: string;
  change: string;
  withdraw: string;
  failed: string;
  changeDialog: ConfirmDialogLabels;
  withdrawDialog: ConfirmDialogLabels;
}

interface ManageShowcaseProps {
  labels: ManageShowcaseLabels;
}

const action = "press flex h-11 items-center justify-center rounded-control border px-4 text-sm font-semibold";

// Changing portfolio is taking this one down, then picking another repository among the
// candidates that Me shows as soon as there is no portfolio (spec: a new repository is a new
// portfolio). Both ask first, since the likes go with it.
export function ManageShowcase({ labels }: ManageShowcaseProps) {
  const router = useRouter();

  async function withdraw(thenPick: boolean): Promise<string | null> {
    const result = await withdrawMyShowcase();
    if (result.status !== 204) return result.message ?? labels.failed;
    router.refresh();
    if (thenPick) router.replace("#me-portfolio", { scroll: true });
    return null;
  }

  return (
    <details className="group/manage rounded-card border border-line bg-surface [&_summary::-webkit-details-marker]:hidden">
      <summary className="flex h-12 cursor-pointer list-none items-center justify-between gap-3 px-4 text-sm font-semibold select-none">
        {labels.title}
        <span aria-hidden="true" className="size-2 rotate-45 border-r-2 border-b-2 border-ink-muted transition-transform duration-200 group-open/manage:-rotate-135" />
      </summary>
      <div className="flex flex-col gap-3 border-t border-line-soft p-4">
        <p className="text-[13px] leading-normal text-ink-body">{labels.lead}</p>
        <div className="flex flex-wrap gap-2">
          <ConfirmDialog
            labels={labels.changeDialog}
            onConfirm={() => withdraw(true)}
            trigger={
              <button type="button" className={`${action} border-ink text-ink`}>
                {labels.change}
              </button>
            }
          />
          <ConfirmDialog
            danger
            labels={labels.withdrawDialog}
            onConfirm={() => withdraw(false)}
            trigger={
              <button type="button" className={`${action} border-line text-danger`}>
                {labels.withdraw}
              </button>
            }
          />
        </div>
      </div>
    </details>
  );
}

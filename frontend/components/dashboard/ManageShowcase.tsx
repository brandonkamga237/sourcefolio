"use client";

import { useRouter } from "next/navigation";
import { ConfirmDialog, type ConfirmDialogLabels } from "@/components/ui/ConfirmDialog";
import { rescanMyRepositories, withdrawMyShowcase } from "@/services/showcase-publication";

export interface ManageShowcaseLabels {
  change: string;
  withdraw: string;
  failed: string;
  changeDialog: ConfirmDialogLabels;
  withdrawDialog: ConfirmDialogLabels;
}

interface ManageShowcaseProps {
  labels: ManageShowcaseLabels;
}

const action = "press flex h-11 items-center justify-center rounded-control px-3 text-sm font-semibold transition-colors duration-150";

// Changing portfolio is taking this one down, then picking another repository among the
// candidates that Me shows as soon as there is no portfolio (spec: a new repository is a new
// portfolio). A new search starts with it, so the list is not the one from sign-in. Both ask
// first, since the likes go with it.
export function ManageShowcase({ labels }: ManageShowcaseProps) {
  const router = useRouter();

  async function withdraw(thenPick: boolean): Promise<string | null> {
    const result = await withdrawMyShowcase();
    if (result.status !== 204) return result.message ?? labels.failed;
    // A refusal only means a search just ran: the list is fresh enough.
    if (thenPick) await rescanMyRepositories();
    router.refresh();
    if (thenPick) router.replace("#me-portfolio", { scroll: true });
    return null;
  }

  return (
    <div className="-mx-3 flex flex-wrap gap-1">
      <ConfirmDialog
        labels={labels.changeDialog}
        onConfirm={() => withdraw(true)}
        trigger={
          <button type="button" className={`${action} text-accent-ink hover:bg-accent-soft`}>
            {labels.change}
          </button>
        }
      />
      <ConfirmDialog
        danger
        labels={labels.withdrawDialog}
        onConfirm={() => withdraw(false)}
        trigger={
          <button type="button" className={`${action} text-ink-muted hover:bg-surface-sunken hover:text-danger`}>
            {labels.withdraw}
          </button>
        }
      />
    </div>
  );
}

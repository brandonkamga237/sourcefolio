"use client";

import { ConfirmDialog, type ConfirmDialogLabels } from "@/components/ui/ConfirmDialog";
import { deleteMyAccount } from "@/services/account";

interface DeleteAccountProps {
  login: string;
  /** Where the member lands once the account is gone. */
  homeHref: string;
  label: string;
  failed: string;
  dialog: ConfirmDialogLabels;
}

// Cannot be undone, so the member types their login before the button wakes up.
export function DeleteAccount({ login, homeHref, label, failed, dialog }: DeleteAccountProps) {
  async function remove(): Promise<string | null> {
    const result = await deleteMyAccount();
    if (result.status !== 204) return result.message ?? failed;
    // A full load: the session is gone, every cached screen must be read again as a visitor.
    window.location.assign(homeHref);
    return null;
  }

  return (
    <ConfirmDialog
      danger
      word={login}
      labels={dialog}
      onConfirm={remove}
      trigger={
        <button type="button" className="press flex h-11 items-center justify-center rounded-control px-4 text-sm font-semibold text-danger hover:bg-danger-soft">
          {label}
        </button>
      }
    />
  );
}

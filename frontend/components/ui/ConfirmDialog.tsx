"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { useState, type ReactNode } from "react";

export interface ConfirmDialogLabels {
  title: string;
  description: string;
  confirm: string;
  pending: string;
  cancel: string;
  /** Asked when a word must be typed before confirming; contains {word}. */
  typeToConfirm?: string;
}

interface ConfirmDialogProps {
  trigger: ReactNode;
  labels: ConfirmDialogLabels;
  /** Resolves to an error message to show, or null once done. */
  onConfirm: () => Promise<string | null>;
  /** A destructive action: the confirm button turns red. */
  danger?: boolean;
  /** For what cannot be undone: the member types this word before the button wakes up. */
  word?: string;
}

// The one confirmation primitive: a sheet from the bottom on a phone (spec), a centred box on
// a wide screen. Radix keeps focus inside, closes on Escape and gives back focus to the trigger.
export function ConfirmDialog({ trigger, labels, onConfirm, danger, word }: ConfirmDialogProps) {
  const [open, setOpen] = useState(false);
  const [typed, setTyped] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const ready = !word || typed.trim().toLowerCase() === word.toLowerCase();

  async function confirm() {
    setPending(true);
    setError(null);
    const failure = await onConfirm();
    setPending(false);
    if (failure) setError(failure);
    else setOpen(false);
  }

  return (
    <Dialog.Root
      open={open}
      onOpenChange={(next) => {
        if (pending) return;
        setOpen(next);
        setTyped("");
        setError(null);
      }}
    >
      <Dialog.Trigger asChild>{trigger}</Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-ink/40 backdrop-blur-[2px] data-[state=open]:animate-[fade-in_200ms_ease-out]" />
        <Dialog.Content className="fixed inset-x-0 bottom-0 z-50 flex flex-col gap-4 rounded-t-panel border border-line bg-surface p-5 pb-[calc(1.25rem+env(safe-area-inset-bottom))] shadow-[0_-12px_40px_-12px_rgba(18,20,18,0.3)] data-[state=open]:animate-[sheet-up_320ms_var(--ease-out-soft)] sm:inset-x-auto sm:top-1/2 sm:bottom-auto sm:left-1/2 sm:w-[440px] sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-panel sm:pb-5 sm:data-[state=open]:animate-[fade-in_200ms_ease-out]">
          <span aria-hidden="true" className="mx-auto h-1 w-10 rounded-full bg-line sm:hidden" />
          <div className="flex flex-col gap-2">
            <Dialog.Title className="font-display text-[22px] leading-tight font-bold tracking-[-0.025em]">{labels.title}</Dialog.Title>
            <Dialog.Description className="text-sm leading-normal text-ink-body">{labels.description}</Dialog.Description>
          </div>
          {word && labels.typeToConfirm ? (
            <label className="flex flex-col gap-2 text-[13px] text-ink-muted">
              {labels.typeToConfirm.replace("{word}", word)}
              <input
                value={typed}
                onChange={(event) => setTyped(event.target.value)}
                autoComplete="off"
                autoCapitalize="none"
                spellCheck={false}
                className="h-12 rounded-control border border-line bg-surface px-4 font-mono text-base text-ink"
              />
            </label>
          ) : null}
          {error ? (
            <p role="alert" className="text-[13px] leading-normal text-danger">
              {error}
            </p>
          ) : null}
          <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <Dialog.Close asChild>
              <button type="button" disabled={pending} className="press flex h-11 items-center justify-center rounded-control border border-line px-4 text-sm font-semibold text-ink">
                {labels.cancel}
              </button>
            </Dialog.Close>
            <button
              type="button"
              onClick={confirm}
              disabled={!ready || pending}
              aria-busy={pending}
              className={`press flex h-11 items-center justify-center rounded-control px-4 text-sm font-semibold disabled:opacity-50 ${danger ? "bg-danger text-ink-inverse" : "bg-accent text-white"}`}
            >
              {pending ? labels.pending : labels.confirm}
            </button>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}

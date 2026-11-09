"use client";

import { useSyncExternalStore } from "react";
import { IconCheck } from "@/components/icons";

let message: { id: number; text: string } | null = null;
let timer = 0;
let nextId = 0;
const listeners = new Set<() => void>();
const publish = () => listeners.forEach((listener) => listener());

export function toast(text: string): void {
  message = { id: ++nextId, text };
  publish();
  window.clearTimeout(timer);
  timer = window.setTimeout(() => {
    message = null;
    publish();
  }, 2400);
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function Toaster() {
  const current = useSyncExternalStore(subscribe, () => message, () => null);
  return (
    <div role="status" aria-live="polite" className="pointer-events-none fixed inset-x-0 bottom-[calc(5rem+env(safe-area-inset-bottom))] z-[60] flex justify-center px-4 lg:bottom-8">
      {current ? (
        <p key={current.id} className="flex items-center gap-2 rounded-card border border-line bg-surface px-4 py-3 text-[14px] font-medium shadow-[var(--shadow-float)] animate-[toast-in_var(--dur-3)_var(--ease-enter)]">
          <IconCheck aria-hidden="true" className="size-4 text-success" />
          {current.text}
        </p>
      ) : null}
    </div>
  );
}

"use client";

import { useState } from "react";
import { signOut } from "@/services/auth";

interface SignOutButtonProps {
  label: string;
  failedMessage: string;
}

export function SignOutButton({ label, failedMessage }: SignOutButtonProps) {
  const [pending, setPending] = useState(false);
  const [failed, setFailed] = useState(false);

  async function handleClick() {
    setPending(true);
    setFailed(false);
    if (await signOut()) {
      window.location.reload();
      return;
    }
    setPending(false);
    setFailed(true);
  }

  return (
    <div className="flex flex-col gap-2">
      <button
        type="button"
        onClick={handleClick}
        disabled={pending}
        className="flex h-11 items-center justify-center rounded-control border border-ink px-5 text-sm font-semibold text-ink disabled:opacity-60"
      >
        {label}
      </button>
      {failed ? (
        <p role="alert" className="text-[13px] text-ink-muted">
          {failedMessage}
        </p>
      ) : null}
    </div>
  );
}

"use client";

import { useState } from "react";

interface SignOutButtonProps {
  label: string;
  failedMessage: string;
}

const CSRF_COOKIE = "XSRF-TOKEN";

function readCsrfToken(): string | undefined {
  return document.cookie
    .split("; ")
    .find((entry) => entry.startsWith(`${CSRF_COOKIE}=`))
    ?.slice(CSRF_COOKIE.length + 1);
}

// The API sets the XSRF-TOKEN cookie on any GET it answers. A visitor who reached this page
// through server rendering may not have it yet: one GET first, then the protected POST.
async function signOut(): Promise<boolean> {
  if (!readCsrfToken()) {
    await fetch("/api/me", { credentials: "same-origin" });
  }
  const response = await fetch("/api/auth/logout", {
    method: "POST",
    credentials: "same-origin",
    headers: { "X-XSRF-TOKEN": decodeURIComponent(readCsrfToken() ?? "") },
  });
  return response.status === 204;
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

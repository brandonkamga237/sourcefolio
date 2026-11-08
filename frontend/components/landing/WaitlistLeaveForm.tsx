"use client";

import { useState, type FormEvent } from "react";
import { leaveWaitlist } from "@/services/waitlist";

interface WaitlistLeaveFormProps {
  labels: {
    summary: string;
    email: string;
    submit: string;
    sending: string;
    done: string;
    failed: string;
  };
}

/** Folded: few visitors need it, and the page's one main action is to sign up. */
export function WaitlistLeaveForm({ labels }: WaitlistLeaveFormProps) {
  const [email, setEmail] = useState("");
  const [pending, setPending] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setPending(true);
    setError(null);
    const result = await leaveWaitlist(email);
    setPending(false);
    if (result.status === 204) setDone(true);
    else setError(result.message ?? labels.failed);
  }

  return (
    <details className="text-[15px]">
      <summary className="flex min-h-11 cursor-pointer items-center text-ink-muted hover:text-ink">{labels.summary}</summary>
      {done ? (
        <p role="status" className="py-2 text-ink-soft">{labels.done}</p>
      ) : (
        <form onSubmit={submit} className="flex flex-col gap-2 pt-1 sm:flex-row sm:items-end">
          <label className="flex min-w-0 flex-col gap-1.5 sm:flex-1">
            <span className="text-[13px] text-ink-muted">{labels.email}</span>
            <input
              type="email"
              required
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="field"
            />
          </label>
          <button type="submit" disabled={pending} className="btn btn-secondary">
            {pending ? labels.sending : labels.submit}
          </button>
          {error ? <p role="alert" className="text-danger">{error}</p> : null}
        </form>
      )}
    </details>
  );
}

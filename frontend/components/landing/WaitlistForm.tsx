"use client";

import { useState, type FormEvent } from "react";
import { joinWaitlist } from "@/services/waitlist";

interface WaitlistFormProps {
  labels: {
    email: string;
    placeholder: string;
    consent: string;
    submit: string;
    sending: string;
    done: string;
    failed: string;
  };
}

export function WaitlistForm({ labels }: WaitlistFormProps) {
  const [email, setEmail] = useState("");
  const [consent, setConsent] = useState(false);
  const [pending, setPending] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setPending(true);
    setError(null);
    const result = await joinWaitlist(email, consent);
    setPending(false);
    if (result.status === 204) setDone(true);
    else setError(result.message ?? labels.failed);
  }

  if (done) {
    return <p role="status" className="rounded-card border border-line bg-surface p-4 text-[15px] font-semibold">{labels.done}</p>;
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <label className="flex flex-col gap-1.5">
        <span className="text-[13px] font-semibold">{labels.email}</span>
        <input
          type="email"
          required
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder={labels.placeholder}
          className="h-12 rounded-control border border-line bg-surface px-3 text-[16px] focus-visible:outline-2 focus-visible:outline-accent"
        />
      </label>
      <label className="flex min-h-11 items-start gap-3 text-[14px] leading-snug text-ink-soft">
        <input type="checkbox" required checked={consent} onChange={(event) => setConsent(event.target.checked)} className="mt-0.5 size-5 shrink-0 accent-[var(--color-cta)]" />
        <span>{labels.consent}</span>
      </label>
      <button type="submit" disabled={pending} className="press flex h-12 items-center justify-center rounded-control bg-cta px-5 text-[15px] font-semibold text-cta-ink hover:bg-cta-hover disabled:opacity-70">
        {pending ? labels.sending : labels.submit}
      </button>
      {error ? <p role="alert" className="text-[14px] text-danger">{error}</p> : null}
    </form>
  );
}

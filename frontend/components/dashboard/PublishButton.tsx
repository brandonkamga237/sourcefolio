"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { publishMyShowcase } from "@/services/showcase-publication";

interface PublishButtonProps {
  repository: string;
  label: string;
  pendingLabel: string;
  failedMessage: string;
  primary?: boolean;
}

// The API checks the site before answering, so the wait is told ("Checking your site…").
// On success the page is rendered again by the server and shows the published portfolio.
export function PublishButton({ repository, label, pendingLabel, failedMessage, primary }: PublishButtonProps) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleClick() {
    setPending(true);
    setError(null);
    const result = await publishMyShowcase(repository);
    if (result.status === 201) {
      router.refresh();
      return;
    }
    setPending(false);
    setError(result.message ?? failedMessage);
  }

  return (
    <div className="flex flex-col gap-2">
      <button
        type="button"
        onClick={handleClick}
        disabled={pending}
        aria-busy={pending}
        className={`flex h-11 items-center justify-center rounded-control px-5 text-sm font-semibold disabled:opacity-70 ${
          primary ? "bg-accent text-white" : "border border-ink text-ink"
        }`}
      >
        {pending ? pendingLabel : label}
      </button>
      {error ? (
        <p role="alert" className="text-[13px] leading-normal text-ink-body">
          {error}
        </p>
      ) : null}
    </div>
  );
}

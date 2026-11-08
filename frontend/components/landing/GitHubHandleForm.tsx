"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { checkPublicProfile } from "@/services/public-profiles-client";

interface GitHubHandleFormProps {
  locale: Locale;
  labels: {
    field: string;
    placeholder: string;
    submit: string;
    checking: string;
    invalid: string;
    missing: string;
    signIn: string;
    failed: string;
  };
}

type State = "idle" | "checking" | "invalid" | "missing" | "failed";

const LOGIN = /^[a-z\d](?:[a-z\d-]{0,38})$/i;

// People paste the whole address as often as they type the name.
function loginFrom(raw: string): string {
  const path = raw.trim().replace(/^https?:\/\//i, "").replace(/^(www\.)?github\.com\//i, "").replace(/^@/, "");
  return path.split(/[/?#]/)[0];
}

export function GitHubHandleForm({ locale, labels }: GitHubHandleFormProps) {
  const router = useRouter();
  const [value, setValue] = useState("");
  const [state, setState] = useState<State>("idle");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const login = loginFrom(value);
    if (!LOGIN.test(login)) {
      setState("invalid");
      return;
    }
    setState("checking");
    const status = await checkPublicProfile(login);
    if (status === 200) router.push(localizedPath(locale, `/u/${login}`));
    else setState(status === 404 ? "missing" : "failed");
  }

  const returnTo = encodeURIComponent(localizedPath(locale, "/me"));
  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <div className="flex flex-col gap-2 sm:flex-row">
        <label className="flex h-12 w-full min-w-0 items-center sm:flex-1 rounded-control border border-line bg-surface px-3 focus-within:outline-2 focus-within:outline-accent">
          <span className="sr-only">{labels.field}</span>
          <span aria-hidden="true" className="font-mono text-[14px] text-ink-muted">github.com/</span>
          <input
            value={value}
            onChange={(event) => {
              setValue(event.target.value);
              setState("idle");
            }}
            placeholder={labels.placeholder}
            autoCapitalize="none"
            autoCorrect="off"
            spellCheck={false}
            className="h-full min-w-0 flex-1 bg-transparent font-mono text-[16px] outline-none"
          />
        </label>
        <button type="submit" disabled={state === "checking"} className="press flex h-12 items-center justify-center rounded-control bg-cta px-5 text-[15px] font-semibold text-cta-ink hover:bg-cta-hover disabled:opacity-70">
          {state === "checking" ? labels.checking : labels.submit}
        </button>
      </div>
      <div aria-live="polite">
        {state === "invalid" ? <p className="text-[14px] text-danger">{labels.invalid}</p> : null}
        {state === "failed" ? <p className="text-[14px] text-danger">{labels.failed}</p> : null}
        {state === "missing" ? (
          <p className="flex flex-wrap items-center gap-x-2 text-[14px] text-ink-soft">
            {labels.missing}
            <a href={`/oauth2/authorization/github?returnTo=${returnTo}`} className="flex min-h-11 items-center font-semibold text-accent hover:underline">
              {labels.signIn}
            </a>
          </p>
        ) : null}
      </div>
    </form>
  );
}

"use client";

import { useEffect, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { IconCheck } from "@/components/icons";
import { Avatar } from "@/components/members/Avatar";
import { Button } from "@/components/ui/Button";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { EXAMPLE_LOGIN } from "@/lib/examples";
import type { ProfileCard } from "@/services/contribution-types";
import { fetchPublicProfile } from "@/services/public-profiles-client";

export interface HandleLabels {
  field: string;
  placeholder: string;
  submit: string;
  invalid: string;
  missing: string;
  signIn: string;
  failed: string;
}

interface HandleFieldProps {
  locale: Locale;
  labels: HandleLabels;
  initial?: string;
  /** Builds the card in place; without it, a found profile opens its page. */
  onFound?: (card: ProfileCard) => void;
  /** Asked when the example login is submitted again. */
  onExample?: () => void;
}

const LOGIN = /^[a-z\d](?:[a-z\d-]{0,38})$/i;

// People paste the whole address as often as they type the name.
function loginFrom(raw: string): string {
  return raw.trim().replace(/^https?:\/\//i, "").replace(/^(www\.)?github\.com\//i, "").replace(/^@/, "").split(/[/?#]/)[0];
}

type Lookup = { login: string; status: number; card: ProfileCard | null };

export function HandleField({ locale, labels, initial = "", onFound, onExample }: HandleFieldProps) {
  const router = useRouter();
  const [value, setValue] = useState(initial);
  const [lookup, setLookup] = useState<Lookup | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [busy, setBusy] = useState(false);

  const login = loginFrom(value);
  const wellFormed = LOGIN.test(login);
  const isExample = login === EXAMPLE_LOGIN;
  const known = lookup?.login === login ? lookup : null;
  const found = known?.status === 200;

  useEffect(() => {
    if (!wellFormed || isExample) return;
    let stale = false;
    const timer = window.setTimeout(() => {
      void fetchPublicProfile(login).then((result) => {
        if (!stale) setLookup({ login, status: result.status, card: result.data });
      });
    }, 350);
    return () => {
      stale = true;
      window.clearTimeout(timer);
    };
  }, [login, wellFormed, isExample]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitted(true);
    if (!wellFormed) return;
    if (isExample) {
      onExample?.();
      return;
    }
    let result = known;
    if (!result) {
      setBusy(true);
      const answer = await fetchPublicProfile(login);
      setBusy(false);
      result = { login, status: answer.status, card: answer.data };
      setLookup(result);
    }
    if (result.status !== 200 || !result.card) return;
    if (onFound) onFound(result.card);
    else router.push(localizedPath(locale, `/u/${login}`));
  }

  const message = !submitted ? null : !wellFormed ? labels.invalid : known && known.status !== 200 && known.status !== 404 ? labels.failed : null;
  const missing = known?.status === 404 && !isExample;

  return (
    <form onSubmit={submit} className="flex flex-col gap-2" noValidate>
      <div className="flex flex-col gap-2 sm:flex-row">
        <label className="flex h-11 min-w-0 flex-1 items-center gap-2 rounded-control border border-line bg-surface px-3 transition-[border-color,box-shadow] duration-(--dur-1) focus-within:border-accent focus-within:shadow-[0_0_0_3px_var(--color-accent-soft)]">
          <span className="sr-only">{labels.field}</span>
          {found ? <span className="animate-[toast-in_var(--dur-2)_var(--ease-enter)]"><Avatar login={login} size={22} /></span> : null}
          <span aria-hidden="true" className="font-mono text-[14px] text-ink-muted">github.com/</span>
          <input value={value} onChange={(event) => { setValue(event.target.value); setSubmitted(false); }} placeholder={labels.placeholder} autoCapitalize="none" autoCorrect="off" spellCheck={false} className="h-full min-w-0 flex-1 bg-transparent font-mono text-[16px] outline-none" />
          {found ? <IconCheck aria-hidden="true" className="size-4 shrink-0 text-success" /> : null}
        </label>
        <Button type="submit" status={busy ? "busy" : "idle"}>{labels.submit}</Button>
      </div>
      <div aria-live="polite" className="min-h-5 text-[13px]">
        {message ? <p className="text-danger">{message}</p> : null}
        {missing ? (
          <p className="flex flex-wrap items-center gap-x-2 text-ink-soft">
            {labels.missing}
            <a href={`/oauth2/authorization/github?returnTo=${encodeURIComponent(localizedPath(locale, "/me"))}`} className="flex min-h-11 items-center font-semibold text-accent sm:min-h-0">{labels.signIn}</a>
          </p>
        ) : null}
      </div>
    </form>
  );
}

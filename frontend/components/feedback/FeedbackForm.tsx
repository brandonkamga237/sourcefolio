"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useId, useRef, useState } from "react";
import { plural } from "@/lib/plural";
import { FEEDBACK_ANGLES, FEEDBACK_MAX, FEEDBACK_MIN, postFeedback, type FeedbackAngle } from "@/services/feedback";
import { fetchSignedIn, sessionKey } from "@/services/session";
import type { FeedbackLabels } from "./types";

interface FeedbackFormProps {
  slug: string;
  locale: string;
  labels: FeedbackLabels;
  onPosted: () => void;
}

interface Draft {
  angle: FeedbackAngle | null;
  body: string;
}

const AUTO_SEND = "feedback";
const draftKey = (slug: string) => `feedback-draft:${slug}`;

// Session storage can be full, disabled or private: the draft is then simply not kept.
function readDraft(slug: string): Draft | null {
  try {
    const raw = window.sessionStorage.getItem(draftKey(slug));
    return raw ? (JSON.parse(raw) as Draft) : null;
  } catch {
    return null;
  }
}

function writeDraft(slug: string, draft: Draft | null) {
  try {
    if (draft) window.sessionStorage.setItem(draftKey(slug), JSON.stringify(draft));
    else window.sessionStorage.removeItem(draftKey(slug));
  } catch {
    // Nothing to do: the visitor retypes after signing in.
  }
}

// Mounted only in the browser, once the page knows the reader is not the author, so a draft
// kept across GitHub sign-in can seed the state without a hydration mismatch. A visitor who
// sends is signed in and brought back with ?feedback=1, where the draft is sent once, as the
// spec asks of every action started before signing in.
export function FeedbackForm({ slug, locale, labels, onPosted }: FeedbackFormProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [angle, setAngle] = useState<FeedbackAngle | null>(() => readDraft(slug)?.angle ?? null);
  const [body, setBody] = useState(() => readDraft(slug)?.body ?? "");
  const [status, setStatus] = useState<{ tone: "done" | "error"; text: string } | null>(null);
  const autoSent = useRef(false);
  const formRef = useRef<HTMLFormElement>(null);
  const bodyId = useId();
  const { data: signedIn } = useQuery({ queryKey: sessionKey, queryFn: fetchSignedIn });

  const mutation = useMutation({
    mutationFn: async (draft: Draft) => {
      const result = await postFeedback(slug, { ...draft, body: draft.body.trim(), language: locale });
      if (result.status === 401) {
        writeDraft(slug, draft);
        // A full navigation on purpose: sign-in is a backend route, not a Next.js page.
        // eslint-disable-next-line @next/next/no-location-assign-relative-destination
        window.location.assign(`/oauth2/authorization/github?returnTo=${encodeURIComponent(`${pathname}?${AUTO_SEND}=1`)}`);
        throw new Error("signing in");
      }
      if (!result.data) throw new Error(result.message ?? labels.failed);
      return result.data;
    },
    onMutate: () => setStatus(null),
    onSuccess: () => {
      writeDraft(slug, null);
      setAngle(null);
      setBody("");
      setStatus({ tone: "done", text: labels.posted });
      onPosted();
    },
    onError: (failure) => {
      if (failure.message !== "signing in") setStatus({ tone: "error", text: failure.message });
    },
  });

  useEffect(() => {
    if (autoSent.current || searchParams.get(AUTO_SEND) !== "1") return;
    autoSent.current = true;
    router.replace(pathname, { scroll: false });
    formRef.current?.scrollIntoView({ block: "center" });
    const draft = readDraft(slug);
    if (draft && draft.body.trim().length >= FEEDBACK_MIN) mutation.mutate(draft);
  }, [mutation, pathname, router, searchParams, slug]);

  const length = body.trim().length;
  const missing = FEEDBACK_MIN - length;
  const number = new Intl.NumberFormat(locale);

  return (
    <form
      ref={formRef}
      onSubmit={(event) => {
        event.preventDefault();
        if (missing <= 0 && !mutation.isPending) mutation.mutate({ angle, body });
      }}
      className="flex flex-col gap-3"
    >
      <fieldset className="flex flex-col gap-2">
        <legend className="mb-2 text-[13px] font-semibold text-ink-body">{labels.angleLabel}</legend>
        <div className="flex flex-wrap gap-2">
          {FEEDBACK_ANGLES.map((option) => (
            <button
              key={option}
              type="button"
              aria-pressed={angle === option}
              onClick={() => setAngle(angle === option ? null : option)}
              className={`press h-11 rounded-full border px-4 text-sm font-semibold transition-colors duration-150 ${
                angle === option ? "border-accent bg-accent-soft text-accent-ink" : "border-line bg-surface text-ink-body hover:border-ink-muted"
              }`}
            >
              {labels.angles[option]}
            </button>
          ))}
        </div>
      </fieldset>

      <div className="flex flex-col gap-1.5">
        <label htmlFor={bodyId} className="text-[13px] font-semibold text-ink-body">
          {labels.bodyLabel}
        </label>
        <textarea
          id={bodyId}
          value={body}
          onChange={(event) => setBody(event.target.value)}
          maxLength={FEEDBACK_MAX}
          rows={4}
          placeholder={labels.placeholder}
          aria-describedby={`${bodyId}-count`}
          className="min-h-28 resize-y rounded-control border border-line bg-surface px-3.5 py-3 text-base leading-normal text-ink placeholder:text-ink-muted focus:border-accent"
        />
        <p id={`${bodyId}-count`} className="flex justify-between gap-3 text-xs text-ink-muted">
          <span>{missing > 0 && length > 0 ? plural(labels.missing, missing, locale) : ""}</span>
          <span className="font-mono tabular-nums">
            {labels.counter.replace("{count}", number.format(body.length)).replace("{max}", number.format(FEEDBACK_MAX))}
          </span>
        </p>
      </div>

      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <button
          type="submit"
          disabled={missing > 0 || mutation.isPending}
          className="press h-12 rounded-control bg-accent px-5 text-[15px] font-semibold text-white transition-opacity duration-150 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {mutation.isPending ? labels.sending : labels.submit}
        </button>
        {signedIn === false ? <p className="text-xs text-ink-muted">{labels.signInHint}</p> : null}
      </div>

      {status ? (
        <p role={status.tone === "error" ? "alert" : "status"} className={`text-sm ${status.tone === "error" ? "text-danger" : "text-ink-body"}`}>
          {status.text}
        </p>
      ) : null}
    </form>
  );
}

"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button, type ButtonStatus } from "@/components/ui/Button";
import type { ClientResult } from "@/lib/api/client";
import { SECTORS } from "@/lib/globe";
import { answerBio, answerCity, answerCountry, answerLinkedIn, answerSector, publishProfile } from "@/services/profile-answers";
import type { Profile } from "@/services/profile-types";
import { MAX_BIO, MAX_CITY } from "@/services/profile-types";

const STEPS = ["country", "city", "bio", "linkedin", "sector", "publish"] as const;
type Step = (typeof STEPS)[number];

interface CompletionStepsProps {
  initial: { country: string | null; city: string | null; bio: string | null; linkedIn: string | null; sector: string | null };
  githubBio: string | null;
  countries: { code: string; name: string }[];
  labels: {
    continue: string; back: string; skip: string; failed: string; progress: string;
    country: string; countryHint: string; countryPlaceholder: string;
    city: string; cityHint: string;
    bio: string; bioHint: string;
    linkedIn: string; linkedInHint: string;
    sector: string; sectorHint: string; sectorPlaceholder: string; sectors: Record<string, string>;
    publishTitle: string; publishLead: string; publish: string; published: string;
  };
}

function firstOpen(initial: CompletionStepsProps["initial"]): number {
  if (!initial.country) return 0;
  if (initial.city === null) return 1;
  if (initial.bio === null) return 2;
  if (initial.linkedIn === null) return 3;
  if (initial.sector === null) return 4;
  return 5;
}

/** One question a screen. Each answer is saved when the member moves on, and the card beside it follows. */
export function CompletionSteps({ initial, githubBio, countries, labels }: CompletionStepsProps) {
  const router = useRouter();
  const [index, setIndex] = useState(() => firstOpen(initial));
  const [values, setValues] = useState({ country: initial.country ?? "", city: initial.city ?? "", bio: initial.bio ?? githubBio ?? "", linkedin: initial.linkedIn ?? "", sector: initial.sector ?? "" });
  const [status, setStatus] = useState<ButtonStatus>("idle");
  const [error, setError] = useState<string | null>(null);
  const [wave, setWave] = useState(false);
  const step: Step = STEPS[index];

  const save: Record<Exclude<Step, "publish">, () => Promise<ClientResult<Profile>>> = {
    country: () => answerCountry(values.country),
    city: () => answerCity(values.city || null),
    bio: () => answerBio(values.bio || null),
    linkedin: () => answerLinkedIn(values.linkedin || null),
    sector: () => answerSector(values.sector),
  };
  const answered = step !== "publish" && values[step] !== "";

  async function next() {
    if (step === "publish") return;
    if (answered) {
      setStatus("busy");
      setError(null);
      const result = await save[step]();
      setStatus("idle");
      if (!result.data) {
        setError(result.message ?? labels.failed);
        return;
      }
      router.refresh();
    }
    setIndex((value) => value + 1);
  }

  async function publish() {
    setStatus("busy");
    setError(null);
    const result = await publishProfile();
    if (!result.data) {
      setStatus("idle");
      setError(result.message ?? labels.failed);
      return;
    }
    setStatus("done");
    setWave(true);
    window.setTimeout(() => router.refresh(), 1500);
  }

  const set = (key: keyof typeof values) => (event: { target: { value: string } }) => setValues((current) => ({ ...current, [key]: event.target.value }));
  const title = "t-h3";
  const hint = "text-[14px] text-ink-muted";

  return (
    <div className="flex flex-col gap-6">
      <ol aria-label={labels.progress} className="flex gap-1">
        {STEPS.map((name, position) => <li key={name} aria-current={position === index ? "step" : undefined} className={`h-2 flex-1 rounded-[2px] transition-colors duration-(--dur-3) ${position < index ? "bg-level-3" : position === index ? "bg-level-1" : "bg-level-0"}`} />)}
      </ol>

      <div key={step} className="flex min-h-40 flex-col gap-2 animate-[toast-in_var(--dur-3)_var(--ease-enter)]">
        {step === "country" ? (
          <>
            <label htmlFor="country" className={title}>{labels.country}</label>
            <select id="country" value={values.country} onChange={set("country")} className="field">
              <option value="" disabled>{labels.countryPlaceholder}</option>
              {countries.map((option) => <option key={option.code} value={option.code}>{option.name}</option>)}
            </select>
            <p className={hint}>{labels.countryHint}</p>
          </>
        ) : null}
        {step === "city" ? (
          <>
            <label htmlFor="city" className={title}>{labels.city}</label>
            <input id="city" value={values.city} onChange={set("city")} maxLength={MAX_CITY} autoComplete="address-level2" className="field" />
            <p className={hint}>{labels.cityHint}</p>
          </>
        ) : null}
        {step === "bio" ? (
          <>
            <label htmlFor="bio" className={title}>{labels.bio}</label>
            <textarea id="bio" rows={3} value={values.bio} onChange={set("bio")} maxLength={MAX_BIO} className="field resize-y py-2" />
            <p className={hint}>{labels.bioHint.replace("{count}", String(values.bio.length))}</p>
          </>
        ) : null}
        {step === "linkedin" ? (
          <>
            <label htmlFor="linkedin" className={title}>{labels.linkedIn}</label>
            <div className="flex">
              <span className="flex items-center rounded-l-control border border-r-0 border-line bg-surface-raised px-3 font-mono text-[13px] text-ink-muted">linkedin.com/in/</span>
              <input id="linkedin" value={values.linkedin} onChange={set("linkedin")} autoCapitalize="none" autoCorrect="off" className="field rounded-l-none" />
            </div>
            <p className={hint}>{labels.linkedInHint}</p>
          </>
        ) : null}
        {step === "sector" ? (
          <>
            <label htmlFor="sector" className={title}>{labels.sector}</label>
            <select id="sector" value={values.sector} onChange={set("sector")} className="field">
              <option value="">{labels.sectorPlaceholder}</option>
              {SECTORS.map((sector) => <option key={sector} value={sector}>{labels.sectors[sector]}</option>)}
            </select>
            <p className={hint}>{labels.sectorHint}</p>
          </>
        ) : null}
        {step === "publish" ? (
          <>
            <h2 className={title}>{labels.publishTitle}</h2>
            <p className="text-[15px] text-ink-soft">{labels.publishLead}</p>
            {wave ? (
              <div aria-hidden="true" className="mt-2 flex gap-1">
                {Array.from({ length: 24 }, (_, position) => <span key={position} className="h-3 flex-1 rounded-[2px] bg-level-3" style={{ animation: "square-pulse 900ms var(--ease-enter) 1 both", animationDelay: `${position * 28}ms` }} />)}
              </div>
            ) : null}
          </>
        ) : null}
        {error ? <p role="alert" className="text-[14px] text-danger">{error}</p> : null}
      </div>

      <div className="flex items-center gap-2">
        {step === "publish" ? (
          <Button onClick={() => void publish()} status={status} doneLabel={labels.published} disabled={!values.country} className="min-h-12">{labels.publish}</Button>
        ) : (
          <Button onClick={() => void next()} status={status} disabled={step === "country" && !answered}>{labels.continue}</Button>
        )}
        {index > 0 && status !== "done" ? <button type="button" onClick={() => setIndex((value) => value - 1)} className="flex min-h-11 items-center px-3 text-[14px] text-ink-muted hover:text-ink">{labels.back}</button> : null}
        {step !== "publish" && step !== "country" ? <button type="button" onClick={() => setIndex((value) => value + 1)} className="ml-auto flex min-h-11 items-center text-[14px] text-ink-muted hover:text-ink">{labels.skip}</button> : null}
      </div>
    </div>
  );
}

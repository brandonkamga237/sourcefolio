"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import type { MyAnswers, Specialty } from "@/services/profile";
import { answerCountry, answerSpecialty, saveEmail } from "@/services/profile-answers";

type Step = "country" | "specialty" | "email";

export interface ProfileQuestionsLabels {
  /** Contains {current} and {total}. */
  progress: string;
  countryTitle: string;
  countryPlaceholder: string;
  specialtyTitle: string;
  specialties: Record<Specialty, string>;
  emailTitle: string;
  emailHint: string;
  next: string;
  skip: string;
  saving: string;
  failed: string;
  summaryTitle: string;
  countryLabel: string;
  specialtyLabel: string;
  emailLabel: string;
  notGiven: string;
  edit: string;
  done: string;
}

interface ProfileQuestionsProps {
  initial: MyAnswers;
  countries: { code: string; name: string }[];
  labels: ProfileQuestionsLabels;
  doneHref: string;
}

// The spec's rule: a question already answered is never asked again. The first run asks what
// is missing, the email last and skippable; afterwards each answer is changed from the summary.
function firstRun(answers: MyAnswers): Step[] {
  const missing: Step[] = [];
  if (!answers.country) missing.push("country");
  if (!answers.specialty) missing.push("specialty");
  if (missing.length > 0 && !answers.email) missing.push("email");
  return missing;
}

const primaryButton =
  "press flex h-12 w-full items-center justify-center rounded-control bg-accent px-5 text-[15px] font-semibold text-white disabled:opacity-50";

export function ProfileQuestions({ initial, countries, labels, doneHref }: ProfileQuestionsProps) {
  const [answers, setAnswers] = useState(initial);
  const [queue, setQueue] = useState<Step[]>(() => firstRun(initial));
  const [total, setTotal] = useState(queue.length);
  const [country, setCountry] = useState(initial.country ?? "");
  const [email, setEmail] = useState(initial.email ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const step = queue[0];

  function advance() {
    setError(null);
    setQueue((current) => current.slice(1));
  }

  function ask(only: Step) {
    setError(null);
    setTotal(1);
    setQueue([only]);
  }

  async function save(request: () => Promise<{ status: number; message: string | null }>, apply: () => void) {
    setSaving(true);
    setError(null);
    const result = await request();
    setSaving(false);
    if (result.status === 200) {
      apply();
      advance();
    } else {
      setError(result.message ?? labels.failed);
    }
  }

  function submitCountry(event: FormEvent) {
    event.preventDefault();
    if (country) void save(() => answerCountry(country), () => setAnswers((current) => ({ ...current, country })));
  }

  function chooseSpecialty(specialty: Specialty) {
    void save(() => answerSpecialty(specialty), () => setAnswers((current) => ({ ...current, specialty })));
  }

  function submitEmail(event: FormEvent) {
    event.preventDefault();
    const value = email.trim() || null;
    void save(() => saveEmail(value), () => setAnswers((current) => ({ ...current, email: value })));
  }

  if (!step) {
    const countryName = countries.find((option) => option.code === answers.country)?.name;
    const rows: { step: Step; label: string; value: string | null }[] = [
      { step: "country", label: labels.countryLabel, value: countryName ?? null },
      { step: "specialty", label: labels.specialtyLabel, value: answers.specialty ? labels.specialties[answers.specialty] : null },
      { step: "email", label: labels.emailLabel, value: answers.email },
    ];
    return (
      <section aria-labelledby="profile-summary" className="flex flex-col gap-4">
        <h1 id="profile-summary" className="font-display text-[28px] leading-none font-bold tracking-[-0.035em]">
          {labels.summaryTitle}
        </h1>
        <dl className="flex flex-col divide-y divide-line rounded-card border border-line bg-surface">
          {rows.map((row) => (
            <div key={row.step} className="flex items-center justify-between gap-3 p-4">
              <div className="flex min-w-0 flex-col gap-0.5">
                <dt className="text-[13px] text-ink-muted">{row.label}</dt>
                <dd className="truncate text-[15px] font-semibold">{row.value ?? labels.notGiven}</dd>
              </div>
              <button
                type="button"
                onClick={() => ask(row.step)}
                className="press flex h-11 shrink-0 items-center rounded-control border border-line px-4 text-sm font-semibold text-ink"
              >
                {labels.edit}
              </button>
            </div>
          ))}
        </dl>
        <Link href={doneHref} className="press flex h-11 w-fit items-center text-sm font-semibold text-ink underline underline-offset-2">
          {labels.done}
        </Link>
      </section>
    );
  }

  const current = total - queue.length + 1;
  return (
    <section aria-live="polite" className="flex flex-col gap-6">
      {total > 1 ? (
        <div className="flex flex-col gap-2">
          <p className="text-[13px] text-ink-muted">
            {labels.progress.replace("{current}", String(current)).replace("{total}", String(total))}
          </p>
          <div aria-hidden="true" className="h-1.5 overflow-hidden rounded-full bg-surface-sunken">
            <div className="h-full rounded-full bg-accent transition-[width]" style={{ width: `${(current / total) * 100}%` }} />
          </div>
        </div>
      ) : null}

      {step === "country" ? (
        <form onSubmit={submitCountry} className="flex flex-col gap-4">
          <label htmlFor="country" className="font-display text-[26px] leading-tight font-bold tracking-[-0.03em]">
            {labels.countryTitle}
          </label>
          <select
            id="country"
            value={country}
            onChange={(event) => setCountry(event.target.value)}
            className="h-14 rounded-control border border-line bg-surface px-4 text-base text-ink"
          >
            <option value="" disabled>
              {labels.countryPlaceholder}
            </option>
            {countries.map((option) => (
              <option key={option.code} value={option.code}>
                {option.name}
              </option>
            ))}
          </select>
          <button type="submit" disabled={!country || saving} className={primaryButton}>
            {saving ? labels.saving : labels.next}
          </button>
        </form>
      ) : null}

      {step === "specialty" ? (
        <fieldset className="flex flex-col gap-4">
          <legend className="mb-4 font-display text-[26px] leading-tight font-bold tracking-[-0.03em]">{labels.specialtyTitle}</legend>
          <div className="flex flex-wrap gap-2">
            {(Object.keys(labels.specialties) as Specialty[]).map((specialty) => (
              <button
                key={specialty}
                type="button"
                disabled={saving}
                aria-pressed={answers.specialty === specialty}
                onClick={() => chooseSpecialty(specialty)}
                className={`press flex h-11 items-center rounded-full px-4 text-[15px] font-semibold disabled:opacity-60 ${
                  answers.specialty === specialty ? "bg-ink text-canvas" : "border border-line bg-surface text-ink"
                }`}
              >
                {labels.specialties[specialty]}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {step === "email" ? (
        <form onSubmit={submitEmail} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <label htmlFor="email" className="font-display text-[26px] leading-tight font-bold tracking-[-0.03em]">
              {labels.emailTitle}
            </label>
            <p className="text-sm leading-normal text-ink-body">{labels.emailHint}</p>
          </div>
          <input
            id="email"
            type="email"
            inputMode="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="h-14 rounded-control border border-line bg-surface px-4 text-base text-ink"
          />
          <button type="submit" disabled={saving} className={primaryButton}>
            {saving ? labels.saving : labels.next}
          </button>
          <button type="button" onClick={advance} disabled={saving} className="press flex h-11 items-center justify-center text-sm font-semibold text-ink">
            {labels.skip}
          </button>
        </form>
      ) : null}

      {error ? (
        <p role="alert" className="text-[13px] leading-normal text-ink-body">
          {error}
        </p>
      ) : null}
    </section>
  );
}

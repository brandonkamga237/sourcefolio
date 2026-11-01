"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import type { MyAnswers } from "@/services/profile";
import { MAX_HEADLINE, MAX_SPECIALTIES, SPECIALTIES, SPECIALTY_GROUPS, type Specialty, type SpecialtyGroup } from "@/services/profile-types";
import { answerCountry, answerHeadline, answerSpecialties, saveEmail } from "@/services/profile-answers";

type Step = "headline" | "country" | "specialty" | "email";

export interface ProfileQuestionsLabels {
  /** Contains {current} and {total}. */
  progress: string;
  headlineTitle: string;
  headlineHint: string;
  headlinePlaceholder: string;
  /** Contains {count} and {max}. */
  headlineCounter: string;
  headlineLabel: string;
  countryTitle: string;
  countryPlaceholder: string;
  specialtyTitle: string;
  /** Tells to tap in order of importance, up to four. */
  specialtyHint: string;
  specialties: Record<Specialty, string>;
  groups: Record<SpecialtyGroup, string>;
  searchPlaceholder: string;
  noMatch: string;
  selectionEmpty: string;
  /** Contains {name}. */
  removeChoice: string;
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
// is missing, the headline first (it is what people read under the name), the email last; both
// can be skipped. Afterwards each answer is changed from the summary.
function firstRun(answers: MyAnswers): Step[] {
  const missing: Step[] = [];
  if (!answers.headline) missing.push("headline");
  if (!answers.country) missing.push("country");
  if (answers.specialties.length === 0) missing.push("specialty");
  if (missing.length > 0 && !answers.email) missing.push("email");
  return missing;
}

// Accent-insensitive, so "securite" finds "Sécurité".
function matches(label: string, query: string): boolean {
  const fold = (text: string) => text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();
  return fold(label).includes(fold(query.trim()));
}

const primaryButton =
  "press flex h-12 w-full items-center justify-center rounded-control bg-accent px-5 text-[15px] font-semibold text-white disabled:opacity-50";

export function ProfileQuestions({ initial, countries, labels, doneHref }: ProfileQuestionsProps) {
  const [answers, setAnswers] = useState(initial);
  const [queue, setQueue] = useState<Step[]>(() => firstRun(initial));
  const [total, setTotal] = useState(queue.length);
  const [country, setCountry] = useState(initial.country ?? "");
  const [email, setEmail] = useState(initial.email ?? "");
  const [headline, setHeadline] = useState(initial.headline ?? "");
  const [picked, setPicked] = useState<Specialty[]>(initial.specialties);
  const [query, setQuery] = useState("");
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

  function submitHeadline(event: FormEvent) {
    event.preventDefault();
    const value = headline.trim() || null;
    void save(() => answerHeadline(value), () => setAnswers((current) => ({ ...current, headline: value })));
  }

  function submitCountry(event: FormEvent) {
    event.preventDefault();
    if (country) void save(() => answerCountry(country), () => setAnswers((current) => ({ ...current, country })));
  }

  // A tap adds the specialty after the others (its rank is its importance); a second tap takes
  // it out and the ones after it move up.
  function toggleSpecialty(specialty: Specialty) {
    setPicked((current) =>
      current.includes(specialty)
        ? current.filter((item) => item !== specialty)
        : current.length < MAX_SPECIALTIES
          ? [...current, specialty]
          : current,
    );
  }

  function submitSpecialties(event: FormEvent) {
    event.preventDefault();
    const specialties = picked;
    if (specialties.length > 0) void save(() => answerSpecialties(specialties), () => setAnswers((current) => ({ ...current, specialties })));
  }

  function submitEmail(event: FormEvent) {
    event.preventDefault();
    const value = email.trim() || null;
    void save(() => saveEmail(value), () => setAnswers((current) => ({ ...current, email: value })));
  }

  if (!step) {
    const countryName = countries.find((option) => option.code === answers.country)?.name;
    const rows: { step: Step; label: string; value: string | null }[] = [
      { step: "headline", label: labels.headlineLabel, value: answers.headline },
      { step: "country", label: labels.countryLabel, value: countryName ?? null },
      {
        step: "specialty",
        label: labels.specialtyLabel,
        value: answers.specialties.length > 0 ? answers.specialties.map((specialty) => labels.specialties[specialty]).join(" · ") : null,
      },
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

      {step === "headline" ? (
        <form onSubmit={submitHeadline} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <label htmlFor="headline" className="font-display text-[26px] leading-tight font-bold tracking-[-0.03em]">
              {labels.headlineTitle}
            </label>
            <p className="text-sm leading-normal text-ink-body">{labels.headlineHint}</p>
          </div>
          <div className="flex flex-col gap-1.5">
            <input
              id="headline"
              value={headline}
              maxLength={MAX_HEADLINE}
              onChange={(event) => setHeadline(event.target.value)}
              placeholder={labels.headlinePlaceholder}
              autoComplete="off"
              aria-describedby="headline-count"
              className="h-14 rounded-control border border-line bg-surface px-4 text-base text-ink placeholder:text-ink-muted"
            />
            <p id="headline-count" className="self-end font-mono text-xs text-ink-muted tabular-nums">
              {labels.headlineCounter.replace("{count}", String(headline.length)).replace("{max}", String(MAX_HEADLINE))}
            </p>
          </div>
          <button type="submit" disabled={saving || headline.trim().length === 1} className={primaryButton}>
            {saving ? labels.saving : labels.next}
          </button>
          <button type="button" onClick={advance} disabled={saving} className="flex h-11 items-center justify-center text-sm font-semibold text-ink">
            {labels.skip}
          </button>
        </form>
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
        <form onSubmit={submitSpecialties} className="flex flex-col gap-4">
          <fieldset className="flex flex-col gap-3">
            <legend className="mb-1 font-display text-[26px] leading-tight font-bold tracking-[-0.03em]">{labels.specialtyTitle}</legend>
            <p className="text-sm leading-normal text-ink-body">{labels.specialtyHint}</p>

            {/* The order chosen so far, always in sight: it is the meaning of the answer. */}
            <div aria-live="polite" className="flex min-h-11 flex-wrap items-center gap-1.5 rounded-control bg-surface-sunken p-1.5">
              {picked.length === 0 ? (
                <span className="px-2 text-[13px] text-ink-muted">{labels.selectionEmpty}</span>
              ) : (
                picked.map((specialty, index) => (
                  <button
                    key={specialty}
                    type="button"
                    onClick={() => toggleSpecialty(specialty)}
                    aria-label={labels.removeChoice.replace("{name}", labels.specialties[specialty])}
                    className="press flex h-9 items-center gap-1.5 rounded-full bg-ink pr-3 pl-1 text-[13px] font-semibold text-ink-inverse"
                  >
                    <span aria-hidden="true" className="flex size-7 items-center justify-center rounded-full bg-accent font-mono text-xs text-white">
                      {index + 1}
                    </span>
                    {labels.specialties[specialty]}
                  </button>
                ))
              )}
            </div>

            <input
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={labels.searchPlaceholder}
              aria-label={labels.searchPlaceholder}
              className="h-12 rounded-control border border-line bg-surface px-4 text-base text-ink placeholder:text-ink-muted"
            />

            <div className="flex flex-col gap-4">
              {(Object.entries(SPECIALTY_GROUPS) as [SpecialtyGroup, readonly Specialty[]][]).map(([group, members]) => {
                const shown = members.filter((specialty) => matches(labels.specialties[specialty], query));
                if (shown.length === 0) return null;
                return (
                  <div key={group} className="flex flex-col gap-2">
                    <h3 className="text-xs font-semibold tracking-wide text-ink-muted uppercase">{labels.groups[group]}</h3>
                    <div className="flex flex-wrap gap-2">
                      {shown.map((specialty) => {
                        const rank = picked.indexOf(specialty) + 1;
                        const full = rank === 0 && picked.length >= MAX_SPECIALTIES;
                        return (
                          <button
                            key={specialty}
                            type="button"
                            disabled={saving || full}
                            aria-pressed={rank > 0}
                            onClick={() => toggleSpecialty(specialty)}
                            className={`press flex h-11 items-center gap-2 rounded-full pr-4 text-[15px] font-semibold transition-colors duration-150 disabled:opacity-40 ${
                              rank > 0 ? "bg-ink pl-1.5 text-canvas" : "border border-line bg-surface pl-4 text-ink hover:border-ink-muted"
                            }`}
                          >
                            {rank > 0 ? (
                              <span aria-hidden="true" className="flex size-8 items-center justify-center rounded-full bg-accent font-mono text-sm text-white">
                                {rank}
                              </span>
                            ) : null}
                            {labels.specialties[specialty]}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
              {SPECIALTIES.every((specialty) => !matches(labels.specialties[specialty], query)) ? (
                <p className="text-sm text-ink-muted">{labels.noMatch}</p>
              ) : null}
            </div>
          </fieldset>
          {/* Kept in reach while the list scrolls on a phone. */}
          <div className="sticky bottom-[calc(4rem+env(safe-area-inset-bottom))] -mx-1 rounded-control bg-canvas/90 p-1 backdrop-blur-sm lg:bottom-4">
            <button type="submit" disabled={picked.length === 0 || saving} className={primaryButton}>
              {saving ? labels.saving : labels.next}
            </button>
          </div>
        </form>
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

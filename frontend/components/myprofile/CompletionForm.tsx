"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import type { ClientResult } from "@/lib/api/client";
import { answerBio, answerCity, answerCountry, answerLinkedIn } from "@/services/profile-answers";
import type { Profile } from "@/services/profile-types";
import { MAX_BIO, MAX_CITY } from "@/services/profile-types";

interface CompletionFormProps {
  country: string | null;
  city: string | null;
  bio: string | null;
  /** GitHub's bio, offered when the member has written none yet. */
  githubBio: string | null;
  linkedIn: string | null;
  countries: { code: string; name: string }[];
  labels: {
    country: string; countryHint: string; countryPlaceholder: string;
    city: string; optional: string; cityHint: string;
    bio: string; bioImported: string; bioHint: string;
    linkedIn: string; linkedInHint: string;
    saving: string; saved: string; failed: string;
  };
}

type FieldState = "idle" | "saving" | "saved" | "failed";

/**
 * The few things only the member can give. Each field is saved on its own when it changes, and
 * the page re-renders so the preview follows; the main action of the screen stays "Publish".
 */
export function CompletionForm({ country, city, bio, githubBio, linkedIn, countries, labels }: CompletionFormProps) {
  const router = useRouter();
  const [state, setState] = useState<Record<string, FieldState>>({});
  const [errors, setErrors] = useState<Record<string, string | null>>({});
  const [bioDraft, setBioDraft] = useState(bio ?? githubBio ?? "");
  const imported = !bio && githubBio !== null && bioDraft === githubBio;
  const importedOnce = useRef(false);

  // The GitHub bio is the member's own words already: kept as their bio right away, so the
  // preview and a later publication show it. Still private until they publish, and editable.
  useEffect(() => {
    if (bio || !githubBio || importedOnce.current) return;
    importedOnce.current = true;
    void save("bio", () => answerBio(githubBio));
    // eslint-disable-next-line react-hooks/exhaustive-deps -- once, on arrival
  }, []);

  async function save(field: string, request: () => Promise<ClientResult<Profile>>) {
    setState((current) => ({ ...current, [field]: "saving" }));
    const result = await request();
    setState((current) => ({ ...current, [field]: result.data ? "saved" : "failed" }));
    setErrors((current) => ({ ...current, [field]: result.data ? null : result.message ?? labels.failed }));
    if (result.data) router.refresh();
  }

  const status = (field: string) => {
    const value = state[field];
    if (value === "saving") return labels.saving;
    if (value === "saved") return labels.saved;
    return null;
  };

  const hint = (field: string, text: string) => (
    <p className={`text-[14px] ${errors[field] ? "text-danger" : "text-ink-muted"}`} role={errors[field] ? "alert" : undefined}>
      {errors[field] ?? text}
      {status(field) ? <span className="ml-2 text-ink-muted">· {status(field)}</span> : null}
    </p>
  );

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-col gap-1.5">
        <label htmlFor="country" className="text-[16px] font-semibold">
          {labels.country} <span className="text-danger">*</span>
        </label>
        <select
          id="country"
          defaultValue={country ?? ""}
          onChange={(event) => event.target.value && void save("country", () => answerCountry(event.target.value))}
          className="field"
        >
          <option value="" disabled>{labels.countryPlaceholder}</option>
          {countries.map((option) => (
            <option key={option.code} value={option.code}>{option.name}</option>
          ))}
        </select>
        {hint("country", labels.countryHint)}
      </div>

      <div className="flex flex-col gap-1.5">
        <label htmlFor="city" className="text-[16px] font-semibold">
          {labels.city} <span className="font-normal text-ink-muted">({labels.optional})</span>
        </label>
        <input
          id="city"
          defaultValue={city ?? ""}
          maxLength={MAX_CITY}
          autoComplete="address-level2"
          onBlur={(event) => event.target.value !== (city ?? "") && void save("city", () => answerCity(event.target.value || null))}
          className="field"
        />
        {hint("city", labels.cityHint)}
      </div>

      <div className="flex flex-col gap-1.5">
        <div className="flex items-center justify-between gap-2">
          <label htmlFor="bio" className="text-[16px] font-semibold">{labels.bio}</label>
          {imported ? <span className="rounded-full border border-line px-2 py-0.5 text-[14px] text-ink-muted">{labels.bioImported}</span> : null}
        </div>
        <textarea
          id="bio"
          value={bioDraft}
          maxLength={MAX_BIO}
          rows={3}
          onChange={(event) => setBioDraft(event.target.value)}
          onBlur={() => bioDraft !== (bio ?? "") && void save("bio", () => answerBio(bioDraft || null))}
          className="field resize-y py-2"
        />
        {hint("bio", labels.bioHint.replace("{count}", String(bioDraft.length)))}
      </div>

      <div className="flex flex-col gap-1.5">
        <label htmlFor="linkedin" className="text-[16px] font-semibold">
          {labels.linkedIn} <span className="font-normal text-ink-muted">({labels.optional})</span>
        </label>
        <div className="flex">
          <span className="flex items-center rounded-l-control border border-r-0 border-line bg-surface-sunken px-3 text-[14px] text-ink-muted">
            linkedin.com/in/
          </span>
          <input
            id="linkedin"
            defaultValue={linkedIn ?? ""}
            inputMode="url"
            autoCapitalize="none"
            autoCorrect="off"
            onBlur={(event) => event.target.value !== (linkedIn ?? "") && void save("linkedin", () => answerLinkedIn(event.target.value || null))}
            className="field rounded-l-none"
          />
        </div>
        {hint("linkedin", labels.linkedInHint)}
      </div>
    </div>
  );
}

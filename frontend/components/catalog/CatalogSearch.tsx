"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { IconClose, IconFilters, IconSearch } from "@/components/icons";
import { countryName, flagOf } from "@/lib/countries";
import { filterParams, hasFilters, readFilters, type CatalogFacets, type CatalogFilters } from "@/services/catalog-types";

export interface CatalogSearchLabels {
  search: string;
  country: string;
  anyCountry: string;
  interest: string;
  anyInterest: string;
  clear: string;
  /** The phone's button that unfolds the country and interest pickers. */
  filters: string;
  specialtyNames: Record<string, string>;
}

interface CatalogSearchProps {
  locale: string;
  facets: CatalogFacets;
  labels: CatalogSearchLabels;
}

const control =
  "h-11 min-w-0 rounded-control border border-line bg-surface px-3 text-[15px] text-ink transition-colors duration-150 hover:border-ink-muted focus:border-ink";

// Words, a country, an interest: written into the address (replaced, not pushed, so Back still
// leaves the page), which the server reads to render the matching first page. Typing waits a
// beat before searching, so each letter does not cost a request.
export function CatalogSearch({ locale, facets, labels }: CatalogSearchProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const current = readFilters(searchParams);
  const [text, setText] = useState(current.q ?? "");
  // On a phone the pickers fold away so the sticky bar stays slim; open when one is in use.
  const [open, setOpen] = useState(Boolean(current.country || current.specialty));
  const active = (current.country ? 1 : 0) + (current.specialty ? 1 : 0);
  const timer = useRef<number | undefined>(undefined);

  function apply(next: CatalogFilters) {
    const query = new URLSearchParams(filterParams(next)).toString();
    router.replace(query ? `${pathname}?${query}` : pathname, { scroll: false });
  }

  useEffect(() => () => window.clearTimeout(timer.current), []);

  function onText(value: string) {
    setText(value);
    window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => apply({ ...readFilters(searchParams), q: value.trim() || undefined }), 350);
  }

  return (
    <div role="search" className="flex w-full flex-col gap-2 sm:flex-row sm:items-center">
      <div className="flex gap-2 sm:contents">
      <label className="relative flex min-w-0 flex-1 items-center">
        <span className="sr-only">{labels.search}</span>
        <IconSearch aria-hidden="true" className="pointer-events-none absolute left-3 size-[18px] text-ink-muted" />
        <input
          type="search"
          value={text}
          maxLength={60}
          onChange={(event) => onText(event.target.value)}
          placeholder={labels.search}
          className={`${control} w-full pl-10`}
        />
      </label>
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-expanded={open}
        className={`press flex h-11 shrink-0 items-center gap-1.5 rounded-control border px-3 text-sm font-semibold sm:hidden ${
          active > 0 ? "border-ink bg-ink text-ink-inverse" : "border-line bg-surface text-ink"
        }`}
      >
        <IconFilters aria-hidden="true" className="size-4" />
        {labels.filters}
        {active > 0 ? <span className="font-mono text-xs">{active}</span> : null}
      </button>
      </div>
      <div className={`grid-cols-2 gap-2 sm:flex ${open ? "grid" : "hidden"}`}>
        <select
          aria-label={labels.country}
          value={current.country ?? ""}
          onChange={(event) => apply({ ...current, country: event.target.value || undefined })}
          className={`${control} sm:w-44`}
        >
          <option value="">{labels.anyCountry}</option>
          {facets.countries.map((facet) => (
            <option key={facet.value} value={facet.value}>
              {`${flagOf(facet.value)} ${countryName(facet.value, locale)} (${facet.count})`}
            </option>
          ))}
        </select>
        <select
          aria-label={labels.interest}
          value={current.specialty ?? ""}
          onChange={(event) => apply({ ...current, specialty: event.target.value || undefined })}
          className={`${control} sm:w-48`}
        >
          <option value="">{labels.anyInterest}</option>
          {facets.specialties.map((facet) => (
            <option key={facet.value} value={facet.value}>
              {`${labels.specialtyNames[facet.value] ?? facet.value} (${facet.count})`}
            </option>
          ))}
        </select>
      </div>
      {hasFilters(current) ? (
        <button
          type="button"
          onClick={() => {
            setText("");
            apply({});
          }}
          className="press flex h-11 shrink-0 items-center justify-center gap-1.5 rounded-control px-3 text-sm font-semibold text-ink-muted transition-colors duration-150 hover:bg-surface-sunken hover:text-ink"
        >
          <IconClose aria-hidden="true" className="size-4" />
          {labels.clear}
        </button>
      ) : null}
    </div>
  );
}

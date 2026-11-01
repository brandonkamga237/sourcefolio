"use client";

import { useId, useState } from "react";
import { IconExternal } from "@/components/icons";
import type { RepositoryWithoutSite } from "@/services/showcases";
import { RescanButton, type RescanLabels } from "./RescanButton";

export interface RepositoryPickerLabels {
  title: string;
  lead: string;
  search: string;
  noMatch: string;
  none: string;
  /** Contains {count}. */
  showAll: string;
  pick: string;
  /** Contains {repository}. */
  stepsTitle: string;
  steps: [string, string, string];
  openOnGithub: string;
  rescan: RescanLabels;
}

interface RepositoryPickerProps {
  repositories: RepositoryWithoutSite[];
  scannedAt: string | null;
  labels: RepositoryPickerLabels;
  /** Unfolded when no site was found at all: the list is then the only way forward. */
  open?: boolean;
}

const SHOWN = 6;

// A repository becomes publishable once its GitHub "Website" field names the site: only its
// owner can set that field, so it proves the site is theirs without anything else to check.
// Picking one here tells the member how, then a new search brings it up as a candidate.
export function RepositoryPicker({ repositories, scannedAt, labels, open }: RepositoryPickerProps) {
  const [query, setQuery] = useState("");
  const [all, setAll] = useState(false);
  const [picked, setPicked] = useState<RepositoryWithoutSite | null>(null);
  const stepsId = useId();

  const needle = query.trim().toLowerCase();
  const matching = repositories.filter((item) => item.repository.toLowerCase().includes(needle));
  const shown = all || needle ? matching : matching.slice(0, SHOWN);

  return (
    <details open={open} className="group/picker border-t border-line-soft [&_summary::-webkit-details-marker]:hidden">
      <summary className="flex min-h-12 cursor-pointer list-none items-center justify-between gap-3 py-3 text-sm font-semibold select-none">
        {labels.title}
        <span aria-hidden="true" className="size-2 shrink-0 rotate-45 border-r-2 border-b-2 border-ink-muted transition-transform duration-200 group-open/picker:-rotate-135" />
      </summary>
      <div className="flex flex-col gap-3 pb-1">
        <p className="text-[13px] leading-normal text-ink-body">{labels.lead}</p>

        {repositories.length === 0 ? (
          <p className="text-sm text-ink-muted">{labels.none}</p>
        ) : (
          <>
            {repositories.length > SHOWN ? (
              <input
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={labels.search}
                aria-label={labels.search}
                className="h-11 rounded-control border border-line bg-canvas px-3.5 text-base text-ink placeholder:text-ink-muted"
              />
            ) : null}
            <ul className="flex flex-col divide-y divide-line-soft rounded-control border border-line">
              {shown.map((item) => {
                const selected = picked?.repository === item.repository;
                return (
                  <li key={item.repository} className="flex items-center justify-between gap-3 py-1 pr-1 pl-3.5">
                    <span className="min-w-0 font-mono text-[13px] break-all text-ink">{item.repository}</span>
                    <button
                      type="button"
                      aria-pressed={selected}
                      aria-controls={stepsId}
                      onClick={() => setPicked(selected ? null : item)}
                      className={`press h-11 shrink-0 rounded-control px-3 text-[13px] font-semibold ${
                        selected ? "bg-accent-soft text-accent-ink" : "text-accent-ink hover:bg-surface-sunken"
                      }`}
                    >
                      {labels.pick}
                    </button>
                  </li>
                );
              })}
              {shown.length === 0 ? <li className="p-3.5 text-sm text-ink-muted">{labels.noMatch}</li> : null}
            </ul>
            {!all && !needle && repositories.length > SHOWN ? (
              <button type="button" onClick={() => setAll(true)} className="press h-11 w-fit rounded-control px-1 text-sm font-semibold text-ink">
                {labels.showAll.replace("{count}", String(repositories.length))}
              </button>
            ) : null}
          </>
        )}

        {picked ? (
          <div id={stepsId} aria-live="polite" className="rise-in flex flex-col gap-3 rounded-control bg-surface-sunken p-4">
            <p className="text-sm font-semibold break-all text-ink">{labels.stepsTitle.replace("{repository}", picked.repository)}</p>
            <ol className="flex flex-col gap-2.5">
              {labels.steps.map((step, index) => (
                <li key={index} className="flex items-start gap-2.5 text-[13px] leading-snug text-ink-body">
                  <span aria-hidden="true" className="flex size-6 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs text-accent-ink">
                    {index + 1}
                  </span>
                  <span className="pt-0.5">{step}</span>
                </li>
              ))}
            </ol>
            <div className="flex flex-col gap-2 sm:flex-row">
              <a
                href={picked.repositoryUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="press flex h-11 items-center justify-center gap-1.5 rounded-control border border-ink px-4 text-sm font-semibold text-ink"
              >
                {labels.openOnGithub}
                <IconExternal className="size-4" />
              </a>
              <RescanButton scannedAt={scannedAt} labels={labels.rescan} primary />
            </div>
          </div>
        ) : null}
      </div>
    </details>
  );
}

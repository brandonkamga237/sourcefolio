import { IconCode } from "@/components/icons";

export interface ProjectFactsLabels {
  title: string;
  languages: string;
  /** Contains {when}. */
  updated: string;
}

interface ProjectFactsProps {
  description: string | null;
  languages: string[];
  /** Already phrased for the reader ("il y a 3 j"); null when GitHub has not said yet. */
  updatedSince: string | null;
  labels: ProjectFactsLabels;
}

// What the card cannot say: the project in its author's words (the repository's description on
// GitHub), what it is built with, and whether its code is still moving. Read from GitHub once
// a day; nothing shows until it has been.
export function ProjectFacts({ description, languages, updatedSince, labels }: ProjectFactsProps) {
  if (!description && languages.length === 0 && !updatedSince) return null;
  return (
    <section aria-label={labels.title} className="flex flex-col gap-2.5 rounded-control bg-surface-raised p-3 ring-1 ring-line-soft">
      {description ? (
        <p className="line-clamp-3 text-sm leading-snug text-ink-body" title={description}>
          {description}
        </p>
      ) : null}
      {languages.length > 0 ? (
        <ul aria-label={labels.languages} className="flex flex-wrap gap-1.5">
          {languages.map((language) => (
            <li key={language} className="flex h-7 items-center rounded-full border border-line bg-surface px-2.5 font-mono text-xs text-ink">
              {language}
            </li>
          ))}
        </ul>
      ) : null}
      {updatedSince ? (
        <p className="flex items-center gap-1.5 text-xs text-ink-muted" suppressHydrationWarning>
          <IconCode aria-hidden="true" className="size-3.5" />
          {labels.updated.replace("{when}", updatedSince)}
        </p>
      ) : null}
    </section>
  );
}

import { IconCode, IconExternal } from "@/components/icons";

interface ShowcaseLinksProps {
  siteUrl: string;
  repositoryUrl: string;
  visitLabel: string;
  codeLabel: string;
}

const linkClass =
  "flex h-11 items-center justify-center gap-1.5 rounded-control border border-line bg-surface px-4 text-sm font-semibold text-ink";

// External sites open in a new tab: many portfolios refuse to be framed, and the member stays
// on their dashboard.
export function ShowcaseLinks({ siteUrl, repositoryUrl, visitLabel, codeLabel }: ShowcaseLinksProps) {
  return (
    <div className="flex flex-wrap gap-2">
      <a href={siteUrl} target="_blank" rel="noopener noreferrer" className={linkClass}>
        {visitLabel}
        <IconExternal className="size-4" />
      </a>
      <a href={repositoryUrl} target="_blank" rel="noopener noreferrer" className={linkClass}>
        <IconCode className="size-4" />
        {codeLabel}
      </a>
    </div>
  );
}

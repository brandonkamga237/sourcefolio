import { IconGithub, IconStar } from "@/components/icons";

interface StarCountProps {
  count: number;
  locale: string;
  /** The full sentence for screen readers and the tooltip, e.g. "12 stars on GitHub". */
  label: string;
  className?: string;
}

// The GitHub stars of a repository: the GitHub mark, a star and a compact number (1.2k), so it
// reads as GitHub's count at a glance; the sentence goes to assistive technology. No hook, so
// server and client components both use it.
export function StarCount({ count, locale, label, className = "" }: StarCountProps) {
  return (
    <span
      title={label}
      className={`inline-flex h-7 items-center gap-1.5 rounded-full border border-line bg-surface px-2.5 font-mono text-xs tabular-nums ${className}`}
    >
      <IconGithub aria-hidden="true" className="size-3.5 shrink-0" />
      <IconStar aria-hidden="true" className="size-3.5 shrink-0 fill-accent text-accent" />
      <span aria-hidden="true">{new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(count)}</span>
      <span className="sr-only">{label}</span>
    </span>
  );
}

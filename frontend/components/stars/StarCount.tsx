import { IconStar } from "@/components/icons";

interface StarCountProps {
  count: number;
  locale: string;
  /** The full sentence for screen readers and the tooltip, e.g. "12 stars on GitHub". */
  label: string;
  className?: string;
}

// The GitHub stars of a repository: an icon and a compact number (1.2k), the sentence for
// assistive technology. No hook, so server and client components both use it.
export function StarCount({ count, locale, label, className = "" }: StarCountProps) {
  return (
    <span title={label} className={`inline-flex items-center gap-1 font-mono tabular-nums ${className}`}>
      <IconStar aria-hidden="true" className="size-[1.1em] fill-current" />
      <span aria-hidden="true">{new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(count)}</span>
      <span className="sr-only">{label}</span>
    </span>
  );
}

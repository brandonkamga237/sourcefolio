import Link from "next/link";

interface SegmentedProps {
  label: string;
  options: readonly { key: string; label: string; href: string }[];
  current: string;
}

/** Equal columns, so the pill only ever translates. Each option is a real address. */
export function Segmented({ label, options, current }: SegmentedProps) {
  const index = options.findIndex((option) => option.key === current);
  return (
    <nav aria-label={label} className="relative grid w-fit auto-cols-fr grid-flow-col rounded-control border border-line bg-surface-raised p-0.5">
      <span
        aria-hidden="true"
        className="absolute inset-y-0.5 left-0.5 rounded-[5px] border border-line bg-canvas shadow-sm transition-transform duration-(--dur-3) ease-spring"
        style={{ width: `calc((100% - 4px) / ${options.length})`, transform: `translateX(${index * 100}%)` }}
      />
      {options.map((option) => (
        <Link
          key={option.key}
          href={option.href}
          aria-current={option.key === current ? "page" : undefined}
          className={`relative flex min-h-11 items-center justify-center px-4 text-[14px] whitespace-nowrap transition-colors duration-(--dur-2) ${option.key === current ? "font-semibold text-ink" : "text-ink-muted hover:text-ink"}`}
        >
          {option.label}
        </Link>
      ))}
    </nav>
  );
}

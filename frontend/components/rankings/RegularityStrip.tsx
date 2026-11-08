interface RegularityStripProps {
  /** Contributions of the last twelve months, oldest first. */
  months: number[];
}

const SHADE = ["bg-level-0", "bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;

// One square a month, shaded against the member's own busiest month: the rhythm, not the volume.
export function RegularityStrip({ months }: RegularityStripProps) {
  const peak = Math.max(1, ...months);
  return (
    <span aria-hidden="true" className="flex gap-[2px]">
      {months.map((count, index) => {
        const level = count === 0 ? 0 : Math.min(4, 1 + Math.floor((count / peak) * 3.999));
        return <span key={index} className={`size-[9px] rounded-[2px] ${SHADE[level]}`} />;
      })}
    </span>
  );
}

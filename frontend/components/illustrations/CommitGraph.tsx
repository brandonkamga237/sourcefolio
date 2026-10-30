interface CommitGraphProps {
  className?: string;
}

// A branch leaving main, two commits, a merge back: the open-source life of a portfolio, as
// decoration. Colours come from the theme tokens, so it follows light and dark.
export function CommitGraph({ className = "" }: CommitGraphProps) {
  return (
    <svg aria-hidden="true" viewBox="0 0 360 180" fill="none" className={className}>
      <path d="M12 120H348" className="stroke-line" strokeWidth="6" strokeLinecap="round" />
      <path d="M92 120C92 64 132 52 176 52H232C268 52 292 72 292 120" className="stroke-accent-line" strokeWidth="6" strokeLinecap="round" />
      <path d="M150 120C150 150 176 160 204 160" className="stroke-line" strokeWidth="4" strokeLinecap="round" strokeDasharray="2 10" />
      <circle cx="40" cy="120" r="11" className="fill-surface stroke-ink" strokeWidth="5" />
      <circle cx="92" cy="120" r="11" className="fill-surface stroke-ink" strokeWidth="5" />
      <circle cx="176" cy="52" r="12" className="fill-accent" />
      <circle cx="232" cy="52" r="12" className="fill-accent" />
      <circle cx="292" cy="120" r="15" className="fill-success" />
      <circle cx="292" cy="120" r="6" className="fill-surface" />
      <circle cx="204" cy="160" r="8" className="fill-surface stroke-ink-muted" strokeWidth="4" />
      <rect x="196" y="12" width="76" height="22" rx="11" className="fill-accent-soft" />
      <rect x="208" y="20" width="40" height="6" rx="3" className="fill-accent" />
      <rect x="24" y="76" width="52" height="18" rx="9" className="fill-surface-sunken" />
    </svg>
  );
}

interface PodiumProps {
  className?: string;
}

// Three steps and a heart on the highest: the Ranking orders by likes. Theme tokens only.
export function Podium({ className = "" }: PodiumProps) {
  return (
    <svg aria-hidden="true" viewBox="0 0 300 200" fill="none" className={className}>
      <ellipse cx="150" cy="186" rx="132" ry="10" className="fill-surface-sunken" />
      <rect x="28" y="100" width="76" height="82" rx="14" className="fill-surface stroke-line" strokeWidth="4" />
      <rect x="112" y="64" width="76" height="118" rx="14" className="fill-ink" />
      <rect x="196" y="122" width="76" height="60" rx="14" className="fill-surface stroke-line" strokeWidth="4" />
      <rect x="58" y="122" width="16" height="30" rx="5" className="fill-accent-line" />
      <rect x="140" y="88" width="20" height="40" rx="6" className="fill-accent" />
      <rect x="226" y="140" width="16" height="22" rx="5" className="fill-accent-line" />
      <path d="M150 50s-22-13-22-29a12 12 0 0 1 22-7 12 12 0 0 1 22 7c0 16-22 29-22 29z" className="fill-accent" />
      <circle cx="82" cy="70" r="7" className="fill-accent-soft" />
      <circle cx="236" cy="92" r="9" className="fill-success" opacity="0.8" />
      <path d="M210 30l4 8 9 1-6 6 1 9-8-4-8 4 1-9-6-6 9-1z" className="fill-accent-line" />
    </svg>
  );
}

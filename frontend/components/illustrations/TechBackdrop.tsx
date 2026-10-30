// Faint line drawings of code behind every page: brackets, a circuit of commits, a terminal
// prompt. Fixed to the viewport and painted under the content, so wide screens never show a
// blank field; light enough to read over, and theme tokens only.
export function TechBackdrop() {
  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 -z-10 overflow-hidden lg:left-[220px]">
      <svg viewBox="0 0 420 420" fill="none" className="absolute -right-24 -bottom-28 w-[340px] opacity-70 sm:w-[460px] lg:-right-16 lg:-bottom-16">
        <path d="M60 300h80l40-40h90l40 40h70" className="stroke-line" strokeWidth="3" strokeLinecap="round" />
        <path d="M140 300v60h120" className="stroke-line" strokeWidth="3" strokeLinecap="round" />
        <path d="M270 260V170l50-50" className="stroke-accent-line" strokeWidth="3" strokeLinecap="round" />
        <circle cx="140" cy="300" r="9" className="fill-canvas stroke-ink-muted" strokeWidth="3" />
        <circle cx="270" cy="260" r="9" className="fill-accent-line" />
        <circle cx="320" cy="120" r="12" className="fill-accent-soft stroke-accent-line" strokeWidth="3" />
        <circle cx="260" cy="360" r="7" className="fill-success" opacity="0.5" />
        <path d="M118 120l-34 34 34 34M182 120l34 34-34 34M164 108l-28 92" className="stroke-line" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <svg viewBox="0 0 360 240" fill="none" className="absolute top-28 -right-20 hidden w-[320px] opacity-60 xl:block">
        <rect x="20" y="20" width="320" height="200" rx="22" className="stroke-line" strokeWidth="3" />
        <path d="M20 60h320" className="stroke-line" strokeWidth="3" />
        <circle cx="46" cy="40" r="6" className="fill-line" />
        <circle cx="66" cy="40" r="6" className="fill-line" />
        <circle cx="86" cy="40" r="6" className="fill-line" />
        <path d="M48 98l16 14-16 14" className="stroke-accent-line" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
        <rect x="80" y="106" width="130" height="10" rx="5" className="fill-line" />
        <rect x="48" y="144" width="90" height="10" rx="5" className="fill-accent-soft" />
        <rect x="148" y="144" width="110" height="10" rx="5" className="fill-line" />
        <rect x="48" y="174" width="60" height="10" rx="5" className="fill-line" />
      </svg>
    </div>
  );
}

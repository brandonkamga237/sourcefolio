/** Fictional data is never shown without this. */
export function ExampleTag({ label }: { label: string }) {
  return <span className="shrink-0 rounded-tag border border-line bg-canvas px-1.5 py-0.5 text-[12px] text-ink-muted">{label}</span>;
}

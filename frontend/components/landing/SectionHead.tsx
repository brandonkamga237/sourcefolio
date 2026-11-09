/** The title and what it means side by side, so a wide screen is used rather than left empty. */
export function SectionHead({ title, lead }: { title: string; lead: string }) {
  return (
    <div className="grid gap-4 lg:grid-cols-[minmax(0,1.05fr)_minmax(0,1fr)] lg:items-end lg:gap-16">
      <h2 className="t-h2">{title}</h2>
      <p className="text-[18px] leading-relaxed text-ink-soft lg:text-[20px]">{lead}</p>
    </div>
  );
}

// A tiny terminal pushing a portfolio, with a blinking caret: fills the sidebar's empty middle
// and says what SourceFolio is about without a word. Theme tokens only.
export function Terminal() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-2 rounded-card border border-line bg-surface p-3">
      <div className="flex gap-1">
        <span className="size-2 rounded-full bg-line" />
        <span className="size-2 rounded-full bg-line" />
        <span className="size-2 rounded-full bg-line" />
      </div>
      <div className="flex items-center gap-1.5">
        <span className="h-1.5 w-2 rounded-full bg-accent" />
        <span className="h-1.5 w-16 rounded-full bg-ink-muted/50" />
      </div>
      <div className="flex items-center gap-1.5 pl-3.5">
        <span className="h-1.5 w-10 rounded-full bg-success/70" />
        <span className="h-1.5 w-12 rounded-full bg-line" />
      </div>
      <div className="flex items-center gap-1.5 pl-3.5">
        <span className="h-1.5 w-20 rounded-full bg-line" />
      </div>
      <div className="flex items-center gap-1.5">
        <span className="h-1.5 w-2 rounded-full bg-accent" />
        <span className="caret h-3 w-1.5 rounded-sm bg-ink" />
      </div>
    </div>
  );
}

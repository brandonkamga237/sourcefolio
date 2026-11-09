/** Waiting, in the product's own unit: three squares lighting in turn. */
export function SquareSpinner() {
  return (
    <span className="inline-flex gap-[3px]">
      {[0, 1, 2].map((index) => (
        <span key={index} className="size-1.5 rounded-[1.5px] bg-current" style={{ animation: "square-pulse 900ms var(--ease-enter) infinite", animationDelay: `${index * 140}ms` }} />
      ))}
    </span>
  );
}

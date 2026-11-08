import { IconCheck } from "@/components/icons";

interface StepsProps {
  label: string;
  steps: { label: string; done: boolean }[];
}

/** Sign-in, analysis, completion, publication: where the member stands, the current one marked. */
export function Steps({ label, steps }: StepsProps) {
  const current = steps.findIndex((step) => !step.done);
  return (
    <ol aria-label={label} className="flex items-center gap-1.5 overflow-x-auto text-[16px]">
      {steps.map((step, index) => {
        const active = index === current;
        return (
          <li key={step.label} aria-current={active ? "step" : undefined} className="flex shrink-0 items-center gap-1.5">
            {index > 0 ? <span aria-hidden="true" className="h-px w-8 bg-line" /> : null}
            {step.done ? (
              <IconCheck aria-hidden="true" className="size-5 text-success" />
            ) : (
              <span aria-hidden="true" className={`size-3.5 rounded-[4px] ${active ? "bg-ink" : "bg-level-0"}`} />
            )}
            <span className={active ? "font-medium text-ink" : "text-ink-muted"}>{step.label}</span>
          </li>
        );
      })}
    </ol>
  );
}

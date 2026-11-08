import { IconCheck } from "@/components/icons";

interface StepsProps {
  label: string;
  steps: { label: string; done: boolean }[];
}

/** Sign-in, analysis, completion, publication: where the member stands, the current one marked. */
export function Steps({ label, steps }: StepsProps) {
  const current = steps.findIndex((step) => !step.done);
  return (
    <ol aria-label={label} className="flex flex-wrap items-center gap-x-5 gap-y-2">
      {steps.map((step, index) => {
        const active = index === current;
        return (
          <li key={step.label} aria-current={active ? "step" : undefined} className="flex items-center gap-2 text-[14px]">
            {step.done ? (
              <span aria-hidden="true" className="flex size-6 items-center justify-center rounded-full bg-success text-cta-ink">
                <IconCheck className="size-3.5" />
              </span>
            ) : (
              <span
                aria-hidden="true"
                className={`flex size-6 items-center justify-center rounded-full border text-[12px] ${active ? "border-accent text-accent" : "border-line text-ink-muted"}`}
              >
                {index + 1}
              </span>
            )}
            <span className={active ? "font-semibold text-accent" : step.done ? "text-ink-soft" : "text-ink-muted"}>{step.label}</span>
          </li>
        );
      })}
    </ol>
  );
}

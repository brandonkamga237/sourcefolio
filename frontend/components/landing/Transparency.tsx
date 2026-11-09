import { IconCheck, IconEyeOff, IconLogoMark } from "@/components/icons";
import { SectionHead } from "./SectionHead";

interface TransparencyProps {
  title: string;
  lead: string;
  readsTitle: string;
  notReadsTitle: string;
  reads: readonly string[];
  notReads: readonly string[];
}

const dashes = "hidden h-px w-8 shrink-0 lg:block";

/** The boundary drawn as a boundary: what crosses into SourceFolio, and what stays outside. */
export function Transparency({ title, lead, readsTitle, notReadsTitle, reads, notReads }: TransparencyProps) {
  return (
    <section id="transparency" className="wrap flex scroll-mt-20 flex-col gap-8 py-14 lg:gap-10 lg:py-20">
      <SectionHead title={title} lead={lead} />
      <div className="flex flex-col gap-6 lg:flex-row lg:items-center">
        <div className="flex min-w-0 flex-1 flex-col gap-3 rounded-panel border border-success p-6 lg:p-8">
          <p className="t-h3">{readsTitle}</p>
          <ul className="flex flex-col gap-2.5 text-[16px] text-ink-soft lg:text-[17px]">
            {reads.map((item) => <li key={item} className="flex gap-2"><IconCheck aria-hidden="true" className="mt-0.5 size-4 shrink-0 text-success" />{item}</li>)}
          </ul>
        </div>
        <span aria-hidden="true" className={`${dashes} bg-success`} />
        <span aria-hidden="true" className="flex size-16 shrink-0 items-center justify-center self-center rounded-panel border border-line bg-surface"><IconLogoMark className="size-8 text-level-3" /></span>
        <span aria-hidden="true" className={`${dashes} border-t border-dashed border-ink-muted`} />
        <div className="flex min-w-0 flex-1 flex-col gap-3 rounded-panel border border-dashed border-ink-muted p-6 lg:p-8">
          <p className="t-h3">{notReadsTitle}</p>
          <ul className="flex flex-col gap-2.5 text-[16px] text-ink-soft lg:text-[17px]">
            {notReads.map((item) => <li key={item} className="flex gap-2"><IconEyeOff aria-hidden="true" className="mt-0.5 size-4 shrink-0 text-ink-muted" />{item}</li>)}
          </ul>
        </div>
      </div>
    </section>
  );
}

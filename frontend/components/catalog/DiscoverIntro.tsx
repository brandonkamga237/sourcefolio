import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

interface DiscoverIntroProps {
  locale: Locale;
}

// The promise in one line, then the way in for an author. Browsing stays the main action of
// Discover, so the band is short on a phone and the cards start above the fold.
export async function DiscoverIntro({ locale }: DiscoverIntroProps) {
  const t = await getTranslations({ locale });
  const steps = [t("catalog.intro.step1"), t("catalog.intro.step2"), t("catalog.intro.step3")];

  return (
    <section
      aria-labelledby="discover-title"
      className="relative isolate overflow-hidden rounded-panel border border-line bg-surface/80 px-4 py-3.5 backdrop-blur-sm sm:p-6 lg:p-8"
    >
      <div className="flex max-w-xl flex-col gap-1.5 sm:gap-3 md:max-w-none md:pr-[280px] lg:gap-4 lg:pr-[360px]">
        <h1
          id="discover-title"
          className="font-display text-[22px] leading-[1.05] font-bold tracking-[-0.04em] text-balance sm:text-[34px] lg:text-[46px]"
        >
          {t("brand.tagline")}
        </h1>
        <p className="text-[13px] leading-snug text-ink-body sm:text-[15px] sm:leading-normal">{t("catalog.intro.lead")}</p>
        {/* On a phone the top bar already carries the way in. */}
        <div className="hidden flex-wrap items-center gap-x-4 gap-y-2 sm:flex">
          <Link
            href={localizedPath(locale, "/me")}
            className="press flex h-11 items-center rounded-control bg-accent px-4 text-sm font-semibold text-white transition-colors duration-150 hover:bg-accent-hover"
          >
            {t("catalog.intro.cta")}
          </Link>
          <span className="text-[13px] text-ink-muted">{t("catalog.intro.ctaHint")}</span>
        </div>
        <ol className="mt-1 hidden gap-2 sm:flex">
          {steps.map((step, index) => (
            <li key={index} className="flex items-center gap-2 text-[13px] text-ink-soft">
              <span
                aria-hidden="true"
                className="flex size-6 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-[11px] font-semibold text-accent-ink"
              >
                {index + 1}
              </span>
              {step}
            </li>
          ))}
        </ol>
      </div>
      <SiteAndCode />
    </section>
  );
}

const line = "h-1.5 rounded-full";

// A site in front of its source, the promise drawn with shapes only (no text to translate).
function SiteAndCode() {
  return (
    <div aria-hidden="true" className="pointer-events-none absolute top-1/2 right-6 hidden -translate-y-1/2 md:block lg:right-10">
      <div className="relative h-[190px] w-[260px] lg:h-[220px] lg:w-[320px]">
        <div className="absolute top-0 right-0 flex h-[150px] w-[220px] -rotate-3 flex-col gap-2.5 rounded-card bg-ink p-4 shadow-[0_18px_40px_-18px_rgba(18,20,18,0.55)] lg:h-[170px] lg:w-[260px]">
          <div className={`${line} w-1/3 bg-accent`} />
          <div className={`${line} ml-4 w-2/3 bg-white/25`} />
          <div className={`${line} ml-4 w-1/2 bg-success`} />
          <div className={`${line} ml-8 w-3/5 bg-white/25`} />
          <div className={`${line} ml-4 w-2/5 bg-accent-line`} />
          <div className={`${line} w-1/4 bg-accent`} />
        </div>
        <div className="absolute bottom-0 left-0 flex h-[130px] w-[200px] rotate-2 flex-col overflow-hidden rounded-card border border-line bg-surface shadow-[0_18px_40px_-18px_rgba(18,20,18,0.35)] lg:h-[150px] lg:w-[230px]">
          <div className="flex gap-1 border-b border-line-soft px-3 py-2">
            <span className="size-2 rounded-full bg-line" />
            <span className="size-2 rounded-full bg-line" />
            <span className="size-2 rounded-full bg-line" />
          </div>
          <div className="flex flex-1 flex-col gap-2 p-3">
            <div className="h-3 w-3/4 rounded-full bg-ink" />
            <div className={`${line} w-1/2 bg-line`} />
            <div className="mt-auto flex gap-1.5">
              <div className="h-5 w-14 rounded-md bg-accent" />
              <div className="h-5 w-10 rounded-md border border-line" />
            </div>
          </div>
        </div>
        <div className="absolute top-3 left-8 size-10 rounded-full border-[6px] border-accent-line" />
        <div className="absolute right-2 bottom-4 size-5 rotate-12 rounded-md bg-accent-soft" />
      </div>
    </div>
  );
}

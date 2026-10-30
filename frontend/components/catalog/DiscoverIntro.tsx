import Link from "next/link";
import { cookies } from "next/headers";
import { getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { INTRO_COOKIE } from "@/lib/intro";
import { DismissibleIntro } from "./DismissIntro";

interface DiscoverIntroProps {
  locale: Locale;
}

// The promise in one line and the way in for an author, in a slim band: browsing stays the main
// action of Discover, so the cards start right below. Dismissed, it is gone for two weeks and
// the feed's own title becomes the page's heading. Reads a cookie: render inside Suspense.
export async function DiscoverIntro({ locale }: DiscoverIntroProps) {
  const [t, jar] = await Promise.all([getTranslations({ locale }), cookies()]);
  if (jar.get(INTRO_COOKIE)?.value === "hidden") {
    return <h1 className="sr-only">{t("brand.tagline")}</h1>;
  }

  return (
    <DismissibleIntro closeLabel={t("catalog.intro.close")}>
      <section
        aria-labelledby="discover-title"
        className="relative isolate flex items-center gap-4 overflow-hidden rounded-panel border border-line bg-surface/85 py-3.5 pr-12 pl-4 backdrop-blur-sm sm:py-4 sm:pl-5 lg:gap-6 lg:py-5 lg:pr-16 lg:pl-6"
      >
        <CodeMark />
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <h1 id="discover-title" className="font-display text-[19px] leading-tight font-bold tracking-[-0.03em] text-balance sm:text-[24px] lg:text-[28px]">
            {t("brand.tagline")}
          </h1>
          <p className="text-[13px] leading-snug text-ink-body sm:text-sm">{t("catalog.intro.lead")}</p>
        </div>
        {/* On a phone the top bar already carries the way in. */}
        <Link
          href={localizedPath(locale, "/me")}
          className="press hidden h-11 shrink-0 items-center rounded-control bg-accent px-4 text-sm font-semibold text-white transition-colors duration-150 hover:bg-accent-hover sm:flex"
        >
          {t("catalog.intro.cta")}
        </Link>
        <FloatingWindows />
      </section>
    </DismissibleIntro>
  );
}

// The site's mark, large and soft: the band reads as SourceFolio's own at a glance.
function CodeMark() {
  return (
    <span aria-hidden="true" className="hidden size-12 shrink-0 items-center justify-center rounded-[14px] bg-ink text-ink-inverse sm:flex lg:size-14">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" className="size-6 lg:size-7">
        <path d="M8 8l-4 4 4 4M16 8l4 4-4 4" />
      </svg>
    </span>
  );
}

// A code window and a site window drifting behind the call to action: decoration only.
function FloatingWindows() {
  return (
    <div aria-hidden="true" className="pointer-events-none absolute top-1/2 right-52 -z-10 hidden h-[76px] w-[170px] -translate-y-1/2 min-[1360px]:block">
      <div className="drift absolute top-0 right-0 flex h-[54px] w-[112px] -rotate-6 flex-col gap-1.5 rounded-control bg-ink p-2.5 opacity-90">
        <span className="h-1.5 w-1/3 rounded-full bg-accent" />
        <span className="ml-3 h-1.5 w-2/3 rounded-full bg-white/25" />
        <span className="ml-3 h-1.5 w-1/2 rounded-full bg-success" />
      </div>
      <div className="drift absolute bottom-0 left-0 flex h-[48px] w-[96px] rotate-3 flex-col overflow-hidden rounded-control border border-line bg-surface [animation-delay:-3s]">
        <div className="flex gap-1 border-b border-line-soft px-2 py-1.5">
          <span className="size-1.5 rounded-full bg-line" />
          <span className="size-1.5 rounded-full bg-line" />
          <span className="size-1.5 rounded-full bg-line" />
        </div>
        <div className="flex flex-1 flex-col gap-1.5 p-2">
          <span className="h-2 w-3/4 rounded-full bg-ink" />
          <span className="mt-auto h-2.5 w-8 rounded bg-accent" />
        </div>
      </div>
    </div>
  );
}

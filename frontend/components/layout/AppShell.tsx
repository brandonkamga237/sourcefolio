import type { ReactNode } from "react";
import { Suspense } from "react";
import { getLocale, getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { BottomTabs } from "./BottomTabs";
import { HeaderNav } from "./HeaderNav";
import { LanguageSwitch } from "./LanguageSwitch";
import { Logo } from "./Logo";
import { ProjectStars } from "./ProjectStars";
import { PublishLink } from "./PublishLink";

interface AppShellProps {
  children: ReactNode;
}

// A top bar at every width, as GitHub has: the sections in it on a wide screen, in the bottom
// tabs (thumb zone) on a phone. Labels are translated here, on the server, and handed to the
// client components as plain strings: the browser never loads the message catalogue.
export async function AppShell({ children }: AppShellProps) {
  const t = await getTranslations();
  const locale = toLocale(await getLocale());
  const labels = { label: t("nav.label"), rankings: t("nav.rankings"), map: t("nav.map"), myProfile: t("nav.myProfile") };

  return (
    <div className="flex min-h-dvh flex-col">
      <a
        href="#content"
        className="sr-only z-50 rounded-control bg-ink px-4 py-3 text-ink-inverse focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
      >
        {t("a11y.skipToContent")}
      </a>

      <header className="sticky top-0 z-30 h-14 border-b border-line bg-surface-raised/95 backdrop-blur-md lg:h-16">
        <div className="mx-auto flex h-full w-full max-w-6xl items-center gap-6 px-4 lg:px-8">
          <Logo />
          <HeaderNav locale={locale} labels={labels} />
          <div className="ml-auto flex items-center gap-2">
            <div className="hidden lg:block">
              <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
            </div>
            <div className="hidden sm:block">
              <Suspense fallback={null}>
                <ProjectStars locale={locale} compact />
              </Suspense>
            </div>
            <PublishLink
              locale={locale}
              label={t("nav.publish")}
              className="press flex h-10 items-center rounded-control bg-cta px-4 text-[14px] font-semibold text-cta-ink hover:bg-cta-hover"
            />
          </div>
        </div>
      </header>

      <main id="content" className="min-w-0 flex-1 pb-[calc(3.5rem+env(safe-area-inset-bottom))] lg:pb-0">
        {children}
      </main>

      <BottomTabs locale={locale} labels={labels} />
    </div>
  );
}

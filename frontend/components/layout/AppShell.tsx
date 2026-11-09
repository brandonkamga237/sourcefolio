import { Suspense, ViewTransition, type ReactNode } from "react";
import { getLocale, getTranslations } from "next-intl/server";
import { NavigationProgress } from "@/components/motion/NavigationProgress";
import { PointerLight } from "@/components/motion/PointerLight";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { Toaster } from "@/components/ui/Toast";
import { toLocale } from "@/i18n/routing";
import { BottomTabs } from "./BottomTabs";
import { Footer } from "./Footer";
import { HeaderNav } from "./HeaderNav";
import { LanguageSwitch } from "./LanguageSwitch";
import { Logo } from "./Logo";
import { PublishLink } from "./PublishLink";
import { SiteHeader } from "./SiteHeader";

// Labels are translated here, on the server, and handed to the client components as plain
// strings: the browser never loads the message catalogue.
export async function AppShell({ children }: { children: ReactNode }) {
  const t = await getTranslations();
  const locale = toLocale(await getLocale());
  const labels = { label: t("nav.label"), home: t("nav.home"), rankings: t("nav.rankings"), map: t("nav.map"), recruiters: t("nav.recruiters"), me: t("nav.me") };

  return (
    <div className="flex min-h-dvh flex-col">
      <a href="#content" className="sr-only z-50 rounded-control bg-ink px-4 py-3 text-ink-inverse focus:not-sr-only focus:fixed focus:top-3 focus:left-3">{t("a11y.skipToContent")}</a>
      <NavigationProgress />
      <SiteHeader>
        <div className="wrap flex h-14 items-center gap-6">
          <Logo />
          <HeaderNav locale={locale} labels={labels} />
          <div className="ml-auto flex items-center gap-1">
            <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
            <ThemeToggle label={t("theme.label")} names={{ system: t("theme.system"), light: t("theme.light"), dark: t("theme.dark") }} />
            <PublishLink locale={locale} label={t("nav.publish")} className="btn btn-primary ml-2 max-lg:hidden" />
          </div>
        </div>
      </SiteHeader>
      <main id="content" className="min-w-0 flex-1">
        <ViewTransition>{children}</ViewTransition>
      </main>
      <div className="pb-[calc(3.5rem+env(safe-area-inset-bottom))] lg:pb-0">
        <Suspense fallback={null}>
          <Footer locale={locale} />
        </Suspense>
      </div>
      <BottomTabs locale={locale} labels={labels} />
      <Toaster />
      <PointerLight />
    </div>
  );
}

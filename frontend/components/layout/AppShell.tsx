import type { ReactNode } from "react";
import { getLocale, getTranslations } from "next-intl/server";
import { ScrollEffects } from "@/components/motion/ScrollEffects";
import { toLocale } from "@/i18n/routing";
import { BottomTabs } from "./BottomTabs";
import { Footer } from "./Footer";
import { HeaderNav } from "./HeaderNav";
import { Logo } from "./Logo";
import { PublishLink } from "./PublishLink";
import { SiteHeader } from "./SiteHeader";

interface AppShellProps {
  children: ReactNode;
}

// Labels are translated here, on the server, and handed to the client components as plain
// strings: the browser never loads the message catalogue.
export async function AppShell({ children }: AppShellProps) {
  const t = await getTranslations();
  const locale = toLocale(await getLocale());
  const labels = {
    label: t("nav.label"),
    home: t("nav.home"),
    explore: t("nav.explore"),
    recruiters: t("nav.recruiters"),
    myProfile: t("nav.myProfile"),
  };

  return (
    <div className="flex min-h-dvh flex-col">
      <a href="#content" className="sr-only z-50 rounded-control bg-ink px-4 py-3 text-ink-inverse focus:not-sr-only focus:fixed focus:top-3 focus:left-3">
        {t("a11y.skipToContent")}
      </a>

      <SiteHeader>
        <div className="wrap flex h-16 items-center gap-8 lg:h-[72px]">
          <Logo />
          <HeaderNav locale={locale} labels={labels} />
          <div className="ml-auto">
            <PublishLink locale={locale} label={t("nav.publish")} className="btn btn-primary" />
          </div>
        </div>
      </SiteHeader>

      <main id="content" className="min-w-0 flex-1">
        {children}
      </main>

      <div className="dark-band pb-[calc(3.5rem+env(safe-area-inset-bottom))] lg:pb-0">
        <Footer locale={locale} />
      </div>

      <BottomTabs locale={locale} labels={labels} />
      <ScrollEffects />
    </div>
  );
}

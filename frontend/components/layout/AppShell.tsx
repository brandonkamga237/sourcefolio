import type { ReactNode } from "react";
import { getLocale, getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import Link from "next/link";
import { localizedPath } from "@/i18n/paths";
import { BottomTabs } from "./BottomTabs";
import { LanguageSwitch } from "./LanguageSwitch";
import { Logo } from "./Logo";
import { SidebarNav } from "./SidebarNav";

interface AppShellProps {
  children: ReactNode;
}

// Desktop (lg): sidebar on the left. Phone: slim top bar and the tabs at the bottom, in the
// thumb zone. Same four sections in both. Labels are translated here, on the server, and
// handed to the client components as plain strings: the browser never loads the message
// catalogue or the ICU parser just to show four words.
export async function AppShell({ children }: AppShellProps) {
  const t = await getTranslations();
  const locale = toLocale(await getLocale());
  const labels = {
    label: t("nav.label"),
    discover: t("nav.discover"),
    search: t("nav.search"),
    portfolio: t("nav.portfolio"),
    me: t("nav.me"),
  };

  return (
    <div className="lg:flex lg:min-h-dvh">
      <a
        href="#content"
        className="sr-only z-50 rounded-control bg-ink px-4 py-3 text-ink-inverse focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
      >
        {t("a11y.skipToContent")}
      </a>

      <aside className="sticky top-0 hidden h-dvh w-[220px] shrink-0 flex-col gap-7 border-r border-line bg-surface-raised px-4 py-6 lg:flex">
        <div className="px-2">
          <Logo />
        </div>
        <SidebarNav locale={locale} labels={labels} />
        <div className="mt-auto flex flex-col gap-3">
          <Link
            href={localizedPath(locale, "/me/portfolio")}
            className="flex h-11 items-center justify-center rounded-control bg-ink text-sm font-semibold text-ink-inverse"
          >
            {t("nav.publish")}
          </Link>
          <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
        </div>
      </aside>

      <header className="sticky top-0 z-30 flex h-14 items-center border-b border-line bg-surface/95 px-4 backdrop-blur lg:hidden">
        <Logo />
      </header>

      <main id="content" className="min-w-0 flex-1 pb-[calc(3.5rem+env(safe-area-inset-bottom))] lg:pb-0">
        {children}
      </main>

      <BottomTabs locale={locale} labels={labels} />
    </div>
  );
}

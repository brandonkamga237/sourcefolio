import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { Suspense } from "react";
import { ThemeChoice } from "@/components/theme/ThemeChoice";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { LanguageSwitch } from "./LanguageSwitch";
import { Logo } from "./Logo";
import { ProjectStars } from "./ProjectStars";

const column = "flex flex-col gap-1";
const heading = "py-1 text-[15px] font-medium text-ink";
const item = "flex min-h-11 items-center text-[16px] text-ink-muted transition-colors duration-150 hover:text-ink lg:min-h-9";

/** What a page has no room for: the way to everything, the language and the theme. */
export async function Footer({ locale }: { locale: Locale }) {
  const t = await getTranslations({ locale });
  const links = [
    { href: "/rankings", label: t("nav.rankings") },
    { href: "/map", label: t("nav.mapLink") },
    { href: "/recruiters", label: t("nav.recruiters") },
  ];

  return (
    <footer className="border-t border-line">
      <div className="wrap grid gap-10 py-14 lg:grid-cols-[minmax(0,1.5fr)_1fr_1fr_minmax(0,1.2fr)] lg:py-20">
        <div className="flex flex-col items-start gap-2">
          <Logo />
          <p className="max-w-sm text-[15px] leading-relaxed text-ink-muted">{t("footer.honest")}</p>
        </div>
        <nav aria-label={t("footer.product")} className={column}>
          <p className={heading}>{t("footer.product")}</p>
          {links.map((link) => (
            <Link key={link.href} href={localizedPath(locale, link.href)} className={item}>{link.label}</Link>
          ))}
        </nav>
        <nav aria-label={t("footer.project")} className={column}>
          <p className={heading}>{t("footer.project")}</p>
          <Link href={`${localizedPath(locale, "/")}#method`} className={item}>{t("footer.method")}</Link>
          <div className="pt-1">
            <Suspense fallback={null}>
              <ProjectStars locale={locale} />
            </Suspense>
          </div>
        </nav>
        <div className="flex flex-col gap-3">
          <p className={heading}>{t("footer.preferences")}</p>
          <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
          <ThemeChoice label={t("theme.label")} names={{ system: t("theme.system"), light: t("theme.light"), dark: t("theme.dark") }} />
        </div>
      </div>
    </footer>
  );
}

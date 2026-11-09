import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { Suspense } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { Logo } from "./Logo";
import { ProjectStars } from "./ProjectStars";

const item = "flex min-h-11 items-center text-[14px] text-ink-soft transition-colors duration-(--dur-1) hover:text-ink lg:min-h-8";

export async function Footer({ locale }: { locale: Locale }) {
  const t = await getTranslations({ locale, namespace: "home.footer" });
  const nav = await getTranslations({ locale, namespace: "nav" });
  const columns = [
    { title: t("explore"), links: [["/rankings", nav("rankings")], ["/map", nav("map")]] },
    { title: t("product"), links: [["/me", nav("publish")], ["/recruiters", nav("recruiters")]] },
    { title: t("trust"), links: [["/#transparency", t("transparency")], ["/#faq", t("faq")]] },
  ];
  return (
    <footer className="border-t border-line bg-surface-raised">
      <div className="wrap grid gap-10 py-12 lg:grid-cols-[1.6fr_1fr_1fr_1fr]">
        <div className="flex flex-col items-start gap-3">
          <Logo />
          <p className="max-w-xs text-[13px] leading-relaxed text-ink-muted">{t("honest")}</p>
          <Suspense fallback={null}>
            <ProjectStars locale={locale} compact />
          </Suspense>
        </div>
        {columns.map((column) => (
          <nav key={column.title} aria-label={column.title} className="flex flex-col">
            <p className="pb-1 text-[13px] font-semibold">{column.title}</p>
            {column.links.map(([href, label]) => (
              <Link key={href} href={localizedPath(locale, href)} className={item}>{label}</Link>
            ))}
          </nav>
        ))}
      </div>
    </footer>
  );
}

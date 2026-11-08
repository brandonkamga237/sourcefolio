import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

interface ExploreHeaderProps {
  locale: Locale;
  current: "rankings" | "map";
  lead: string;
}

/** Rankings and map are two views of the same people: one section, one heading, two addresses. */
export async function ExploreHeader({ locale, current, lead }: ExploreHeaderProps) {
  const t = await getTranslations({ locale, namespace: "nav" });
  const views = [
    { key: "rankings", href: "/rankings", label: t("rankings") },
    { key: "map", href: "/map", label: t("map") },
  ] as const;
  return (
    <header className="animate-rise flex flex-col gap-5">
      <h1 className="text-[52px] leading-[1] font-semibold tracking-[-0.04em] lg:text-[88px]">{t("explore")}</h1>
      <p className="max-w-3xl text-[18px] leading-relaxed text-ink-soft lg:text-[21px]">{lead}</p>
      <ul className="flex w-fit gap-1 rounded-segment bg-surface-sunken p-1">
        {views.map((view) => (
          <li key={view.key}>
            <Link
              href={localizedPath(locale, view.href)}
              aria-current={view.key === current ? "page" : undefined}
              className={`flex min-h-12 items-center rounded-[6px] px-6 text-[17px] transition-colors duration-150 ${view.key === current ? "bg-surface font-medium text-ink shadow-sm" : "text-ink-muted hover:text-ink"}`}
            >
              {view.label}
            </Link>
          </li>
        ))}
      </ul>
    </header>
  );
}

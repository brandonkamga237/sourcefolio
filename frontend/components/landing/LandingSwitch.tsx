import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

interface LandingSwitchProps {
  locale: Locale;
  current: "tech" | "recruit";
}

// Links, not a toggle: each audience has its own address, to share and to index.
export async function LandingSwitch({ locale, current }: LandingSwitchProps) {
  const t = await getTranslations({ locale, namespace: "landing.switch" });
  const options = [
    { key: "tech", href: "/", label: t("tech") },
    { key: "recruit", href: "/recruiters", label: t("recruit") },
  ] as const;
  return (
    <nav aria-label={t("label")} className="w-fit">
      <ul className="flex gap-1 rounded-segment border border-line bg-surface-raised p-1">
        {options.map((option) => (
          <li key={option.key}>
            <Link
              href={localizedPath(locale, option.href)}
              aria-current={option.key === current ? "page" : undefined}
              className={`flex min-h-11 items-center rounded-control px-3.5 text-[14px] whitespace-nowrap ${
                option.key === current ? "border border-line bg-surface font-semibold text-ink shadow-sm" : "text-ink-soft hover:text-ink"
              }`}
            >
              {option.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}

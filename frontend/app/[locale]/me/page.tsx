import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "pages.me" });
  return {
    title: t("title"),
    alternates: { canonical: `/${locale}/me`, languages: { fr: "/fr/me", en: "/en/me" } },
    robots: { index: false },
  };
}

export default async function MePage({ params }: PageProps<"/[locale]/me">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "pages.me" });

  return (
    <div className="px-4 py-6 lg:px-10 lg:py-8">
      <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">{t("title")}</h1>
    </div>
  );
}

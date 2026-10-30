import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { CatalogScreen } from "@/components/catalog/CatalogScreen";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/recent">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "catalog.sort" });
  return {
    title: t("recent"),
    alternates: { canonical: `/${locale}/recent`, languages: { fr: "/fr/recent", en: "/en/recent", "x-default": "/en/recent" } },
  };
}

export default async function RecentPage({ params }: PageProps<"/[locale]/recent">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  return <CatalogScreen locale={locale} sort="recent" />;
}

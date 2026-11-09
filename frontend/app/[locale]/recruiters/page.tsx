import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Landing } from "@/components/landing/Landing";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/recruiters">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "recruiters" });
  return {
    title: t("meta.title"),
    description: t("hero.lead"),
    alternates: { canonical: `/${locale}/recruiters`, languages: { fr: "/fr/recruiters", en: "/en/recruiters", "x-default": "/en/recruiters" } },
  };
}

export default async function RecruitersPage({ params }: PageProps<"/[locale]/recruiters">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  return <Landing locale={locale} audience="recruit" />;
}

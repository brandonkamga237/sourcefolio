import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { MyPortfolio, MyPortfolioSkeleton } from "@/components/dashboard/MyPortfolio";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me/portfolio">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "pages.portfolio" });
  return {
    title: t("title"),
    alternates: { canonical: `/${locale}/me/portfolio`, languages: { fr: "/fr/me/portfolio", en: "/en/me/portfolio" } },
    robots: { index: false },
  };
}

export default async function MyPortfolioPage({ params }: PageProps<"/[locale]/me/portfolio">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-8 px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <Suspense fallback={<MyPortfolioSkeleton />}>
        <MyPortfolio locale={locale} />
      </Suspense>
    </div>
  );
}

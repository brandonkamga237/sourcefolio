import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { MeScreen, MeScreenSkeleton } from "@/components/me/MeScreen";
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
  return (
    <div className="max-w-6xl px-4 py-5 lg:px-8 lg:py-7">
      <Suspense fallback={<MeScreenSkeleton />}>
        <MeScreen locale={locale} />
      </Suspense>
    </div>
  );
}

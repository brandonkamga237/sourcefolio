import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { MyProfileScreen, MyProfileScreenSkeleton } from "@/components/myprofile/MyProfileScreen";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "myProfile" });
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
    <div className="wrap py-8 lg:py-12">
      <Suspense fallback={<MyProfileScreenSkeleton />}>
        <MyProfileScreen locale={locale} />
      </Suspense>
    </div>
  );
}

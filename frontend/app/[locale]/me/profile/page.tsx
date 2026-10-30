import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { ProfileScreen, ProfileScreenSkeleton } from "@/components/profile/ProfileScreen";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me/profile">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "profile" });
  return {
    title: t("summaryTitle"),
    alternates: { canonical: `/${locale}/me/profile`, languages: { fr: "/fr/me/profile", en: "/en/me/profile" } },
    robots: { index: false },
  };
}

export default async function ProfilePage({ params }: PageProps<"/[locale]/me/profile">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  return (
    <div className="mx-auto flex max-w-xl flex-col px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <Suspense fallback={<ProfileScreenSkeleton />}>
        <ProfileScreen locale={locale} />
      </Suspense>
    </div>
  );
}

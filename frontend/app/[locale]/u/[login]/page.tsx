import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { ProfileView } from "@/components/contribution/ProfileView";
import { JsonLd } from "@/components/seo/JsonLd";
import { BackToRankings } from "@/components/layout/BackToRankings";
import { CopyButton } from "@/components/ui/CopyButton";
import { toLocale } from "@/i18n/routing";
import type { Sector } from "@/lib/globe";
import { getPublicProfile } from "@/services/public-profiles";
import type { ProfileCard } from "@/services/contribution-types";

// Rendered whole before answering: an unknown or private profile must get a real 404, and a
// link preview crawler must find the metadata in the first response.
export const instant = false;

export async function generateMetadata({ params }: PageProps<"/[locale]/u/[login]">): Promise<Metadata> {
  const { locale: raw, login } = await params;
  const locale = toLocale(raw);
  const [t, card] = await Promise.all([getTranslations({ locale, namespace: "pages.profile" }), getPublicProfile(login)]);
  if (!card) notFound();
  const name = card.name ?? card.login;
  return {
    title: t("metaTitle", { name }),
    description: t("metaDescription", { name }),
    alternates: {
      canonical: `/${locale}/u/${card.login}`,
      languages: { fr: `/fr/u/${card.login}`, en: `/en/u/${card.login}`, "x-default": `/en/u/${card.login}` },
    },
    openGraph: { type: "profile", locale: locale === "fr" ? "fr_FR" : "en_US", url: `/${locale}/u/${card.login}` },
  };
}

function structuredData(card: ProfileCard) {
  return {
    "@context": "https://schema.org",
    "@type": "ProfilePage",
    mainEntity: {
      "@type": "Person",
      name: card.name ?? card.login,
      alternateName: card.login,
      sameAs: [card.indicators.proofs.profile, ...(card.linkedInUrl ? [card.linkedInUrl] : [])],
    },
  };
}

export default async function PublicProfilePage({ params }: PageProps<"/[locale]/u/[login]">) {
  const { locale: raw, login } = await params;
  const locale = toLocale(raw);
  setRequestLocale(locale);
  const [nav, my, card] = await Promise.all([getTranslations({ locale, namespace: "nav" }), getTranslations({ locale, namespace: "myProfile" }), getPublicProfile(login)]);
  if (!card) notFound();
  return (
    <div className="wrap flex flex-col gap-4 py-6 lg:py-10">
      <BackToRankings locale={locale} label={nav("rankings")} />
      <JsonLd data={structuredData(card)} />
      <ProfileView
        login={card.login}
        name={card.name}
        country={card.country}
        city={card.city}
        sector={card.sector ? my(`sectors.${card.sector as Sector}`) : null}
        actions={<CopyButton value={`${process.env.SITE_URL ?? "http://localhost:3000"}/${locale}/u/${card.login}`} label={my("copy")} copied={my("copied")} />}
        bio={card.bio}
        linkedInUrl={card.linkedInUrl}
        indicators={card.indicators}
      />
    </div>
  );
}

import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { ProfileView } from "@/components/contribution/ProfileView";
import { toLocale } from "@/i18n/routing";
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

// "<" escaped so a value can never close the script element.
function jsonLd(card: ProfileCard): string {
  return JSON.stringify({
    "@context": "https://schema.org",
    "@type": "ProfilePage",
    mainEntity: {
      "@type": "Person",
      name: card.name ?? card.login,
      alternateName: card.login,
      sameAs: [card.indicators.proofs.profile, ...(card.linkedInUrl ? [card.linkedInUrl] : [])],
    },
  }).replace(/</g, "\\u003c");
}

export default async function PublicProfilePage({ params }: PageProps<"/[locale]/u/[login]">) {
  const { locale: raw, login } = await params;
  setRequestLocale(toLocale(raw));
  const card = await getPublicProfile(login);
  if (!card) notFound();
  return (
    <div className="mx-auto w-full max-w-6xl px-4 py-6 lg:px-8 lg:py-10">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(card) }} />
      <ProfileView
        login={card.login}
        name={card.name}
        country={card.country}
        city={card.city}
        bio={card.bio}
        linkedInUrl={card.linkedInUrl}
        indicators={card.indicators}
      />
    </div>
  );
}

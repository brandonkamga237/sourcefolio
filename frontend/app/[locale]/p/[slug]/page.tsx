import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { IconCode, IconExternal } from "@/components/icons";
import { LikeButton } from "@/components/likes/LikeButton";
import { Preview } from "@/components/catalog/Preview";
import { StarCount } from "@/components/stars/StarCount";
import { CommitGraph } from "@/components/illustrations/CommitGraph";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import { getPublicShowcase, type PublicShowcase } from "@/services/public-showcases";

// Rendered whole before answering, not streamed behind a placeholder: an unknown address must
// get a real 404 status, and the WhatsApp or LinkedIn crawler must find the metadata in the
// first response. The showcase comes from a cache, so the wait stays short.
export const instant = false;

export async function generateMetadata({ params }: PageProps<"/[locale]/p/[slug]">): Promise<Metadata> {
  const { locale: raw, slug } = await params;
  const locale = toLocale(raw);
  const [t, showcase] = await Promise.all([
    getTranslations({ locale, namespace: "showcasePage" }),
    getPublicShowcase(slug),
  ]);
  if (!showcase) notFound();
  const values = { site: siteLabel(showcase.siteUrl), login: showcase.ownerLogin };
  return {
    title: t("metaTitle", values),
    description: t("metaDescription", values),
    alternates: {
      canonical: `/${locale}/p/${slug}`,
      languages: { fr: `/fr/p/${slug}`, en: `/en/p/${slug}`, "x-default": `/en/p/${slug}` },
    },
    openGraph: { type: "profile", locale: locale === "fr" ? "fr_FR" : "en_US", url: `/${locale}/p/${slug}` },
    // Hidden from the catalogue while its site is down: search engines should not list it either.
    robots: showcase.status === "PUBLISHED" ? undefined : { index: false },
  };
}

// "<" escaped so a value can never close the script element.
function jsonLd(showcase: PublicShowcase): string {
  return JSON.stringify({
    "@context": "https://schema.org",
    "@type": "ProfilePage",
    mainEntity: {
      "@type": "Person",
      name: showcase.ownerLogin,
      url: showcase.siteUrl,
      sameAs: [showcase.siteUrl, showcase.ownerUrl],
    },
  }).replace(/</g, "\\u003c");
}

const secondaryLink =
  "press flex h-11 items-center justify-center gap-1.5 rounded-control border border-line bg-surface px-4 text-sm font-semibold text-ink";

// The page most visitors land on first, from a shared link on a phone. One main action, the
// site; the code next to it is the promise of SourceFolio. External sites open in a new tab.
export default async function ShowcasePage({ params }: PageProps<"/[locale]/p/[slug]">) {
  const { locale: raw, slug } = await params;
  const locale = toLocale(raw);
  setRequestLocale(locale);
  const [t, tl, ts, showcase] = await Promise.all([
    getTranslations({ locale, namespace: "showcasePage" }),
    getTranslations({ locale, namespace: "likes" }),
    getTranslations({ locale, namespace: "stars" }),
    getPublicShowcase(slug),
  ]);
  if (!showcase) notFound();

  return (
    <article className="mx-auto flex max-w-xl flex-col gap-6 px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(showcase) }} />

      {showcase.status !== "PUBLISHED" ? (
        <p role="status" className="rounded-card border border-line bg-surface-sunken p-3.5 text-sm leading-normal text-ink">
          {t(`banner.${showcase.status}`)}
        </p>
      ) : null}

      <div className="overflow-hidden rounded-card border border-line">
        {showcase.previewVideoUrl && showcase.previewImageUrl ? (
          <video
            src={showcase.previewVideoUrl}
            poster={showcase.previewImageUrl}
            controls
            playsInline
            preload="none"
            className="aspect-[16/10] w-full bg-surface-sunken object-cover"
          />
        ) : (
          <Preview slug={showcase.slug} siteUrl={showcase.siteUrl} imageUrl={showcase.previewImageUrl} size="large" sizes="(min-width: 640px) 576px, 100vw" priority />
        )}
      </div>

      <header className="flex flex-col gap-2">
        <h1 className="font-display text-[32px] leading-[1.05] font-bold tracking-[-0.035em] break-all lg:text-[44px]">
          {siteLabel(showcase.siteUrl)}
        </h1>
        <p className="text-[15px] text-ink-body">
          {t.rich("by", {
            login: showcase.ownerLogin,
            profile: (chunks) => (
              <a href={showcase.ownerUrl} target="_blank" rel="noopener noreferrer" className="font-semibold text-ink underline underline-offset-2">
                {chunks}
              </a>
            ),
          })}
        </p>
        <p className="flex flex-wrap items-center gap-x-3 gap-y-1 font-mono text-xs text-ink-soft">
          <span className="break-all">{showcase.repository}</span>
          {showcase.repositoryStars != null ? (
            <a href={showcase.repositoryUrl} target="_blank" rel="noopener noreferrer" className="font-semibold text-ink">
              <StarCount count={showcase.repositoryStars} locale={locale} label={ts("repository", { count: showcase.repositoryStars })} />
            </a>
          ) : null}
        </p>
        {showcase.publishedAt ? (
          <p className="text-[13px] text-ink-muted">{t("publishedOn", { date: new Date(showcase.publishedAt) })}</p>
        ) : null}
      </header>

      <div className="flex flex-col gap-2">
        <a
          href={showcase.siteUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="press flex h-12 items-center justify-center gap-2 rounded-control bg-accent px-5 text-[15px] font-semibold text-white"
        >
          {t("visitSite")}
          <IconExternal className="size-4" />
        </a>
        <div className="flex gap-2">
          <a href={showcase.repositoryUrl} target="_blank" rel="noopener noreferrer" className={`${secondaryLink} flex-1`}>
            <IconCode className="size-4" />
            {t("viewCode")}
          </a>
          <Suspense fallback={<div className="h-[46px] w-16 rounded-control border border-line" />}>
            <LikeButton
              slug={showcase.slug}
              showCount
              labels={{ like: tl.raw("like") as string, unlike: tl.raw("unlike") as string, own: tl.raw("own") as string, failed: tl("failed") }}
            />
          </Suspense>
        </div>
      </div>

      <aside className="relative isolate mt-4 flex flex-col gap-2 overflow-hidden rounded-card border border-line bg-surface p-4">
        <CommitGraph className="pointer-events-none absolute -right-10 -bottom-8 -z-10 w-[200px] opacity-50" />
        <h2 className="text-[15px] font-semibold">{t("ctaTitle")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("ctaLead")}</p>
        <Link href={localizedPath(locale, "/me")} className="press flex h-11 w-fit items-center rounded-control border border-ink px-4 text-sm font-semibold text-ink">
          {t("ctaLink")}
        </Link>
      </aside>
    </article>
  );
}

import { getTranslations } from "next-intl/server";
import Link from "next/link";
import { Preview } from "@/components/catalog/Preview";
import { IconCode, IconExternal } from "@/components/icons";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import type { MyShowcase } from "@/services/showcases";
import { ShareButton } from "./ShareButton";

interface PublishedShowcaseProps {
  locale: Locale;
  showcase: MyShowcase;
}

const iconLink = "flex size-11 shrink-0 items-center justify-center rounded-control border border-line bg-surface text-ink";

// The author's view of their portfolio: what visitors see on the card, its status, and the
// actions they come here for. Anything but PUBLISHED means visitors no longer find it in the
// catalogue.
export async function PublishedShowcase({ locale, showcase }: PublishedShowcaseProps) {
  const t = await getTranslations({ locale, namespace: "myShowcase" });
  const published = showcase.status === "PUBLISHED";
  const pagePath = localizedPath(locale, `/p/${showcase.slug}`);

  return (
    <section aria-labelledby="my-showcase" className="grid overflow-hidden rounded-card border border-line bg-surface sm:grid-cols-[minmax(0,240px)_1fr]">
      <Preview
        slug={showcase.slug}
        siteUrl={showcase.siteUrl}
        imageUrl={showcase.previewImageUrl}
        videoUrl={showcase.previewVideoUrl}
        aspectClass="aspect-[16/7] sm:aspect-auto sm:h-full sm:min-h-[150px]"
        sizes="(min-width: 640px) 240px, 100vw"
      />
      <div className="flex min-w-0 flex-col gap-3 p-4">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${published ? "bg-accent-soft text-accent-ink" : "bg-surface-sunken text-ink"}`}>
            {t(`status.${showcase.status}`)}
          </span>
          {showcase.publishedAt ? (
            <span className="text-xs text-ink-muted">{t("publishedOn", { date: new Date(showcase.publishedAt) })}</span>
          ) : null}
        </div>
        <div className="flex min-w-0 flex-col gap-0.5">
          <h2 id="my-showcase" className="truncate font-display text-[22px] leading-tight font-bold tracking-[-0.025em]">
            {siteLabel(showcase.siteUrl)}
          </h2>
          <p className="truncate font-mono text-xs text-ink-muted">{showcase.repository}</p>
        </div>
        <div className="mt-auto flex flex-wrap items-start gap-2">
          {published ? (
            <ShareButton
              path={pagePath}
              title={t("shareTitle", { site: siteLabel(showcase.siteUrl) })}
              label={t("share")}
              copiedLabel={t("shareCopied")}
            />
          ) : null}
          <Link href={pagePath} className="press flex h-11 items-center rounded-control border border-ink px-3.5 text-sm font-semibold text-ink">
            {t("viewPage")}
          </Link>
          <a href={showcase.siteUrl} target="_blank" rel="noopener noreferrer" aria-label={t("visitSite")} title={t("visitSite")} className={iconLink}>
            <IconExternal className="size-4" />
          </a>
          <a href={showcase.repositoryUrl} target="_blank" rel="noopener noreferrer" aria-label={t("viewCode")} title={t("viewCode")} className={iconLink}>
            <IconCode className="size-4" />
          </a>
        </div>
      </div>
    </section>
  );
}

import { getTranslations } from "next-intl/server";
import type { Locale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import type { MyShowcase } from "@/services/showcases";
import { ShowcaseLinks } from "./ShowcaseLinks";

interface PublishedShowcaseProps {
  locale: Locale;
  showcase: MyShowcase;
}

// The status is the author's private view of their portfolio (never a public score): anything
// but PUBLISHED means visitors no longer find it in the catalogue.
export async function PublishedShowcase({ locale, showcase }: PublishedShowcaseProps) {
  const t = await getTranslations({ locale, namespace: "myShowcase" });
  const published = showcase.status === "PUBLISHED";

  return (
    <section aria-labelledby="my-showcase" className="flex flex-col gap-3 rounded-card border border-line bg-surface p-4">
      <div className="flex flex-col gap-2">
        <span
          className={`w-fit rounded-full px-2.5 py-1 text-xs font-semibold ${
            published ? "bg-accent-soft text-accent-ink" : "bg-surface-sunken text-ink"
          }`}
        >
          {t(`status.${showcase.status}`)}
        </span>
        <h2 id="my-showcase" className="font-display text-2xl font-bold tracking-[-0.025em] break-all">
          {siteLabel(showcase.siteUrl)}
        </h2>
        <p className="font-mono text-xs text-ink-soft">{showcase.repository}</p>
        {showcase.publishedAt ? (
          <p className="text-[13px] text-ink-muted">{t("publishedOn", { date: new Date(showcase.publishedAt) })}</p>
        ) : null}
      </div>
      {published ? <p className="text-sm leading-normal text-ink-body">{t("publishedLead")}</p> : null}
      <ShowcaseLinks
        siteUrl={showcase.siteUrl}
        repositoryUrl={showcase.repositoryUrl}
        visitLabel={t("visitSite")}
        codeLabel={t("viewCode")}
      />
    </section>
  );
}

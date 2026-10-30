import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { ShareButton } from "@/components/dashboard/ShareButton";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import type { MyShowcase } from "@/services/showcases";

interface JourneyCardProps {
  locale: Locale;
  showcase: MyShowcase | null;
  profileComplete: boolean;
}

type StepKey = "account" | "publish" | "preview" | "profile";

const primary =
  "press flex h-11 items-center justify-center rounded-control bg-accent px-4 text-sm font-semibold text-white transition-colors duration-150 hover:bg-accent-hover";

// Where the member stands and the one thing to do next (spec: one main action per screen).
// The steps follow the order a new member meets them; the last action, once all are done, is
// sharing the page, since that is what brings visitors.
export async function JourneyCard({ locale, showcase, profileComplete }: JourneyCardProps) {
  const t = await getTranslations({ locale });
  const steps: { key: StepKey; done: boolean; href: string }[] = [
    { key: "account", done: true, href: "#me-portfolio" },
    { key: "publish", done: showcase !== null, href: "#me-portfolio" },
    { key: "preview", done: showcase?.previewImageUrl != null, href: "#me-preview" },
    { key: "profile", done: profileComplete, href: localizedPath(locale, "/me/profile") },
  ];
  const doneCount = steps.filter((step) => step.done).length;
  // The account step is done by being here, so the next step is always one of the others.
  const next = steps.find((step): step is { key: Exclude<StepKey, "account">; done: boolean; href: string } => !step.done);
  const pagePath = showcase ? localizedPath(locale, `/p/${showcase.slug}`) : null;

  return (
    <section aria-labelledby="journey-title" className="flex flex-col gap-4 rounded-panel border border-line bg-surface p-4 sm:p-5">
      <div className="flex flex-col gap-1">
        <h2 id="journey-title" className="font-display text-[20px] leading-tight font-bold tracking-[-0.025em]">
          {next ? t("journey.title") : t("journey.doneTitle")}
        </h2>
        <p className="text-[13px] text-ink-muted">{t("journey.progress", { done: doneCount, total: steps.length })}</p>
      </div>

      <ol className="grid grid-cols-4 gap-1.5">
        {steps.map((step) => {
          const current = step.key === next?.key;
          return (
            <li key={step.key} className="flex min-w-0 flex-col gap-2">
              <span
                aria-hidden="true"
                className={`h-1.5 rounded-full transition-colors duration-300 ${step.done ? "bg-accent" : current ? "bg-accent-line" : "bg-surface-sunken"}`}
              />
              <span className={`text-[11px] leading-tight sm:text-xs ${step.done ? "text-ink" : current ? "font-semibold text-accent-ink" : "text-ink-muted"}`}>
                <span className="sr-only">{step.done ? t("journey.stateDone") : t("journey.stateTodo")} </span>
                {t(`journey.steps.${step.key}`)}
              </span>
            </li>
          );
        })}
      </ol>

      {next ? (
        <div className="flex flex-col gap-2 rounded-control bg-surface-sunken p-3 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm leading-snug text-ink-body">{t(`journey.next.${next.key}`)}</p>
          <Link href={next.href} className={`${primary} shrink-0`}>
            {t(`journey.actions.${next.key}`)}
          </Link>
        </div>
      ) : showcase && pagePath ? (
        <div className="flex flex-col gap-2 rounded-control bg-accent-soft p-3 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm leading-snug text-accent-ink">{t("journey.doneLead")}</p>
          {showcase.status === "PUBLISHED" ? (
            <ShareButton
              path={pagePath}
              title={t("myShowcase.shareTitle", { site: siteLabel(showcase.siteUrl) })}
              label={t("myShowcase.share")}
              copiedLabel={t("myShowcase.shareCopied")}
            />
          ) : (
            <Link href={pagePath} className={`${primary} shrink-0`}>
              {t("myShowcase.viewPage")}
            </Link>
          )}
        </div>
      ) : null}
    </section>
  );
}

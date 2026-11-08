import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { IconArrowRight, IconCheck, IconClose } from "@/components/icons";
import { GitHubHandleForm } from "@/components/landing/GitHubHandleForm";
import { ProfilePreviews } from "@/components/landing/ProfilePreviews";
import { ProfileSpecimen } from "@/components/landing/ProfileSpecimen";
import { AfricaMosaic } from "@/components/map/AfricaMosaic";
import { JsonLd } from "@/components/seo/JsonLd";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { placeCounts, type PlacedMap } from "@/lib/map";
import { getCityCounts } from "@/services/map";

const h2 = "text-[30px] leading-[1.1] font-semibold tracking-[-0.03em] lg:text-[44px]";
const lead = "max-w-xl text-[16px] leading-relaxed text-ink-soft lg:text-[17px]";
const section = "wrap flex flex-col gap-8 py-16 lg:gap-12 lg:py-28";
const INDICATORS = ["seniority", "volume", "regularity", "stars", "languages", "external"] as const;
const PATH_STEPS = ["find", "first", "accepted", "publish"] as const;
const PATH_LEVELS = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;
const READS = ["contributions", "seniority", "stars", "languages", "external"] as const;
const NOT_READS = ["private", "linkedin", "strangers", "score"] as const;

async function MapBand({ locale }: { locale: Locale }) {
  await connection();
  const [t, m] = await Promise.all([getTranslations({ locale, namespace: "landing.map" }), getTranslations({ locale, namespace: "map" })]);
  let placed: PlacedMap | null = null;
  try {
    placed = placeCounts(await getCityCounts());
  } catch (error) {
    console.error("The landing map could not be loaded", error);
  }
  return (
    <div className="wrap grid items-center gap-10 py-16 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)] lg:gap-16 lg:py-24">
      <div className="flex flex-col items-start gap-5">
        <h2 className={h2}>{t("title")}</h2>
        <p className={lead}>{t("lead")}</p>
        {placed ? <p className="text-[15px] font-medium tabular-nums">{m("count", { count: placed.total })}</p> : null}
        <Link href={localizedPath(locale, "/map")} className="btn btn-secondary">
          {t("explore")}
          <IconArrowRight aria-hidden="true" className="size-4" />
        </Link>
      </div>
      <div className="mx-auto w-full max-w-[520px]">
        <AfricaMosaic locale={locale} cities={placed?.cities ?? []} label={t("picture")} />
      </div>
    </div>
  );
}

export default async function LandingPage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const [t, meta, auth] = await Promise.all([
    getTranslations({ locale, namespace: "landing" }),
    getTranslations({ locale, namespace: "meta" }),
    getTranslations({ locale, namespace: "auth" }),
  ]);

  return (
    <>
      <JsonLd data={{ "@context": "https://schema.org", "@type": "WebSite", name: "SourceFolio", url: `${process.env.SITE_URL ?? "http://localhost:3000"}/${locale}`, description: meta("description"), inLanguage: locale }} />

      <section className="wrap grid items-center gap-12 py-12 lg:min-h-[calc(100dvh-4rem)] lg:grid-cols-[minmax(0,1.05fr)_minmax(0,1fr)] lg:gap-16 lg:py-16">
        <div className="animate-rise flex min-w-0 flex-col items-start gap-6">
          <h1 className="text-[44px] leading-[1.02] font-semibold tracking-[-0.04em] sm:text-[56px] lg:text-[68px]">{t("hero.title")}</h1>
          <p className="max-w-lg text-[17px] leading-relaxed text-ink-soft lg:text-[18px]">{t("hero.lead")}</p>
          <div className="w-full max-w-lg">
            <GitHubHandleForm
              locale={locale}
              labels={{
                field: t("form.field"),
                placeholder: t("form.placeholder"),
                submit: t("form.submit"),
                checking: t("form.checking"),
                invalid: t("form.invalid"),
                missing: t("form.missing"),
                signIn: t("form.signIn"),
                failed: t("form.failed"),
              }}
            />
          </div>
          <Link href={localizedPath(locale, "/recruiters")} className="link flex min-h-11 items-center gap-1.5 text-[14px]">
            {t("hero.recruit")}
            <IconArrowRight aria-hidden="true" className="size-3.5" />
          </Link>
        </div>
        <div className="animate-rise min-w-0 [animation-delay:80ms]">
          <Suspense fallback={<div className="h-80 animate-pulse rounded-panel bg-surface-sunken" />}>
            <ProfileSpecimen locale={locale} />
          </Suspense>
        </div>
      </section>

      <section className="border-t border-line-soft">
        <div className="wrap py-16 lg:py-28">
          <h2 className="max-w-4xl text-[36px] leading-[1.06] font-semibold tracking-[-0.035em] lg:text-[64px]">{t("problem.title")}</h2>
          <p className={`${lead} mt-6`}>{t("problem.body")}</p>
        </div>
      </section>

      <section className={section}>
        <div className="flex flex-col gap-4">
          <h2 className={h2}>{t("profile.title")}</h2>
          <p className={lead}>{t("profile.lead")}</p>
        </div>
        <ul className="grid gap-px overflow-hidden rounded-card border border-line bg-line sm:grid-cols-2 lg:grid-cols-3">
          {INDICATORS.map((key) => (
            <li key={key} className="flex flex-col gap-2 bg-canvas p-6">
              <span className="text-[15px] font-medium">{t(`indicators.${key}.title`)}</span>
              <span className="text-[14px] leading-relaxed text-ink-muted">{t(`indicators.${key}.body`)}</span>
            </li>
          ))}
        </ul>
        <Suspense fallback={null}>
          <ProfilePreviews locale={locale} />
        </Suspense>
      </section>

      <section className="dark-band">
        <Suspense fallback={<div className="h-[560px]" />}>
          <MapBand locale={locale} />
        </Suspense>
      </section>

      <section className={section}>
        <div className="flex flex-col gap-4">
          <h2 className={h2}>{t("path.title")}</h2>
          <p className={lead}>{t("path.lead")}</p>
        </div>
        <ol className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          {PATH_STEPS.map((step, index) => (
            <li key={step} className="flex flex-col gap-3">
              <span aria-hidden="true" className={`size-6 rounded-[5px] ${PATH_LEVELS[index]}`} />
              <span className="text-[16px] font-medium">{t(`path.${step}.title`)}</span>
              <span className="text-[14px] leading-relaxed text-ink-muted">{t(`path.${step}.body`)}</span>
            </li>
          ))}
        </ol>
      </section>

      <section id="method" className="scroll-mt-20 border-t border-line-soft">
        <div className={section}>
          <h2 className={h2}>{t("method.title")}</h2>
          <div className="grid gap-10 sm:grid-cols-2">
            <div className="flex flex-col gap-4">
              <h3 className="text-[16px] font-medium">{t("method.readsTitle")}</h3>
              <ul className="flex flex-col gap-3 text-[15px] text-ink-soft">
                {READS.map((key) => (
                  <li key={key} className="flex gap-3">
                    <IconCheck aria-hidden="true" className="mt-1 size-4 shrink-0 text-accent" />
                    {t(`method.reads.${key}`)}
                  </li>
                ))}
              </ul>
            </div>
            <div className="flex flex-col gap-4">
              <h3 className="text-[16px] font-medium">{t("method.notReadsTitle")}</h3>
              <ul className="flex flex-col gap-3 text-[15px] text-ink-soft">
                {NOT_READS.map((key) => (
                  <li key={key} className="flex gap-3">
                    <IconClose aria-hidden="true" className="mt-1 size-4 shrink-0 text-ink-muted" />
                    {t(`method.notReads.${key}`)}
                  </li>
                ))}
              </ul>
            </div>
          </div>
          <p className="max-w-2xl text-[14px] leading-relaxed text-ink-muted">{t("method.honest")}</p>
        </div>
      </section>

      <section className="wrap">
        <div className="flex flex-col items-start gap-5 rounded-panel bg-surface-sunken p-8 lg:p-14">
          <h2 className={`${h2} max-w-2xl`}>{t("close.title")}</h2>
          <p className={lead}>{t("close.body")}</p>
          <GitHubSignIn label={auth("signInWithGithub")} permission={auth("permission")} returnTo={localizedPath(locale, "/me")} />
        </div>
      </section>
    </>
  );
}

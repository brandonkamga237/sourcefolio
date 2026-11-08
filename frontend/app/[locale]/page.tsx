import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { GitHubHandleForm } from "@/components/landing/GitHubHandleForm";
import { LandingSwitch } from "@/components/landing/LandingSwitch";
import { ProfilePreviews } from "@/components/landing/ProfilePreviews";
import { AfricaMosaic } from "@/components/map/AfricaMosaic";
import { JsonLd } from "@/components/seo/JsonLd";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { placeCounts, type PlacedMap } from "@/lib/map";
import { getCityCounts } from "@/services/map";

const title2 = "font-display text-[24px] leading-tight font-semibold lg:text-[28px]";
const body = "max-w-2xl text-[15px] leading-relaxed text-ink-soft";
const PATH_STEPS = ["find", "first", "accepted", "publish"] as const;
const PATH_LEVELS = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;
const READS = ["contributions", "seniority", "stars", "languages", "external"] as const;
const NOT_READS = ["private", "linkedin", "strangers", "score"] as const;

async function HeroMap({ locale }: { locale: Locale }) {
  await connection();
  const t = await getTranslations({ locale, namespace: "landing.map" });
  let placed: PlacedMap = { cities: [], unplaced: 0, total: 0 };
  try {
    placed = placeCounts(await getCityCounts());
  } catch (error) {
    console.error("The landing map could not be loaded", error);
  }
  return (
    <div className="flex flex-col gap-2">
      <AfricaMosaic locale={locale} cities={placed.cities} label={t("picture")} />
      <Link href={localizedPath(locale, "/map")} className="flex min-h-11 w-fit items-center text-[14px] font-semibold text-accent hover:underline">
        {t("explore")}
      </Link>
    </div>
  );
}

export default async function LandingPage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const [t, meta] = await Promise.all([getTranslations({ locale, namespace: "landing" }), getTranslations({ locale, namespace: "meta" })]);

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-14 px-4 py-6 lg:gap-20 lg:px-8 lg:py-10">
      <JsonLd data={{ "@context": "https://schema.org", "@type": "WebSite", name: "SourceFolio", url: `${process.env.SITE_URL ?? "http://localhost:3000"}/${locale}`, description: meta("description"), inLanguage: locale }} />
      <section className="grid items-center gap-8 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
        <div className="flex min-w-0 flex-col gap-5">
          <LandingSwitch locale={locale} current="tech" />
          <h1 className="font-display text-[36px] leading-[1.08] font-semibold tracking-[-0.02em] lg:text-[52px]">{t("hero.title")}</h1>
          <p className="max-w-xl text-[16px] leading-relaxed text-ink-soft">{t("hero.lead")}</p>
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
        <Suspense fallback={<div className="aspect-square animate-pulse rounded-card bg-surface-sunken" />}>
          <HeroMap locale={locale} />
        </Suspense>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className={title2}>{t("problem.title")}</h2>
        <p className={body}>{t("problem.body")}</p>
      </section>

      <section className="flex flex-col gap-5">
        <div className="flex flex-col gap-3">
          <h2 className={title2}>{t("profile.title")}</h2>
          <p className={body}>{t("profile.lead")}</p>
        </div>
        <Suspense fallback={<div className="h-36 animate-pulse rounded-card bg-surface-sunken" />}>
          <ProfilePreviews locale={locale} />
        </Suspense>
      </section>

      <section className="flex flex-col gap-5">
        <div className="flex flex-col gap-3">
          <h2 className={title2}>{t("path.title")}</h2>
          <p className={body}>{t("path.lead")}</p>
        </div>
        <ol className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {PATH_STEPS.map((step, index) => (
            <li key={step} className="flex items-start gap-3">
              <span aria-hidden="true" className={`mt-0.5 size-5 shrink-0 rounded-[4px] ${PATH_LEVELS[index]}`} />
              <span className="flex flex-col gap-1">
                <span className="text-[15px] font-semibold">{t(`path.${step}.title`)}</span>
                <span className="text-[14px] leading-snug text-ink-soft">{t(`path.${step}.body`)}</span>
              </span>
            </li>
          ))}
        </ol>
      </section>

      <section className="flex flex-col items-start gap-3 rounded-panel border border-line bg-surface-raised p-5 lg:p-8">
        <h2 className={title2}>{t("recruit.title")}</h2>
        <p className={body}>{t("recruit.body")}</p>
        <Link href={localizedPath(locale, "/recruiters")} className="flex min-h-11 items-center text-[15px] font-semibold text-accent hover:underline">
          {t("recruit.link")}
        </Link>
      </section>

      <section className="flex flex-col gap-5">
        <h2 className={title2}>{t("method.title")}</h2>
        <div className="grid gap-6 sm:grid-cols-2">
          <div className="flex flex-col gap-2">
            <h3 className="text-[15px] font-semibold">{t("method.readsTitle")}</h3>
            <ul className="flex list-disc flex-col gap-1.5 pl-5 text-[14px] text-ink-soft">
              {READS.map((key) => <li key={key}>{t(`method.reads.${key}`)}</li>)}
            </ul>
          </div>
          <div className="flex flex-col gap-2">
            <h3 className="text-[15px] font-semibold">{t("method.notReadsTitle")}</h3>
            <ul className="flex list-disc flex-col gap-1.5 pl-5 text-[14px] text-ink-soft">
              {NOT_READS.map((key) => <li key={key}>{t(`method.notReads.${key}`)}</li>)}
            </ul>
          </div>
        </div>
        <p className="max-w-2xl text-[13px] leading-relaxed text-ink-muted">{t("method.honest")}</p>
      </section>
    </div>
  );
}

import Link from "next/link";
import type { CSSProperties, ComponentType, SVGProps } from "react";
import { connection } from "next/server";
import { getFormatter, getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { IconArrowRight, IconCalendar, IconCheck, IconClose, IconCode, IconCommit, IconMerge, IconPulse, IconStar } from "@/components/icons";
import { GitHubHandleForm } from "@/components/landing/GitHubHandleForm";
import { ProfilePreviews } from "@/components/landing/ProfilePreviews";
import { ProfileSpecimen } from "@/components/landing/ProfileSpecimen";
import { AfricaFieldLazy as AfricaField } from "@/components/map/AfricaFieldLazy";
import { JsonLd } from "@/components/seo/JsonLd";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { MAP_CITIES } from "@/lib/africa-map";
import { fieldCities, placeCounts, type PlacedMap } from "@/lib/map";
import { getCityCounts } from "@/services/map";

const h2 = "text-[40px] leading-[1.04] font-semibold tracking-[-0.035em] lg:text-[72px]";
const lead = "max-w-2xl text-[19px] leading-relaxed text-ink-soft lg:text-[22px]";
const section = "wrap flex flex-col gap-12 py-24 lg:gap-20 lg:py-40";
const INDICATORS: ["seniority" | "volume" | "regularity" | "stars" | "languages" | "external", ComponentType<SVGProps<SVGSVGElement>>][] = [
  ["seniority", IconCalendar],
  ["volume", IconCommit],
  ["regularity", IconPulse],
  ["stars", IconStar],
  ["languages", IconCode],
  ["external", IconMerge],
];
const PATH_STEPS = ["find", "first", "accepted", "publish"] as const;
const PATH_LEVELS = ["bg-level-1", "bg-level-2", "bg-level-3", "bg-level-4"] as const;
const READS = ["contributions", "seniority", "stars", "languages", "external"] as const;
const NOT_READS = ["private", "linkedin", "strangers", "score"] as const;

async function loadPlaced(): Promise<PlacedMap | null> {
  await connection();
  try {
    return placeCounts(await getCityCounts());
  } catch (error) {
    console.error("The landing map could not be loaded", error);
    return null;
  }
}

async function HeroMap({ locale }: { locale: Locale }) {
  const [t, placed] = await Promise.all([getTranslations({ locale, namespace: "landing.map" }), loadPlaced()]);
  return <AfricaField locale={locale} cities={fieldCities(placed?.cities ?? [])} label={t("picture")} />;
}

async function HeroStats({ locale }: { locale: Locale }) {
  const [t, format, placed] = await Promise.all([getTranslations({ locale, namespace: "landing.stats" }), getFormatter({ locale }), loadPlaced()]);
  const total = placed?.total ?? null;
  const stats = [
    ...(total === null ? [] : [{ value: total, label: t("profiles", { count: total }) }]),
    { value: MAP_CITIES.length, label: t("cities") },
    { value: new Set(MAP_CITIES.map((city) => city.country)).size, label: t("countries") },
    { value: 6, label: t("indicators") },
  ];
  return (
    <dl className="grid grid-cols-2 gap-x-6 gap-y-10 lg:grid-cols-4">
      {stats.map((stat) => (
        <div key={stat.label} className="flex flex-col-reverse gap-2">
          <dt className="text-[16px] text-ink-muted lg:text-[18px]">{stat.label}</dt>
          <dd data-count={stat.value} className="text-[56px] leading-none font-semibold tracking-[-0.04em] tabular-nums lg:text-[88px]">{format.number(stat.value)}</dd>
        </div>
      ))}
    </dl>
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
  const words = t("hero.title").split(" ");

  return (
    <div className="dark-band overflow-hidden">
      <JsonLd data={{ "@context": "https://schema.org", "@type": "WebSite", name: "SourceFolio", url: `${process.env.SITE_URL ?? "http://localhost:3000"}/${locale}`, description: meta("description"), inLanguage: locale }} />

      <section className="hero-lights relative">
        <div className="wrap flex flex-col gap-6 pt-6 pb-14 lg:grid lg:min-h-[calc(100dvh-4.5rem)] lg:items-center lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)] lg:gap-12 lg:py-12">
          <div className="contents lg:flex lg:min-w-0 lg:flex-col lg:items-start lg:gap-7">
            <h1 className="order-1 text-[52px] lg:order-none leading-[0.98] font-semibold tracking-[-0.045em] sm:text-[80px] xl:text-[104px]">
              {words.map((word, index) => (
                <span key={index} style={{ animationDelay: `${index * 90}ms` } as CSSProperties} className={`animate-rise inline-block ${index === words.length - 1 ? "text-gradient" : ""}`}>
                  {word}
                  {index < words.length - 1 ? " " : ""}
                </span>
              ))}
            </h1>
            <p className="animate-rise order-4 max-w-xl text-[19px] leading-relaxed text-ink-soft [animation-delay:320ms] lg:order-none lg:text-[22px]">{t("hero.lead")}</p>
            <div className="animate-rise order-3 w-full max-w-xl [animation-delay:420ms] lg:order-none">
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
            <div className="animate-rise order-5 flex flex-wrap gap-x-6 [animation-delay:520ms] lg:order-none">
              <Link href={localizedPath(locale, "/map")} className="link flex min-h-11 items-center gap-2 text-[16px]">
                {t("map.explore")}
                <IconArrowRight aria-hidden="true" className="size-4" />
              </Link>
              <Link href={localizedPath(locale, "/recruiters")} className="link flex min-h-11 items-center gap-2 text-[16px]">
                {t("hero.recruit")}
                <IconArrowRight aria-hidden="true" className="size-4" />
              </Link>
            </div>
          </div>
          <div className="order-2 mx-auto w-[76%] min-w-0 max-w-sm lg:order-none lg:mx-0 lg:-mr-12 lg:w-auto lg:max-w-none">
            <Suspense fallback={<div className="aspect-[72/74] w-full" />}>
              <HeroMap locale={locale} />
            </Suspense>
          </div>
        </div>
        <div className="wrap border-t border-line-soft py-14 lg:py-20">
          <Suspense fallback={<div className="h-28" />}>
            <HeroStats locale={locale} />
          </Suspense>
        </div>
      </section>

      <section className="border-t border-line-soft">
        <div className="wrap py-24 lg:py-40">
          <h2 data-reveal="" className="max-w-5xl text-[44px] leading-[1.02] font-semibold tracking-[-0.04em] lg:text-[96px]">{t("problem.title")}</h2>
          <p data-reveal="" className={`${lead} mt-8`}>{t("problem.body")}</p>
        </div>
      </section>

      <section className={section}>
        <div data-reveal="" className="flex flex-col gap-6">
          <h2 className={h2}>{t("profile.title")}</h2>
          <p className={lead}>{t("profile.lead")}</p>
        </div>
        <div data-reveal="">
          <Suspense fallback={<div className="h-[520px] animate-pulse rounded-panel bg-surface-sunken" />}>
            <ProfileSpecimen locale={locale} />
          </Suspense>
        </div>
        <ul data-stagger="" className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {INDICATORS.map(([key, Icon]) => (
            <li key={key} className="glow-card flex flex-col gap-4 p-7 lg:p-8">
              <Icon aria-hidden="true" className="size-8 text-level-4" />
              <span className="text-[22px] font-semibold tracking-[-0.02em]">{t(`indicators.${key}.title`)}</span>
              <span className="text-[17px] leading-relaxed text-ink-muted">{t(`indicators.${key}.body`)}</span>
            </li>
          ))}
        </ul>
        <Suspense fallback={null}>
          <ProfilePreviews locale={locale} />
        </Suspense>
      </section>

      <section className="hero-lights border-t border-line-soft">
        <div className={section}>
          <div data-reveal="" className="flex flex-col gap-6">
            <h2 className={h2}>{t("path.title")}</h2>
            <p className={lead}>{t("path.lead")}</p>
          </div>
          <ol className="relative flex flex-col gap-16 pl-14 lg:gap-24 lg:pl-20">
            <svg aria-hidden="true" viewBox="0 0 4 100" preserveAspectRatio="none" className="absolute top-2 bottom-2 left-[14px] h-[calc(100%-1rem)] w-1 lg:left-[18px]">
              <path d="M2 0V100" stroke="var(--color-line)" strokeWidth={4} fill="none" />
              <path data-draw="" d="M2 0V100" stroke="var(--color-level-4)" strokeWidth={4} fill="none" />
            </svg>
            {PATH_STEPS.map((step, index) => (
              <li key={step} data-reveal="" className="relative flex max-w-2xl flex-col gap-3">
                <span aria-hidden="true" data-light="" className={`absolute top-1 -left-14 size-8 rounded-[7px] lg:-left-20 lg:size-10 ${PATH_LEVELS[index]}`} />
                <span className="text-[28px] font-semibold tracking-[-0.025em] lg:text-[40px]">{t(`path.${step}.title`)}</span>
                <span className="text-[18px] leading-relaxed text-ink-muted lg:text-[20px]">{t(`path.${step}.body`)}</span>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section id="method" className="scroll-mt-24 border-t border-line-soft">
        <div className={section}>
          <h2 data-reveal="" className={h2}>{t("method.title")}</h2>
          <div data-stagger="" className="grid gap-4 lg:grid-cols-2">
            <div className="glow-card flex flex-col gap-6 p-8 lg:p-10">
              <h3 className="text-[26px] font-semibold tracking-[-0.02em]">{t("method.readsTitle")}</h3>
              <ul className="flex flex-col gap-4 text-[18px] text-ink-soft">
                {READS.map((key) => (
                  <li key={key} className="flex gap-3">
                    <IconCheck aria-hidden="true" className="mt-1 size-5 shrink-0 text-level-4" />
                    {t(`method.reads.${key}`)}
                  </li>
                ))}
              </ul>
            </div>
            <div className="glow-card flex flex-col gap-6 p-8 lg:p-10">
              <h3 className="text-[26px] font-semibold tracking-[-0.02em]">{t("method.notReadsTitle")}</h3>
              <ul className="flex flex-col gap-4 text-[18px] text-ink-soft">
                {NOT_READS.map((key) => (
                  <li key={key} className="flex gap-3">
                    <IconClose aria-hidden="true" className="mt-1 size-5 shrink-0 text-ink-muted" />
                    {t(`method.notReads.${key}`)}
                  </li>
                ))}
              </ul>
            </div>
          </div>
          <p data-reveal="" className="max-w-3xl text-[17px] leading-relaxed text-ink-muted">{t("method.honest")}</p>
        </div>
      </section>

      <section className="wrap pb-24 lg:pb-40">
        <div data-reveal="" className="hero-lights flex flex-col items-start gap-8 rounded-[28px] border border-line p-8 lg:p-20">
          <h2 className={`${h2} text-gradient max-w-4xl`}>{t("close.title")}</h2>
          <p className={lead}>{t("close.body")}</p>
          <GitHubSignIn label={auth("signInWithGithub")} permission={auth("permission")} returnTo={localizedPath(locale, "/me")} />
        </div>
      </section>
    </div>
  );
}

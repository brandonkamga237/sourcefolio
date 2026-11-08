import type { Metadata } from "next";
import type { ComponentType, SVGProps } from "react";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { IconArrowRight, IconCode, IconMerge, IconPulse } from "@/components/icons";
import { TalentField } from "@/components/landing/TalentField";
import { WaitlistForm } from "@/components/landing/WaitlistForm";
import { WaitlistLeaveForm } from "@/components/landing/WaitlistLeaveForm";
import { localizedPath } from "@/i18n/paths";
import { toLocale, type Locale } from "@/i18n/routing";
import { getRankings } from "@/services/rankings";

export async function generateMetadata({ params }: PageProps<"/[locale]/recruiters">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "recruiters" });
  return {
    title: t("meta.title"),
    description: t("hero.lead"),
    alternates: { canonical: `/${locale}/recruiters`, languages: { fr: "/fr/recruiters", en: "/en/recruiters", "x-default": "/en/recruiters" } },
  };
}

async function Talents({ locale }: { locale: Locale }) {
  await connection();
  const t = await getTranslations({ locale, namespace: "recruiters.field" });
  let rows;
  try {
    rows = (await getRankings({ sort: "regularity", country: null, language: null, cursor: null })).items;
  } catch (error) {
    console.error("The talent field could not be loaded", error);
    return <p role="alert" className="rounded-card border border-line bg-surface p-4 text-[14px]">{t("unavailable")}</p>;
  }
  if (rows.length === 0) return <p className="text-[14px] text-ink-soft">{t("empty")}</p>;

  return (
    <TalentField
      locale={locale}
      talents={rows.map(({ login, city, languages, activeMonths }) => ({ login, city, languages, activeMonths }))}
      labels={{
        language: t("language"),
        allLanguages: t("allLanguages"),
        city: t("city"),
        allCities: t("allCities"),
        rhythm: t("rhythm"),
        allRhythms: t("allRhythms"),
        steady: t("steady"),
        regular: t("regular"),
        occasional: t("occasional"),
        shown: t.raw("shown") as string,
        noMatch: t("noMatch"),
        months: t.raw("months") as string,
      }}
    />
  );
}

const READ: ["regularity" | "accepted" | "languages", ComponentType<SVGProps<SVGSVGElement>>][] = [
  ["regularity", IconPulse],
  ["accepted", IconMerge],
  ["languages", IconCode],
];
const h2 = "text-[40px] leading-[1.04] font-semibold tracking-[-0.035em] lg:text-[72px]";

export default async function RecruitersPage({ params }: PageProps<"/[locale]/recruiters">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "recruiters" });

  return (
    <div className="dark-band overflow-hidden">
      <section className="hero-lights">
        <div className="wrap grid items-center gap-12 py-14 lg:min-h-[calc(100dvh-4.5rem)] lg:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)] lg:gap-16 lg:py-16">
          <div className="animate-rise flex min-w-0 flex-col items-start gap-7">
            <h1 className="text-[64px] leading-[0.98] font-semibold tracking-[-0.045em] sm:text-[88px] xl:text-[120px]">{t("hero.title")}</h1>
            <p className="max-w-xl text-[19px] leading-relaxed text-ink-soft lg:text-[22px]">{t("hero.lead")}</p>
            <Link href={localizedPath(locale, "/")} className="link flex min-h-11 items-center gap-2 text-[16px]">
              {t("hero.tech")}
              <IconArrowRight aria-hidden="true" className="size-4" />
            </Link>
          </div>
          <div className="glow-card animate-rise flex flex-col gap-5 p-7 [animation-delay:120ms] lg:p-9">
            <h2 className="text-[28px] font-semibold tracking-[-0.025em]">{t("waitlist.title")}</h2>
            <p className="text-[17px] leading-relaxed text-ink-soft">{t("waitlist.lead")}</p>
            <WaitlistForm
              labels={{
                email: t("waitlist.email"),
                placeholder: t("waitlist.placeholder"),
                consent: t("waitlist.consent"),
                submit: t("waitlist.submit"),
                sending: t("waitlist.sending"),
                done: t("waitlist.done"),
                failed: t("waitlist.failed"),
              }}
            />
            <WaitlistLeaveForm
              labels={{
                summary: t("waitlist.leave.summary"),
                email: t("waitlist.email"),
                submit: t("waitlist.leave.submit"),
                sending: t("waitlist.sending"),
                done: t("waitlist.leave.done"),
                failed: t("waitlist.leave.failed"),
              }}
            />
          </div>
        </div>
      </section>

      <section className="border-t border-line-soft">
        <div className="wrap flex flex-col gap-12 py-24 lg:gap-20 lg:py-40">
          <h2 data-reveal="" className={`${h2} max-w-5xl`}>{t("read.title")}</h2>
          <ul data-stagger="" className="grid gap-4 lg:grid-cols-3">
            {READ.map(([key, Icon]) => (
              <li key={key} className="glow-card flex flex-col gap-4 p-8">
                <Icon aria-hidden="true" className="size-8 text-level-4" />
                <span className="text-[24px] font-semibold tracking-[-0.02em]">{t(`read.${key}.title`)}</span>
                <span className="text-[18px] leading-relaxed text-ink-muted">{t(`read.${key}.body`)}</span>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="hero-lights border-t border-line-soft">
        <div className="wrap flex flex-col gap-10 py-24 lg:py-40">
          <div data-reveal="" className="flex flex-col gap-6">
            <h2 className={h2}>{t("field.title")}</h2>
            <p className="max-w-2xl text-[19px] leading-relaxed text-ink-soft lg:text-[22px]">{t("field.lead")}</p>
          </div>
          <Suspense fallback={<div className="h-40 animate-pulse rounded-card bg-surface-sunken" />}>
            <Talents locale={locale} />
          </Suspense>
          <p className="max-w-3xl text-[16px] leading-relaxed text-ink-muted">{t("field.honest")}</p>
          <Link href={localizedPath(locale, "/rankings")} className="btn btn-secondary w-fit">
            {t("field.rankings")}
            <IconArrowRight aria-hidden="true" className="size-4" />
          </Link>
        </div>
      </section>
    </div>
  );
}

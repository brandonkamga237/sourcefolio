import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { LandingSwitch } from "@/components/landing/LandingSwitch";
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

export default async function RecruitersPage({ params }: PageProps<"/[locale]/recruiters">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "recruiters" });

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-12 px-4 py-6 lg:gap-16 lg:px-8 lg:py-10">
      <section className="grid items-start gap-8 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)]">
        <div className="flex min-w-0 flex-col gap-5">
          <LandingSwitch locale={locale} current="recruit" />
          <h1 className="font-display text-[36px] leading-[1.08] font-semibold tracking-[-0.02em] lg:text-[52px]">{t("hero.title")}</h1>
          <p className="max-w-xl text-[16px] leading-relaxed text-ink-soft">{t("hero.lead")}</p>
        </div>
        <div className="flex flex-col gap-3 rounded-panel border border-line bg-surface-raised p-5">
          <h2 className="font-display text-[20px] font-semibold">{t("waitlist.title")}</h2>
          <p className="text-[14px] leading-relaxed text-ink-soft">{t("waitlist.lead")}</p>
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
      </section>

      <section className="flex flex-col gap-5">
        <div className="flex flex-col gap-2">
          <h2 className="font-display text-[24px] leading-tight font-semibold lg:text-[28px]">{t("field.title")}</h2>
          <p className="max-w-2xl text-[15px] leading-relaxed text-ink-soft">{t("field.lead")}</p>
        </div>
        <Suspense fallback={<div className="h-40 animate-pulse rounded-card bg-surface-sunken" />}>
          <Talents locale={locale} />
        </Suspense>
        <p className="max-w-2xl text-[13px] leading-relaxed text-ink-muted">{t("field.honest")}</p>
        <Link href={localizedPath(locale, "/rankings")} className="flex min-h-11 w-fit items-center text-[14px] font-semibold text-accent hover:underline">
          {t("field.rankings")}
        </Link>
      </section>
    </div>
  );
}

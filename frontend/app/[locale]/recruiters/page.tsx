import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import { IconArrowRight } from "@/components/icons";
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

const READ = ["regularity", "accepted", "languages"] as const;

export default async function RecruitersPage({ params }: PageProps<"/[locale]/recruiters">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "recruiters" });

  return (
    <>
      <section className="wrap grid items-center gap-10 py-12 lg:min-h-[calc(100dvh-4rem)] lg:grid-cols-[minmax(0,1.15fr)_minmax(0,1fr)] lg:gap-16 lg:py-16">
        <div className="animate-rise flex min-w-0 flex-col items-start gap-6">
          <h1 className="text-[44px] leading-[1.02] font-semibold tracking-[-0.04em] sm:text-[56px] lg:text-[72px]">{t("hero.title")}</h1>
          <p className="max-w-lg text-[17px] leading-relaxed text-ink-soft lg:text-[18px]">{t("hero.lead")}</p>
          <Link href={localizedPath(locale, "/")} className="link flex min-h-11 items-center gap-1.5 text-[14px]">
            {t("hero.tech")}
            <IconArrowRight aria-hidden="true" className="size-3.5" />
          </Link>
        </div>
        <div className="animate-rise flex flex-col gap-4 rounded-panel border border-line bg-surface p-6 shadow-[0_24px_60px_-32px_rgba(0,0,0,0.28)] [animation-delay:80ms]">
          <h2 className="text-[20px] font-semibold tracking-[-0.02em]">{t("waitlist.title")}</h2>
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

      <section className="border-t border-line-soft">
        <div className="wrap flex flex-col gap-8 py-16 lg:gap-12 lg:py-28">
          <h2 className="max-w-3xl text-[30px] leading-[1.1] font-semibold tracking-[-0.03em] lg:text-[44px]">{t("read.title")}</h2>
          <ul className="grid gap-px overflow-hidden rounded-card border border-line bg-line lg:grid-cols-3">
            {READ.map((key) => (
              <li key={key} className="flex flex-col gap-2 bg-canvas p-6">
                <span className="text-[15px] font-medium">{t(`read.${key}.title`)}</span>
                <span className="text-[14px] leading-relaxed text-ink-muted">{t(`read.${key}.body`)}</span>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="border-t border-line-soft">
        <div className="wrap flex flex-col gap-8 py-16 lg:py-24">
          <div className="flex flex-col gap-4">
            <h2 className="text-[30px] leading-[1.1] font-semibold tracking-[-0.03em] lg:text-[44px]">{t("field.title")}</h2>
            <p className="max-w-2xl text-[16px] leading-relaxed text-ink-soft">{t("field.lead")}</p>
          </div>
          <Suspense fallback={<div className="h-40 animate-pulse rounded-card bg-surface-sunken" />}>
            <Talents locale={locale} />
          </Suspense>
          <p className="max-w-2xl text-[13px] leading-relaxed text-ink-muted">{t("field.honest")}</p>
          <Link href={localizedPath(locale, "/rankings")} className="link flex min-h-11 w-fit items-center text-[14px]">
            {t("field.rankings")}
          </Link>
        </div>
      </section>
    </>
  );
}

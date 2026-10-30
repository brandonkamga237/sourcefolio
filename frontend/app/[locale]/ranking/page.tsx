import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/ranking">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "ranking" });
  return {
    title: t("title"),
    alternates: { canonical: `/${locale}/ranking`, languages: { fr: "/fr/ranking", en: "/en/ranking", "x-default": "/en/ranking" } },
  };
}

export default async function RankingPage({ params }: PageProps<"/[locale]/ranking">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "ranking" });
  return (
    <div className="flex flex-col gap-4 px-4 py-5 lg:px-8 lg:py-7">
      <div className="flex flex-col gap-1">
        <h1 className="font-display text-[24px] leading-none font-bold tracking-[-0.035em] sm:text-[30px] lg:text-[40px]">{t("title")}</h1>
        <p className="text-sm text-ink-muted">{t("lead")}</p>
      </div>
      <p className="max-w-xl rounded-card border border-line bg-surface p-4 text-sm leading-normal text-ink-body">{t("empty")}</p>
    </div>
  );
}

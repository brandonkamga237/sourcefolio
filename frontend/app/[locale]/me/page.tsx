import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { LanguageSwitch } from "@/components/layout/LanguageSwitch";
import { ThemeChoice } from "@/components/theme/ThemeChoice";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "pages.me" });
  return {
    title: t("title"),
    alternates: { canonical: `/${locale}/me`, languages: { fr: "/fr/me", en: "/en/me" } },
    robots: { index: false },
  };
}

// The visitor's version: once sign-in exists, a member sees their profile and notification
// settings here instead of the sign-in block.
export default async function MePage({ params }: PageProps<"/[locale]/me">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale });

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-8 px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">
        {t("pages.me.title")}
      </h1>

      <section aria-labelledby="me-account" className="flex flex-col gap-3">
        <h2 id="me-account" className="text-[15px] font-semibold">{t("pages.me.account")}</h2>
        <p className="text-sm leading-normal text-ink-body">{t("pages.me.accountHint")}</p>
        <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} />
      </section>

      <section aria-labelledby="me-language" className="flex flex-col gap-3">
        <h2 id="me-language" className="text-[15px] font-semibold">{t("language.label")}</h2>
        <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
      </section>

      <section aria-labelledby="me-theme" className="flex flex-col gap-3">
        <h2 id="me-theme" className="text-[15px] font-semibold">{t("theme.label")}</h2>
        <ThemeChoice
          label={t("theme.label")}
          names={{ system: t("theme.system"), light: t("theme.light"), dark: t("theme.dark") }}
        />
      </section>
    </div>
  );
}

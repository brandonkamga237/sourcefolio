import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { toLocale } from "@/i18n/routing";

export async function generateMetadata({ params }: PageProps<"/[locale]/me/portfolio">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "pages.portfolio" });
  return {
    title: t("title"),
    alternates: { canonical: `/${locale}/me/portfolio`, languages: { fr: "/fr/me/portfolio", en: "/en/me/portfolio" } },
    robots: { index: false },
  };
}

// The visitor's version of the author dashboard: one action, signing in with GitHub, which
// starts the detection of the portfolio (spec: "Connexion et publication en 1 clic").
export default async function MyPortfolioPage({ params }: PageProps<"/[locale]/me/portfolio">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale });
  const steps = [t("pages.portfolio.step1"), t("pages.portfolio.step2"), t("pages.portfolio.step3")];

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-8 px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <div className="flex flex-col gap-3">
        <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">
          {t("pages.portfolio.visitorTitle")}
        </h1>
        <p className="text-[15px] leading-normal text-ink-body">{t("pages.portfolio.visitorLead")}</p>
      </div>

      <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} />

      <section aria-labelledby="portfolio-steps" className="flex flex-col gap-3">
        <h2 id="portfolio-steps" className="text-[15px] font-semibold">{t("pages.portfolio.stepsLabel")}</h2>
        <ol className="flex flex-col gap-2">
          {steps.map((step, index) => (
            <li key={index} className="flex items-start gap-3 rounded-card border border-line bg-surface p-3.5">
              <span
                aria-hidden="true"
                className="flex size-7 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-[13px] font-medium text-accent-ink"
              >
                {index + 1}
              </span>
              <span className="pt-1 text-sm leading-normal text-ink-body">{step}</span>
            </li>
          ))}
        </ol>
      </section>
    </div>
  );
}

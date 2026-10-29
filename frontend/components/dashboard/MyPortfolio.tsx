import { getTranslations } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import type { Locale } from "@/i18n/routing";
import { getMe } from "@/services/me";

interface MyPortfolioProps {
  locale: Locale;
}

const titleClass = "font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]";

// A visitor gets one action, signing in with GitHub, which starts the detection of their
// portfolio (spec: "Connexion et publication en 1 clic"). A member gets their dashboard; until
// detection exists, it can only say that nothing is published.
export async function MyPortfolio({ locale }: MyPortfolioProps) {
  const [t, member] = await Promise.all([getTranslations({ locale }), getMe()]);

  if (member) {
    return (
      <div className="flex flex-col gap-3">
        <h1 className={titleClass}>{t("pages.portfolio.title")}</h1>
        <p className="text-[15px] leading-normal text-ink-body">{t("pages.portfolio.memberEmpty")}</p>
      </div>
    );
  }

  const steps = [t("pages.portfolio.step1"), t("pages.portfolio.step2"), t("pages.portfolio.step3")];
  return (
    <>
      <div className="flex flex-col gap-3">
        <h1 className={titleClass}>{t("pages.portfolio.visitorTitle")}</h1>
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
    </>
  );
}

export function MyPortfolioSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-4">
      <div className="h-10 w-2/3 animate-pulse rounded bg-surface-sunken" />
      <div className="h-4 w-full animate-pulse rounded bg-surface-sunken" />
      <div className="h-[46px] animate-pulse rounded-control bg-surface-sunken" />
    </div>
  );
}

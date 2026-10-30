import { getTranslations } from "next-intl/server";
import Link from "next/link";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { getMe } from "@/services/me";
import { GitHubSignIn } from "./GitHubSignIn";
import { SignOutButton } from "./SignOutButton";

interface AccountSectionProps {
  locale: Locale;
}

// Reads the session, so it renders per request inside a Suspense boundary while the rest of
// the page stays prerendered.
export async function AccountSection({ locale }: AccountSectionProps) {
  const [t, member] = await Promise.all([getTranslations({ locale }), getMe()]);

  if (!member) {
    return (
      <>
        <p className="text-sm leading-normal text-ink-body">{t("pages.me.accountHint")}</p>
        <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} />
      </>
    );
  }

  return (
    <>
      <p className="text-sm leading-normal text-ink-body">{t("account.signedInAs", { login: `@${member.login}` })}</p>
      <Link href={localizedPath(locale, "/me/profile")} className="flex h-11 w-fit items-center text-sm font-semibold text-ink underline underline-offset-2">
        {t("profile.summaryTitle")}
      </Link>
      <SignOutButton label={t("account.signOut")} failedMessage={t("account.signOutFailed")} />
    </>
  );
}

export function AccountSectionSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-3">
      <div className="h-4 w-3/4 animate-pulse rounded bg-surface-sunken" />
      <div className="h-[46px] animate-pulse rounded-control bg-surface-sunken" />
    </div>
  );
}

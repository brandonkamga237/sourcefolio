import { getTranslations } from "next-intl/server";
import { GitHubSignIn } from "@/components/auth/GitHubSignIn";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryOptions } from "@/lib/countries";
import { getMe } from "@/services/me";
import { getMyAnswers } from "@/services/profile";
import { SPECIALTIES, SPECIALTY_GROUPS, type Specialty, type SpecialtyGroup } from "@/services/profile-types";
import { ProfileQuestions } from "./ProfileQuestions";

interface ProfileScreenProps {
  locale: Locale;
}

// Reads the session, so it renders per request inside the page's Suspense boundary. Country
// names come from Intl in the page's language, sorted for that language.
export async function ProfileScreen({ locale }: ProfileScreenProps) {
  const [t, member] = await Promise.all([getTranslations({ locale }), getMe()]);
  if (!member) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-[15px] leading-normal text-ink-body">{t("profile.signInHint")}</p>
        <GitHubSignIn label={t("auth.signInWithGithub")} permission={t("auth.permission")} />
      </div>
    );
  }

  const answers = await getMyAnswers();
  if (!answers) {
    return <p role="alert" className="text-sm text-ink-body">{t("profile.unavailable")}</p>;
  }

  const specialties = Object.fromEntries(SPECIALTIES.map((slug) => [slug, t(`specialty.${slug}`)])) as Record<Specialty, string>;
  return (
    <ProfileQuestions
      initial={answers}
      countries={countryOptions(locale)}
      doneHref={localizedPath(locale, "/me")}
      labels={{
        progress: t.raw("profile.progress") as string,
        headlineTitle: t("profile.headlineTitle"),
        headlineHint: t("profile.headlineHint"),
        headlinePlaceholder: t("profile.headlinePlaceholder"),
        headlineCounter: t.raw("profile.headlineCounter") as string,
        headlineLabel: t("profile.headlineLabel"),
        countryTitle: t("profile.countryTitle"),
        countryPlaceholder: t("profile.countryPlaceholder"),
        specialtyTitle: t("profile.specialtyTitle"),
        specialtyHint: t("profile.specialtyHint"),
        specialties,
        groups: Object.fromEntries((Object.keys(SPECIALTY_GROUPS) as SpecialtyGroup[]).map((group) => [group, t(`specialtyGroup.${group}`)])) as Record<SpecialtyGroup, string>,
        searchPlaceholder: t("profile.searchPlaceholder"),
        noMatch: t("profile.noMatch"),
        selectionEmpty: t("profile.selectionEmpty"),
        removeChoice: t.raw("profile.removeChoice") as string,
        emailTitle: t("profile.emailTitle"),
        emailHint: t("profile.emailHint"),
        next: t("profile.next"),
        skip: t("profile.skip"),
        saving: t("profile.saving"),
        failed: t("profile.failed"),
        summaryTitle: t("profile.summaryTitle"),
        countryLabel: t("profile.countryLabel"),
        specialtyLabel: t("profile.specialtyLabel"),
        emailLabel: t("profile.emailLabel"),
        notGiven: t("profile.notGiven"),
        edit: t("profile.edit"),
        done: t("profile.done"),
      }}
    />
  );
}

export function ProfileScreenSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-4">
      <div className="h-8 w-3/4 animate-pulse rounded bg-surface-sunken" />
      <div className="h-14 animate-pulse rounded-control bg-surface-sunken" />
      <div className="h-12 animate-pulse rounded-control bg-surface-sunken" />
    </div>
  );
}

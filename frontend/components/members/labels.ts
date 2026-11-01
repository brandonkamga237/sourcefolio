import { getTranslations } from "next-intl/server";
import type { Locale } from "@/i18n/routing";
import { ROLES, SPECIALTIES } from "@/services/profile-types";
import type { MemberSheetLabels } from "./MemberSheet";

/** The member sheet's strings, translated on the server and handed to the client as props. */
export async function memberSheetLabels(locale: Locale): Promise<MemberSheetLabels> {
  const [t, ts] = await Promise.all([getTranslations({ locale, namespace: "member" }), getTranslations({ locale })]);
  return {
    open: t.raw("open") as string,
    close: t("close"),
    loading: t("loading"),
    failed: t("failed"),
    since: t.raw("since") as string,
    likesReceived: { one: t.raw("likesOne") as string, other: t.raw("likesOther") as string },
    stars: { one: ts.raw("stars.one") as string, other: ts.raw("stars.other") as string },
    specialtiesTitle: t("specialtiesTitle"),
    noSpecialty: t("noSpecialty"),
    noCountry: t("noCountry"),
    portfolioTitle: t("portfolioTitle"),
    openPage: t("openPage"),
    visitSite: t("visitSite"),
    viewCode: t("viewCode"),
    githubProfile: t("githubProfile"),
    specialtyNames: Object.fromEntries(SPECIALTIES.map((slug) => [slug, ts(`specialty.${slug}`)])),
    roleClaim: t.raw("roleClaim") as string,
    roleNames: Object.fromEntries(ROLES.map((slug) => [slug, ts(`role.${slug}`)])),
  };
}

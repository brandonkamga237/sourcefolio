import { getTranslations } from "next-intl/server";
import type { Locale } from "@/i18n/routing";
import { FEEDBACK_ANGLES, type FeedbackAngle } from "@/services/feedback";
import type { FeedbackLabels } from "./types";

/** The feedback section's strings, translated on the server and handed to the client as props. */
export async function feedbackLabels(locale: Locale): Promise<FeedbackLabels> {
  const t = await getTranslations({ locale, namespace: "feedback" });
  return {
    title: t("title"),
    lead: t("lead"),
    angleLabel: t("angleLabel"),
    angles: Object.fromEntries(FEEDBACK_ANGLES.map((angle) => [angle, t(`angles.${angle}`)])) as Record<FeedbackAngle, string>,
    bodyLabel: t("bodyLabel"),
    placeholder: t("placeholder"),
    counter: t.raw("counter") as string,
    missing: { one: t.raw("missingOne") as string, other: t.raw("missingOther") as string },
    submit: t("submit"),
    sending: t("sending"),
    signInHint: t("signInHint"),
    posted: t("posted"),
    failed: t("failed"),
    own: t("own"),
    empty: t("empty"),
    emptyOwn: t("emptyOwn"),
    loadFailed: t("loadFailed"),
    more: t("more"),
    you: t("you"),
    justNow: t("justNow"),
    remove: t("remove"),
    confirmRemove: t("confirmRemove"),
    cancel: t("cancel"),
    removeFailed: t("removeFailed"),
    writtenIn: { fr: t("writtenIn.fr"), en: t("writtenIn.en") },
  };
}

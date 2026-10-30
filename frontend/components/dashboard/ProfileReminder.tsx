import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

interface ProfileReminderProps {
  locale: Locale;
}

// Discreet on purpose (spec): a note under the portfolio, never a modal or a badge.
export async function ProfileReminder({ locale }: ProfileReminderProps) {
  const t = await getTranslations({ locale, namespace: "profile" });
  return (
    <aside className="flex flex-col gap-2 rounded-card border border-line bg-surface p-4">
      <p className="text-sm leading-normal text-ink-body">{t("reminder")}</p>
      <Link href={localizedPath(locale, "/me/profile")} className="flex h-11 w-fit items-center rounded-control border border-ink px-4 text-sm font-semibold text-ink">
        {t("reminderLink")}
      </Link>
    </aside>
  );
}

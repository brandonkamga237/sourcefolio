import { getTranslations } from "next-intl/server";
import { SignOutButton } from "@/components/auth/SignOutButton";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { DeleteAccount } from "./DeleteAccount";
import { MePreferences } from "./MePreferences";

interface AccountSettingsProps {
  locale: Locale;
  login: string;
}

/** Theme, language, sign-out and account deletion: visited rarely, so folded. */
export async function AccountSettings({ locale, login }: AccountSettingsProps) {
  const t = await getTranslations({ locale });
  return (
    <details className="group/account rounded-card border border-line bg-surface [&_summary::-webkit-details-marker]:hidden">
      <summary className="flex h-12 cursor-pointer list-none items-center justify-between gap-3 px-4 text-sm font-semibold select-none">
        {t("me.accountTitle")}
        <span aria-hidden="true" className="size-2 rotate-45 border-r-2 border-b-2 border-ink-muted transition-transform duration-200 group-open/account:-rotate-135" />
      </summary>
      <div className="flex flex-col gap-4 border-t border-line-soft p-4">
        <MePreferences locale={locale} />
        <SignOutButton label={t("account.signOut")} failedMessage={t("account.signOutFailed")} />
        <div className="flex flex-col gap-2 border-t border-line-soft pt-4">
          <p className="text-[13px] leading-normal text-ink-muted">{t("account.deleteLead")}</p>
          <DeleteAccount
            login={login}
            homeHref={localizedPath(locale, "/")}
            label={t("account.delete")}
            failed={t("account.deleteFailed")}
            dialog={{
              title: t("account.deleteDialog.title"),
              description: t("account.deleteDialog.description"),
              confirm: t("account.deleteDialog.confirm"),
              pending: t("account.deleteDialog.pending"),
              cancel: t("common.cancel"),
              typeToConfirm: t.raw("account.deleteDialog.typeToConfirm") as string,
            }}
          />
        </div>
      </div>
    </details>
  );
}

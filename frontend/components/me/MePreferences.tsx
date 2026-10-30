import { getTranslations } from "next-intl/server";
import { LanguageSwitch } from "@/components/layout/LanguageSwitch";
import { ThemeChoice } from "@/components/theme/ThemeChoice";
import type { Locale } from "@/i18n/routing";

interface MePreferencesProps {
  locale: Locale;
}

export async function MePreferences({ locale }: MePreferencesProps) {
  const t = await getTranslations({ locale });
  return (
    <section aria-labelledby="me-preferences" className="flex flex-col gap-3">
      <h2 id="me-preferences" className="text-[13px] font-semibold tracking-wide text-ink-muted uppercase">{t("me.preferencesTitle")}</h2>
      <div className="flex flex-col gap-3 rounded-card border border-line bg-surface p-4">
        <div className="flex flex-col gap-2">
          <span className="text-xs text-ink-muted">{t("language.label")}</span>
          <LanguageSwitch current={locale} label={t("language.label")} names={{ fr: t("language.fr"), en: t("language.en") }} />
        </div>
        <div className="flex flex-col gap-2">
          <span className="text-xs text-ink-muted">{t("theme.label")}</span>
          <ThemeChoice label={t("theme.label")} names={{ system: t("theme.system"), light: t("theme.light"), dark: t("theme.dark") }} />
        </div>
      </div>
    </section>
  );
}

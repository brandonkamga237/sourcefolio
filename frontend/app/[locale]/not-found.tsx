import Link from "next/link";
import { getLocale, getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";

export default async function NotFound() {
  const locale = toLocale(await getLocale());
  const t = await getTranslations({ locale, namespace: "notFound" });

  return (
    <div className="wrap flex flex-col items-start gap-5 py-20 lg:py-32">
      <h1 className="text-[40px] leading-[1.05] font-semibold tracking-[-0.035em] lg:text-[56px]">{t("title")}</h1>
      <p className="max-w-lg text-[17px] leading-relaxed text-ink-soft">{t("body")}</p>
      <Link
        href={localizedPath(locale, "/")}
        className="btn btn-primary"
      >
        {t("back")}
      </Link>
    </div>
  );
}

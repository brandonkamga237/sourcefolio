import Link from "next/link";
import { getLocale, getTranslations } from "next-intl/server";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";

export default async function NotFound() {
  const locale = toLocale(await getLocale());
  const t = await getTranslations({ locale, namespace: "notFound" });

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-4 px-4 py-6 lg:mx-0 lg:px-10 lg:py-8">
      <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">{t("title")}</h1>
      <p className="text-[15px] leading-normal text-ink-body">{t("body")}</p>
      <Link
        href={localizedPath(locale, "/")}
        className="flex h-11 items-center justify-center self-start rounded-control bg-ink px-5 text-sm font-semibold text-ink-inverse"
      >
        {t("back")}
      </Link>
    </div>
  );
}

import Link from "next/link";
import { getLocale, getTranslations } from "next-intl/server";
import { IconLogoMark } from "@/components/icons";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";

export async function Logo() {
  const t = await getTranslations("brand");
  const locale = toLocale(await getLocale());
  return (
    <Link href={localizedPath(locale, "/")} className="flex items-center gap-2 rounded-control">
      <span aria-hidden="true" className="flex size-[26px] items-center justify-center rounded-[7px] bg-ink text-ink-inverse">
        <IconLogoMark className="size-[15px]" />
      </span>
      <span className="font-display text-[21px] font-bold tracking-[-0.02em]">{t("wordmark")}</span>
    </Link>
  );
}

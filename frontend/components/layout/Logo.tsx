import Link from "next/link";
import { getLocale, getTranslations } from "next-intl/server";
import { IconLogoMark } from "@/components/icons";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";

export async function Logo() {
  const t = await getTranslations("brand");
  const locale = toLocale(await getLocale());
  return (
    <Link href={localizedPath(locale, "/")} className="flex h-11 items-center gap-2 rounded-control">
      <IconLogoMark aria-hidden="true" className="size-5 text-level-3" />
      <span className="text-[17px] font-semibold tracking-[-0.02em]">{t("wordmark")}</span>
    </Link>
  );
}

import { getTranslations, setRequestLocale } from "next-intl/server";
import { IconLogoMark } from "@/components/icons";
import { toLocale } from "@/i18n/routing";

export default async function DiscoverPage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "brand" });

  return (
    <main className="mx-auto flex min-h-dvh max-w-3xl flex-col justify-center gap-6 px-4 py-16">
      <div className="flex items-center gap-2">
        <span className="flex size-[26px] items-center justify-center rounded-[7px] bg-ink text-ink-inverse">
          <IconLogoMark className="size-[15px]" />
        </span>
        <span className="font-display text-[21px] font-bold tracking-[-0.02em]">{t("wordmark")}</span>
      </div>
      <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">
        {t("tagline")}
      </h1>
    </main>
  );
}

import { getTranslations, setRequestLocale } from "next-intl/server";
import { toLocale } from "@/i18n/routing";

export default async function DiscoverPage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "brand" });

  return (
    <div className="px-4 py-6 lg:px-10 lg:py-8">
      <h1 className="font-display text-[28px] leading-none font-bold tracking-[-0.035em] lg:text-[40px]">
        {t("tagline")}
      </h1>
    </div>
  );
}

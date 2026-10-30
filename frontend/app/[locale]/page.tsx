import { setRequestLocale } from "next-intl/server";
import { CatalogScreen } from "@/components/catalog/CatalogScreen";
import { toLocale } from "@/i18n/routing";

export default async function DiscoverPage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  return <CatalogScreen locale={locale} sort="discover" />;
}

import { setRequestLocale } from "next-intl/server";
import { Landing } from "@/components/landing/Landing";
import { toLocale } from "@/i18n/routing";

export default async function HomePage({ params }: PageProps<"/[locale]">) {
  const locale = toLocale((await params).locale);
  setRequestLocale(locale);
  return <Landing locale={locale} audience="tech" />;
}

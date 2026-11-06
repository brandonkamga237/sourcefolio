import type { Metadata, Viewport } from "next";
import { Mona_Sans } from "next/font/google";
import { notFound } from "next/navigation";
import { hasLocale } from "next-intl";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { AppShell } from "@/components/layout/AppShell";
import { QueryProvider } from "@/components/providers/QueryProvider";
import { themeBootScript } from "@/components/theme/theme";
import { ThemeKeeper } from "@/components/theme/ThemeKeeper";
import { routing, toLocale } from "@/i18n/routing";
import "../globals.css";

// Headings only; the body and the figures use the system fonts, as GitHub does, so a low-end
// phone downloads one font file.
const mona = Mona_Sans({
  variable: "--font-mona",
  subsets: ["latin", "latin-ext"],
  weight: ["600", "700"],
});

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

// viewport-fit=cover lets the bottom tab bar sit above the iPhone home indicator through
// env(safe-area-inset-bottom).
export const viewport: Viewport = {
  viewportFit: "cover",
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0d1117" },
  ],
};

export async function generateMetadata({ params }: LayoutProps<"/[locale]">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "meta" });
  return {
    metadataBase: new URL(process.env.SITE_URL ?? "http://localhost:3000"),
    title: { default: t("title"), template: "%s · SourceFolio" },
    description: t("description"),
    // Pages inherit these; the image itself is the opengraph-image file of this segment.
    openGraph: { type: "website", siteName: "SourceFolio", locale: locale === "fr" ? "fr_FR" : "en_US" },
    twitter: { card: "summary_large_image" },
    alternates: {
      canonical: `/${locale}`,
      languages: { fr: "/fr", en: "/en", "x-default": "/en" },
    },
  };
}

export default async function LocaleLayout({ children, params }: LayoutProps<"/[locale]">) {
  const { locale } = await params;
  if (!hasLocale(routing.locales, locale)) {
    notFound();
  }
  setRequestLocale(locale);

  return (
    <html
      lang={locale}
      className={`${mona.variable} h-full antialiased`}
      // The boot script may set data-theme before React hydrates.
      suppressHydrationWarning
    >
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeBootScript }} />
      </head>
      <body className="backdrop min-h-full">
        <ThemeKeeper />
        <QueryProvider>
          <AppShell>{children}</AppShell>
        </QueryProvider>
      </body>
    </html>
  );
}

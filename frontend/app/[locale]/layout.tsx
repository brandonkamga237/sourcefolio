import type { Metadata, Viewport } from "next";
import { Bricolage_Grotesque, Geist, Geist_Mono } from "next/font/google";
import { notFound } from "next/navigation";
import { hasLocale } from "next-intl";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { AppShell } from "@/components/layout/AppShell";
import { QueryProvider } from "@/components/providers/QueryProvider";
import { themeBootScript } from "@/components/theme/theme";
import { ThemeKeeper } from "@/components/theme/ThemeKeeper";
import { routing, toLocale } from "@/i18n/routing";
import "../globals.css";

const bricolage = Bricolage_Grotesque({
  variable: "--font-bricolage",
  subsets: ["latin", "latin-ext"],
  weight: ["600", "700"],
});

const geist = Geist({
  variable: "--font-geist",
  subsets: ["latin", "latin-ext"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

// viewport-fit=cover lets the bottom tab bar sit above the iPhone home indicator through
// env(safe-area-inset-bottom).
export const viewport: Viewport = {
  viewportFit: "cover",
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#f5f5f1" },
    { media: "(prefers-color-scheme: dark)", color: "#0e100e" },
  ],
};

export async function generateMetadata({ params }: LayoutProps<"/[locale]">): Promise<Metadata> {
  const locale = toLocale((await params).locale);
  const t = await getTranslations({ locale, namespace: "meta" });
  return {
    metadataBase: new URL(process.env.SITE_URL ?? "http://localhost:3000"),
    title: { default: t("title"), template: "%s · SourceFolio" },
    description: t("description"),
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
      className={`${bricolage.variable} ${geist.variable} ${geistMono.variable} h-full antialiased`}
      // The boot script may set data-theme before React hydrates.
      suppressHydrationWarning
    >
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeBootScript }} />
      </head>
      <body className="min-h-full">
        <ThemeKeeper />
        <QueryProvider>
          <AppShell>{children}</AppShell>
        </QueryProvider>
      </body>
    </html>
  );
}

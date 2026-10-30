import { ImageResponse } from "next/og";
import { getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { og, ogBackground, OgLogo, ogFonts, OgSiteAndCode, ogSize } from "@/lib/og";

export const size = ogSize;
export const contentType = "image/png";
export const alt = "SourceFolio";

// The share image of every page without its own (home, Ranking, Me…), in the URL's language.
// Flat shapes and two fonts keep it far under the 300 KB WhatsApp accepts.
export default async function OpenGraphImage({ params }: { params: Promise<{ locale: string }> }) {
  const locale = toLocale((await params).locale);
  const [t, fonts] = await Promise.all([getTranslations({ locale }), ogFonts()]);
  const site = new URL(process.env.SITE_URL ?? "http://localhost:3000").host;

  return new ImageResponse(
    (
      <div style={{ ...ogBackground, width: "100%", height: "100%", display: "flex", padding: 72, fontFamily: "Geist", color: og.ink }}>
        <div style={{ display: "flex", flexDirection: "column", justifyContent: "space-between", width: 620 }}>
          <OgLogo />
          <div style={{ display: "flex", flexDirection: "column", gap: 26 }}>
            <div style={{ fontFamily: "Bricolage", fontSize: 76, lineHeight: 1.0, letterSpacing: -3.5 }}>{t("brand.tagline")}</div>
            <div style={{ fontSize: 30, lineHeight: 1.35, color: og.body }}>{t("og.lead")}</div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 14, fontSize: 26, color: og.accent }}>
            <div style={{ display: "flex", width: 12, height: 12, borderRadius: 6, background: og.accent }} />
            {site}
          </div>
        </div>
        <div style={{ display: "flex", flexGrow: 1, alignItems: "center", justifyContent: "flex-end" }}>
          <OgSiteAndCode scale={0.92} />
        </div>
      </div>
    ),
    { ...size, fonts },
  );
}

import { ImageResponse } from "next/og";
import { getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { siteLabel } from "@/lib/site-label";
import { getPublicShowcase } from "@/services/public-showcases";

export const size = { width: 1200, height: 630 };
export const contentType = "image/png";
export const alt = "SourceFolio";

// The WhatsApp, LinkedIn and X preview of a shared showcase, in the language of its URL. Flat
// colours and system text keep it far under the 300 KB WhatsApp accepts.
export default async function OpenGraphImage({ params }: { params: Promise<{ locale: string; slug: string }> }) {
  const { locale: raw, slug } = await params;
  const locale = toLocale(raw);
  const [t, showcase] = await Promise.all([
    getTranslations({ locale, namespace: "showcasePage" }),
    getPublicShowcase(slug),
  ]);
  const site = showcase ? siteLabel(showcase.siteUrl) : "SourceFolio";

  return new ImageResponse(
    (
      <div style={{ width: "100%", height: "100%", display: "flex", flexDirection: "column", justifyContent: "space-between", padding: 72, background: "#f5f5f1", color: "#121412" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ width: 64, height: 64, borderRadius: 16, background: "#121412", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <svg width={36} height={36} viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth={2.4} strokeLinecap="round" strokeLinejoin="round">
              <path d="M8 8l-4 4 4 4M16 8l4 4-4 4" />
            </svg>
          </div>
          <div style={{ fontSize: 40, fontWeight: 700, letterSpacing: -1 }}>sourcefolio</div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
          <div style={{ fontSize: site.length > 24 ? 64 : 88, fontWeight: 700, letterSpacing: -3, lineHeight: 1.05, wordBreak: "break-all" }}>{site}</div>
          {showcase ? <div style={{ fontSize: 40, color: "#2e312c" }}>{t("ogBy", { login: showcase.ownerLogin })}</div> : null}
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 16, fontSize: 32, color: "#2b45e0", fontWeight: 600 }}>
          {t("ogTagline")}
        </div>
      </div>
    ),
    size,
  );
}

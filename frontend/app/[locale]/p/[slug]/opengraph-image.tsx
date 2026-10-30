import { ImageResponse } from "next/og";
import { getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { formatStars, og, ogBackground, OgLogo, ogFonts, OgSiteAndCode, ogSize } from "@/lib/og";
import { siteLabel } from "@/lib/site-label";
import { getPublicShowcase } from "@/services/public-showcases";

export const size = ogSize;
export const contentType = "image/png";
export const alt = "SourceFolio";

// next/og draws PNG and JPEG only; a WebP preview, a slow or missing file falls back to the
// drawing. Fetched with a short budget: a crawler waits a few seconds at most.
async function previewDataUrl(url: string | null): Promise<string | null> {
  if (!url) return null;
  try {
    const response = await fetch(url, { signal: AbortSignal.timeout(3000) });
    if (!response.ok) return null;
    const bytes = Buffer.from(await response.arrayBuffer());
    const png = bytes.subarray(0, 4).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47]));
    const jpeg = bytes[0] === 0xff && bytes[1] === 0xd8;
    if (!png && !jpeg) return null;
    return `data:image/${png ? "png" : "jpeg"};base64,${bytes.toString("base64")}`;
  } catch {
    return null;
  }
}

// The WhatsApp, LinkedIn and X preview of a shared showcase, in the language of its URL: the
// author's own preview in a browser frame, the site, the author and the repository's stars.
export default async function OpenGraphImage({ params }: { params: Promise<{ locale: string; slug: string }> }) {
  const { locale: raw, slug } = await params;
  const locale = toLocale(raw);
  const [t, showcase, fonts] = await Promise.all([
    getTranslations({ locale, namespace: "showcasePage" }),
    getPublicShowcase(slug),
    ogFonts(),
  ]);
  const site = showcase ? siteLabel(showcase.siteUrl) : "SourceFolio";
  const preview = await previewDataUrl(showcase?.previewImageUrl ?? null);
  const stars = showcase?.repositoryStars ?? null;

  return new ImageResponse(
    (
      <div style={{ ...ogBackground, width: "100%", height: "100%", display: "flex", padding: 64, gap: 48, fontFamily: "Geist", color: og.ink }}>
        <div style={{ display: "flex", flexDirection: "column", justifyContent: "space-between", width: 560 }}>
          <OgLogo />
          <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
            <div style={{ fontFamily: "Bricolage", fontSize: site.length > 20 ? 58 : 78, lineHeight: 1.02, letterSpacing: -3, wordBreak: "break-all" }}>{site}</div>
            {showcase ? <div style={{ fontSize: 32, color: og.body }}>{t("ogBy", { login: showcase.ownerLogin })}</div> : null}
            {stars !== null ? (
              <div style={{ display: "flex", alignItems: "center", gap: 12, fontSize: 28, color: og.ink }}>
                <svg width={30} height={30} viewBox="0 0 24 24" fill={og.accent} stroke={og.accent} strokeWidth={1.6} strokeLinejoin="round">
                  <path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z" />
                </svg>
                {t("ogStars", { count: stars, formatted: formatStars(stars, locale) })}
              </div>
            ) : null}
          </div>
          <div style={{ display: "flex", fontSize: 28, color: og.accent }}>{t("ogTagline")}</div>
        </div>
        <div style={{ display: "flex", flexGrow: 1, alignItems: "center", justifyContent: "center" }}>
          {preview ? (
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                width: 480,
                borderRadius: 26,
                border: `2px solid ${og.line}`,
                background: og.surface,
                overflow: "hidden",
                transform: "rotate(1.5deg)",
                boxShadow: "0 30px 60px -28px rgba(18,20,18,0.45)",
              }}
            >
              <div style={{ display: "flex", gap: 8, padding: "14px 18px", borderBottom: `2px solid ${og.line}` }}>
                {[0, 1, 2].map((dot) => (
                  <div key={dot} style={{ display: "flex", width: 12, height: 12, borderRadius: 6, background: og.line }} />
                ))}
              </div>
              <img src={preview} alt="" width={480} height={300} style={{ objectFit: "cover", objectPosition: "top" }} />
            </div>
          ) : (
            <OgSiteAndCode scale={0.92} />
          )}
        </div>
      </div>
    ),
    { ...size, fonts },
  );
}

import { ImageResponse } from "next/og";
import { connection } from "next/server";
import { getFormatter, getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { og, OgLogo, ogFonts, ogSize } from "@/lib/og";
import { readPublicProfile } from "@/services/public-profiles";

export const size = ogSize;
export const contentType = "image/png";
export const alt = "SourceFolio";

const NAME_LENGTH = 34;

// The share image's renderer crashes on the shaping tables of scripts such as Arabic, and a
// crash here is a dropped connection: those names fall back to the login.
function drawable(text: string): boolean {
  return !/[^\p{Script=Latin}\p{Script=Common}\p{Script=Inherited}]/u.test(text);
}

// A missing avatar must not cost the member their preview, nor a slow GitHub the crawler's patience.
async function avatarData(login: string): Promise<string | null> {
  try {
    const response = await fetch(`https://github.com/${encodeURIComponent(login)}.png?size=240`, { signal: AbortSignal.timeout(3000) });
    if (!response.ok) return null;
    const type = response.headers.get("content-type") ?? "image/png";
    return `data:${type};base64,${Buffer.from(await response.arrayBuffer()).toString("base64")}`;
  } catch {
    return null;
  }
}

// Read fresh, never from the page's cache: the proxy does not check this address, and a profile
// taken down must stop being shareable at once.
export default async function OpenGraphImage({ params }: { params: Promise<{ locale: string; login: string }> }) {
  await connection();
  const { locale: raw, login } = await params;
  const locale = toLocale(raw);
  const card = await readPublicProfile(login);
  if (!card) return new Response(null, { status: 404 });

  const [t, format, fonts, avatar] = await Promise.all([getTranslations({ locale, namespace: "og.profile" }), getFormatter({ locale }), ogFonts(), avatarData(card.login)]);
  const { indicators } = card;
  const fullName = card.name && drawable(card.name) ? card.name : card.login;
  const name = fullName.length > NAME_LENGTH ? `${fullName.slice(0, NAME_LENGTH - 1)}…` : fullName;
  const place = [card.city && drawable(card.city) ? card.city : null, countryName(card.country, locale)].filter(Boolean).join(" · ");
  // French groups digits with a narrow space, a glyph the share image's font does not have.
  const number = (value: number) => format.number(value).replace(/\u202f/g, " ");
  const peak = Math.max(1, ...indicators.months.map((month) => month.contributions));
  // A figure at zero stays off a share image: an empty start is not something to advertise.
  const figures = [
    { value: indicators.activeMonths, shown: t("months", { active: indicators.activeMonths }) },
    { value: indicators.publicContributions, shown: t("contributions", { count: indicators.publicContributions, formatted: number(indicators.publicContributions) }) },
    { value: indicators.stars, shown: t("stars", { count: indicators.stars, formatted: number(indicators.stars) }) },
  ].filter((figure) => figure.value > 0);

  return new ImageResponse(
    (
      <div style={{ background: og.canvas, width: "100%", height: "100%", display: "flex", flexDirection: "column", justifyContent: "space-between", padding: 64, fontFamily: "Geist", color: og.ink }}>
        <OgLogo />
        <div style={{ display: "flex", alignItems: "center", gap: 40 }}>
          {avatar ? (
            <img src={avatar} alt="" width={180} height={180} style={{ borderRadius: 90 }} />
          ) : (
            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", width: 180, height: 180, borderRadius: 24, background: og.levels[3], color: og.canvas, fontWeight: 600, fontSize: 96 }}>
              {card.login.charAt(0).toUpperCase()}
            </div>
          )}
          <div style={{ display: "flex", flexDirection: "column", gap: 10, flex: 1 }}>
            <div style={{ fontWeight: 600, fontSize: 68, lineHeight: 1.05, letterSpacing: -2 }}>{name}</div>
            <div style={{ fontSize: 32, color: og.muted }}>{place}</div>
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 22 }}>
          <div style={{ display: "flex", gap: 8 }}>
            {indicators.months.map((month) => (
              <div
                key={month.month}
                style={{ display: "flex", width: 44, height: 44, borderRadius: 8, background: og.levels[month.contributions === 0 ? 0 : Math.min(4, 1 + Math.floor((month.contributions / peak) * 3.999))] }}
              />
            ))}
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: 28 }}>
            <div style={{ display: "flex", gap: 30 }}>
              {figures.map((figure) => <div key={figure.shown} style={{ display: "flex" }}>{figure.shown}</div>)}
            </div>
            <div style={{ display: "flex", fontSize: 24, color: og.muted }}>{t("source")}</div>
          </div>
        </div>
      </div>
    ),
    { ...size, fonts },
  );
}

import { readFile } from "node:fs/promises";
import { join } from "node:path";

// Share images (WhatsApp, LinkedIn, X) are drawn by next/og, which only reads static fonts: the
// site's own two faces, as WOFF files served from public/og. Read once per server process.
let fonts: Promise<{ name: string; data: Buffer; weight: 500 | 700; style: "normal" }[]> | null = null;

export function ogFonts() {
  fonts ??= Promise.all([
    readFile(join(process.cwd(), "public/og/bricolage-700.woff")),
    readFile(join(process.cwd(), "public/og/geist-500.woff")),
  ]).then(([display, body]) => [
    { name: "Bricolage", data: display, weight: 700 as const, style: "normal" as const },
    { name: "Geist", data: body, weight: 500 as const, style: "normal" as const },
  ]);
  return fonts;
}

export const ogSize = { width: 1200, height: 630 };

// The light theme's tokens: a share image has no dark mode.
export const og = {
  canvas: "#f5f5f1",
  surface: "#ffffff",
  ink: "#121412",
  body: "#2e312c",
  muted: "#5b5f58",
  line: "#e2e2dc",
  accent: "#2b45e0",
  accentSoft: "#e9ecfd",
  accentLine: "#c9d0fa",
  success: "#1d7a4b",
};

// The dot grid and accent glow of the site's backdrop.
export const ogBackground = {
  backgroundColor: og.canvas,
  backgroundImage: `radial-gradient(circle at 1px 1px, rgba(18,20,18,0.10) 1.5px, transparent 0)`,
  backgroundSize: "28px 28px",
};

export function OgLogo() {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 18 }}>
      <div style={{ width: 60, height: 60, borderRadius: 16, background: og.ink, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <svg width={34} height={34} viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth={2.4} strokeLinecap="round" strokeLinejoin="round">
          <path d="M8 8l-4 4 4 4M16 8l4 4-4 4" />
        </svg>
      </div>
      <div style={{ fontFamily: "Bricolage", fontSize: 40, letterSpacing: -1.5, color: og.ink }}>sourcefolio</div>
    </div>
  );
}

const bar = (width: string, color: string, indent = 0) => (
  <div style={{ display: "flex", height: 12, width, marginLeft: indent, borderRadius: 6, background: color }} />
);

// A site in front of its source: the promise of SourceFolio, drawn with shapes only.
export function OgSiteAndCode({ scale = 1 }: { scale?: number }) {
  const s = (value: number) => value * scale;
  return (
    <div style={{ display: "flex", position: "relative", width: s(470), height: s(400) }}>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: s(18),
          position: "absolute",
          top: 0,
          right: 0,
          width: s(390),
          height: s(290),
          padding: s(30),
          borderRadius: s(28),
          background: og.ink,
          transform: "rotate(-3deg)",
          boxShadow: "0 30px 60px -30px rgba(18,20,18,0.6)",
        }}
      >
        {bar("34%", og.accent)}
        {bar("62%", "rgba(255,255,255,0.28)", s(28))}
        {bar("48%", og.success, s(28))}
        {bar("56%", "rgba(255,255,255,0.28)", s(56))}
        {bar("40%", og.accentLine, s(28))}
        {bar("24%", og.accent)}
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          position: "absolute",
          bottom: 0,
          left: 0,
          width: s(340),
          height: s(230),
          borderRadius: s(28),
          border: `2px solid ${og.line}`,
          background: og.surface,
          transform: "rotate(2deg)",
          boxShadow: "0 30px 60px -30px rgba(18,20,18,0.4)",
          overflow: "hidden",
        }}
      >
        <div style={{ display: "flex", gap: s(8), padding: `${s(16)}px ${s(20)}px`, borderBottom: `2px solid ${og.line}` }}>
          {[0, 1, 2].map((dot) => (
            <div key={dot} style={{ display: "flex", width: s(14), height: s(14), borderRadius: s(7), background: og.line }} />
          ))}
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: s(14), padding: s(22), flexGrow: 1 }}>
          <div style={{ display: "flex", height: s(22), width: "78%", borderRadius: s(11), background: og.ink }} />
          {bar("52%", og.line)}
          <div style={{ display: "flex", gap: s(10), marginTop: "auto" }}>
            <div style={{ display: "flex", width: s(96), height: s(34), borderRadius: s(10), background: og.accent }} />
            <div style={{ display: "flex", width: s(70), height: s(34), borderRadius: s(10), border: `2px solid ${og.line}` }} />
          </div>
        </div>
      </div>
      <div style={{ display: "flex", position: "absolute", top: s(20), left: s(40), width: s(64), height: s(64), borderRadius: s(32), border: `${s(10)}px solid ${og.accentLine}` }} />
    </div>
  );
}

export function formatStars(count: number, locale: string): string {
  return new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(count);
}

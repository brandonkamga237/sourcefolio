import { readFile } from "node:fs/promises";
import { join } from "node:path";
import { landSquares, MAP_COLUMNS, MAP_ROWS, squaresPath } from "@/lib/map";

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
  canvas: "#ffffff",
  surface: "#ffffff",
  ink: "#1f2328",
  body: "#1f2328",
  muted: "#59636e",
  line: "#d1d9e0",
  accent: "#0969da",
  accentSoft: "#ddf4ff",
  accentLine: "#b6e3ff",
  success: "#1a7f37",
  star: "#bf8700",
  levels: ["#eff2f5", "#aceebb", "#4ac26b", "#2da44e", "#116329"],
};

export function OgLogo() {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 18 }}>
      <svg width={52} height={52} viewBox="0 0 24 24" fill={og.levels[3]}>
        <rect x="2" y="2" width="9" height="9" rx="2" fillOpacity={0.4} />
        <rect x="13" y="2" width="9" height="9" rx="2" fillOpacity={0.7} />
        <rect x="2" y="13" width="9" height="9" rx="2" />
        <rect x="13" y="13" width="9" height="9" rx="2" />
      </svg>
      <div style={{ fontFamily: "Bricolage", fontSize: 40, letterSpacing: -1.5, color: og.ink }}>sourcefolio</div>
    </div>
  );
}

// The continent in one colour, as a shape: no square here stands for real figures.
export function OgAfrica({ width }: { width: number }) {
  return (
    <svg width={width} height={(width * MAP_ROWS) / MAP_COLUMNS} viewBox={`0 0 ${MAP_COLUMNS * 10} ${MAP_ROWS * 10}`}>
      <path d={squaresPath(landSquares())} fill={og.levels[2]} stroke={og.levels[2]} strokeWidth={2} strokeLinejoin="round" />
    </svg>
  );
}

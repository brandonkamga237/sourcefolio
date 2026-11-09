import { connection } from "next/server";
import { getTranslations } from "next-intl/server";
import { toLocale } from "@/i18n/routing";
import { readPublicProfile } from "@/services/public-profiles";

const SQUARES = ["#9be9a8", "#40c463", "#30a14e", "#216e39"];

// Read fresh: the badge lives in other people's READMEs, and must stop the moment a profile is
// taken down. It states that a profile exists and carries no score.
export async function GET(_request: Request, { params }: { params: Promise<{ locale: string; login: string }> }) {
  await connection();
  const { locale, login } = await params;
  const card = await readPublicProfile(login);
  if (!card) return new Response(null, { status: 404 });
  const t = await getTranslations({ locale: toLocale(locale), namespace: "myProfile" });
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="172" height="24" role="img" aria-label="SourceFolio"><rect width="172" height="24" rx="4" fill="#0d1117"/>${SQUARES.map((color, index) => `<rect x="${8 + (index % 2) * 7}" y="${5 + Math.floor(index / 2) * 7}" width="6" height="6" rx="1.2" fill="${color}"/>`).join("")}<text x="29" y="16" font-family="-apple-system,Segoe UI,sans-serif" font-size="12" fill="#f0f6fc">${t("badge")}</text></svg>`;
  return new Response(svg, { headers: { "content-type": "image/svg+xml", "cache-control": "max-age=0, must-revalidate" } });
}

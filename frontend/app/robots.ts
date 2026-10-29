import type { MetadataRoute } from "next";
import { connection } from "next/server";

export default async function robots(): Promise<MetadataRoute.Robots> {
  // SITE_URL is read per request: the same image runs in every environment.
  await connection();
  const site = process.env.SITE_URL ?? "http://localhost:3000";
  return {
    rules: { userAgent: "*", allow: "/", disallow: ["/fr/me", "/en/me", "/api/"] },
    sitemap: `${site}/sitemap.xml`,
  };
}

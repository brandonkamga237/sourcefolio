import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./i18n/request.ts");

const site = new URL(process.env.SITE_URL ?? "http://localhost:3000");

const nextConfig: NextConfig = {
  // The Docker image runs the minimal server.js this emits.
  output: "standalone",
  // Previews live in the bucket: under /media on the site in production (Traefik to RustFS),
  // on RustFS's own port in development. next/image resizes them to WebP or AVIF.
  images: {
    formats: ["image/avif", "image/webp"],
    // Next refuses to fetch images from private addresses (SSRF). Only a local build, whose
    // bucket is on localhost, lifts it; a build for a public origin keeps the protection.
    dangerouslyAllowLocalIP: site.hostname === "localhost",
    remotePatterns: [
      { protocol: site.protocol.replace(":", "") as "http" | "https", hostname: site.hostname, pathname: "/media/**" },
      { protocol: "http", hostname: "localhost", port: "9000", pathname: "/sourcefolio-media/**" },
    ],
  },
  // The browser stays on one origin, as in production behind Traefik.
  async rewrites() {
    const api = process.env.API_INTERNAL_URL ?? "http://localhost:8080";
    return [
      { source: "/api/:path*", destination: `${api}/api/:path*` },
      { source: "/oauth2/:path*", destination: `${api}/oauth2/:path*` },
      { source: "/login/oauth2/:path*", destination: `${api}/login/oauth2/:path*` },
    ];
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default withNextIntl(nextConfig);

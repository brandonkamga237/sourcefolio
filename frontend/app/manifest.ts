import type { MetadataRoute } from "next";

// One manifest for both languages: "/" sends each visitor to /fr or /en from their browser.
export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "SourceFolio",
    short_name: "SourceFolio",
    description: "Portfolios you can read the code of. Des portfolios dont tu peux lire le code.",
    start_url: "/",
    display: "standalone",
    background_color: "#f5f5f1",
    theme_color: "#f5f5f1",
    icons: [
      { src: "/icon/192", sizes: "192x192", type: "image/png" },
      { src: "/icon/512", sizes: "512x512", type: "image/png" },
      { src: "/icon/512", sizes: "512x512", type: "image/png", purpose: "maskable" },
    ],
  };
}

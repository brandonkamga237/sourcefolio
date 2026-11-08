import type { MetadataRoute } from "next";

// One manifest for both languages: "/" sends each visitor to /fr or /en from their browser.
export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "SourceFolio",
    short_name: "SourceFolio",
    description: "From open source to opportunity. De l'open source à l'opportunité.",
    start_url: "/",
    display: "standalone",
    background_color: "#ffffff",
    theme_color: "#ffffff",
    icons: [
      { src: "/icon/192", sizes: "192x192", type: "image/png" },
      { src: "/icon/512", sizes: "512x512", type: "image/png" },
      { src: "/icon/512", sizes: "512x512", type: "image/png", purpose: "maskable" },
    ],
  };
}

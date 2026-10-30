import { permanentRedirect } from "next/navigation";
import { localizedPath } from "@/i18n/paths";
import { toLocale } from "@/i18n/routing";

// My portfolio became part of Me (three tabs); shared and bookmarked links still land there.
export default async function MyPortfolioPage({ params }: PageProps<"/[locale]/me/portfolio">) {
  permanentRedirect(localizedPath(toLocale((await params).locale), "/me"));
}

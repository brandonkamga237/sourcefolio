import createMiddleware from "next-intl/middleware";
import { NextResponse, type NextRequest } from "next/server";
import { routing } from "./i18n/routing";
import { apiResourceExists } from "./lib/page-exists";

// Sends "/" and any path without a locale to /fr or /en, from the browser language.
const intl = createMiddleware(routing);

// Pages backed by one API resource: the page path, and the API path that says whether it exists.
const RESOURCE_PAGES: { page: RegExp; api: (key: string) => string }[] = [
  { page: /^\/(fr|en)\/u\/([^/]+)\/?$/, api: (login) => `/api/profiles/${encodeURIComponent(login)}` },
];

// The retired portfolio pages, whose links were shared before the V2.
const RETIRED_PAGES = /^\/(fr|en)\/p\/[^/]+\/?$/;

function notFound(request: NextRequest, locale: string) {
  return NextResponse.rewrite(new URL(`/${locale}/not-found-page`, request.url), { status: 404 });
}

// A retired page, or a profile that does not exist (or is private), gets a real 404 (the
// localised not-found page of the catch-all route), so search engines drop dead links and
// crawlers see the truth.
export default async function proxy(request: NextRequest) {
  const retired = RETIRED_PAGES.exec(request.nextUrl.pathname);
  if (retired) return notFound(request, retired[1]);
  for (const { page, api } of RESOURCE_PAGES) {
    const match = page.exec(request.nextUrl.pathname);
    if (match && !(await apiResourceExists(api(decodeURIComponent(match[2]))))) {
      return notFound(request, match[1]);
    }
  }
  return intl(request);
}

export const config = {
  // The API and the GitHub sign-in belong to the backend; files and the generated app icons
  // keep their own path (the icons have no extension, so the dot rule does not catch them).
  matcher: "/((?!api|oauth2|login|_next|_vercel|icon|apple-icon|.*\\..*).*)",
};

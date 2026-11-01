import createMiddleware from "next-intl/middleware";
import { NextResponse, type NextRequest } from "next/server";
import { routing } from "./i18n/routing";
import { showcaseExists } from "./lib/showcase-exists";

// Sends "/" and any path without a locale to /fr or /en, from the browser language.
const intl = createMiddleware(routing);

const SHOWCASE_PAGE = /^\/(fr|en)\/p\/([^/]+)\/?$/;

// A showcase that does not exist gets a real 404 (the localised not-found page of the
// catch-all route), so search engines drop dead links and crawlers see the truth.
export default async function proxy(request: NextRequest) {
  const match = SHOWCASE_PAGE.exec(request.nextUrl.pathname);
  if (match && !(await showcaseExists(decodeURIComponent(match[2])))) {
    return NextResponse.rewrite(new URL(`/${match[1]}/not-found-showcase`, request.url), { status: 404 });
  }
  return intl(request);
}

export const config = {
  // The API and the GitHub sign-in belong to the backend; files and the generated app icons
  // keep their own path (the icons have no extension, so the dot rule does not catch them).
  matcher: "/((?!api|oauth2|login|_next|_vercel|icon|apple-icon|.*\\..*).*)",
};

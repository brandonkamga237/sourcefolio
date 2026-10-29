import createMiddleware from "next-intl/middleware";
import { routing } from "./i18n/routing";

// Sends "/" and any path without a locale to /fr or /en, from the browser language.
export default createMiddleware(routing);

export const config = {
  // The API and the GitHub sign-in belong to the backend; files and the generated app icons
  // keep their own path (the icons have no extension, so the dot rule does not catch them).
  matcher: "/((?!api|oauth2|login|_next|_vercel|icon|apple-icon|.*\\..*).*)",
};

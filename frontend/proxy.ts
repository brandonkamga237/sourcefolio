import createMiddleware from "next-intl/middleware";
import { routing } from "./i18n/routing";

// Sends "/" and any path without a locale to /fr or /en, from the browser language.
export default createMiddleware(routing);

export const config = {
  // The API and the GitHub sign-in belong to the backend; files keep their own path.
  matcher: "/((?!api|oauth2|login|_next|_vercel|.*\\..*).*)",
};

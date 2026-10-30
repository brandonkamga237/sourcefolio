import type { routing } from "@/i18n/routing";
import type messages from "./messages/en.json";

// Typed keys: a t("...") call with a key that does not exist fails pnpm typecheck.
declare module "next-intl" {
  interface AppConfig {
    Locale: (typeof routing.locales)[number];
    Messages: typeof messages;
  }
}

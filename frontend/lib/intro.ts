// Shared by the server, which skips a dismissed Discover intro, and the button that dismisses it.
export const INTRO_COOKIE = "sf-intro";

/** Hidden for two weeks, then shown again: a returning visitor gets a reminder now and then. */
export const INTRO_HIDDEN_SECONDS = 14 * 24 * 60 * 60;

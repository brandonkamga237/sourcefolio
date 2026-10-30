export const themes = ["system", "light", "dark"] as const;
export type Theme = (typeof themes)[number];

export const THEME_STORAGE_KEY = "sourcefolio-theme";

// Runs in <head> before the first paint, so a member who chose a theme never sees the other
// one flash. "system" removes the attribute and lets prefers-color-scheme decide.
export const themeBootScript = `try{var t=localStorage.getItem("${THEME_STORAGE_KEY}");if(t==="light"||t==="dark")document.documentElement.dataset.theme=t}catch(e){}`;

export function applyTheme(theme: Theme): void {
  const root = document.documentElement;
  if (theme === "system") delete root.dataset.theme;
  else root.dataset.theme = theme;
  try {
    if (theme === "system") localStorage.removeItem(THEME_STORAGE_KEY);
    else localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // Private browsing can refuse storage: the theme still applies to this page.
  }
}

export function storedTheme(): Theme {
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    return stored === "light" || stored === "dark" ? stored : "system";
  } catch {
    return "system";
  }
}

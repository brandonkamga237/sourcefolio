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

/** Reveals the new theme from the control that asked for it; a plain switch where the API or motion is off. */
export function changeTheme(theme: Theme, origin?: { x: number; y: number }): void {
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (!document.startViewTransition || reduced) {
    applyTheme(theme);
    return;
  }
  const root = document.documentElement;
  if (origin) {
    root.style.setProperty("--vt-x", `${origin.x}px`);
    root.style.setProperty("--vt-y", `${origin.y}px`);
  }
  root.classList.add("theme-switching");
  void document.startViewTransition(() => applyTheme(theme)).finished.finally(() => root.classList.remove("theme-switching"));
}


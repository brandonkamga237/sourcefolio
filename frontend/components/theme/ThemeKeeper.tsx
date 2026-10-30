"use client";

import { useLayoutEffect } from "react";
import { storedTheme } from "./theme";

// Switching language swaps the root layout, and with it the <html> element the boot script
// had marked: the chosen theme was lost until the next full load. This puts it back before
// the browser paints, on every mount of the root layout.
export function ThemeKeeper() {
  useLayoutEffect(() => {
    const theme = storedTheme();
    if (theme === "system") delete document.documentElement.dataset.theme;
    else document.documentElement.dataset.theme = theme;
  }, []);
  return null;
}

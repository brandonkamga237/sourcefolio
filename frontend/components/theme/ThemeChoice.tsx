"use client";

import { useSyncExternalStore } from "react";
import { applyTheme, storedTheme, themes, type Theme } from "./theme";

interface ThemeChoiceProps {
  label: string;
  names: Record<Theme, string>;
}

const CHANGE_EVENT = "sourcefolio-theme-change";

function subscribe(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener(CHANGE_EVENT, onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener(CHANGE_EVENT, onChange);
  };
}

function choose(theme: Theme) {
  applyTheme(theme);
  window.dispatchEvent(new Event(CHANGE_EVENT));
}

// Stored in the browser for now; once members sign in, the choice also goes to the account.
export function ThemeChoice({ label, names }: ThemeChoiceProps) {
  const current = useSyncExternalStore(subscribe, storedTheme, (): Theme => "system");

  return (
    <fieldset className="grid grid-cols-3 gap-1 rounded-control bg-surface-sunken p-1">
      <legend className="sr-only">{label}</legend>
      {themes.map((theme) => (
        <label
          key={theme}
          className="flex h-11 cursor-pointer items-center justify-center rounded-segment text-sm text-ink-muted has-checked:bg-surface has-checked:font-medium has-checked:text-ink has-checked:shadow-[0_1px_2px_rgba(18,20,18,0.12)] has-focus-visible:outline-2 has-focus-visible:outline-accent"
        >
          <input
            type="radio"
            name="theme"
            value={theme}
            checked={current === theme}
            onChange={() => choose(theme)}
            className="sr-only"
          />
          {names[theme]}
        </label>
      ))}
    </fieldset>
  );
}

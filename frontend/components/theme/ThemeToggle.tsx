"use client";

import { useSyncExternalStore } from "react";
import { IconAuto, IconMoon, IconSun } from "@/components/icons";
import { changeTheme, storedTheme, themes, type Theme } from "./theme";

const CHANGE_EVENT = "sourcefolio-theme-change";
const ICON = { system: IconAuto, light: IconSun, dark: IconMoon } as const;

function subscribe(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener(CHANGE_EVENT, onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener(CHANGE_EVENT, onChange);
  };
}

interface ThemeToggleProps {
  label: string;
  names: Record<Theme, string>;
}

/** One tap moves to the next theme; the label says which is on, so it reads without the icon. */
export function ThemeToggle({ label, names }: ThemeToggleProps) {
  const current = useSyncExternalStore(subscribe, storedTheme, (): Theme => "system");
  const Icon = ICON[current];
  return (
    <button
      type="button"
      aria-label={`${label} : ${names[current]}`}
      title={names[current]}
      onClick={(event) => {
        const next = themes[(themes.indexOf(current) + 1) % themes.length];
        changeTheme(next, { x: event.clientX, y: event.clientY });
        window.dispatchEvent(new Event(CHANGE_EVENT));
      }}
      className="flex size-11 items-center justify-center rounded-control text-ink-muted transition-colors duration-(--dur-1) hover:bg-surface-sunken hover:text-ink"
    >
      <Icon aria-hidden="true" className="size-[18px]" />
    </button>
  );
}

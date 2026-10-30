"use client";

import { useState, type ReactNode } from "react";
import { IconClose } from "@/components/icons";
import { INTRO_COOKIE, INTRO_HIDDEN_SECONDS } from "@/lib/intro";

interface DismissibleIntroProps {
  closeLabel: string;
  children: ReactNode;
}

// The band fades up and away at once; the cookie keeps it hidden for two weeks. No refresh:
// the feed below stays where it is.
export function DismissibleIntro({ closeLabel, children }: DismissibleIntroProps) {
  const [state, setState] = useState<"shown" | "leaving" | "gone">("shown");
  if (state === "gone") return null;

  return (
    <div
      className={state === "leaving" ? "animate-[collapse_220ms_var(--ease-out-soft)_forwards]" : "animate-rise"}
      onAnimationEnd={() => {
        if (state === "leaving") setState("gone");
      }}
    >
      <div className="relative">
        {children}
        <button
          type="button"
          aria-label={closeLabel}
          title={closeLabel}
          onClick={() => {
            document.cookie = `${INTRO_COOKIE}=hidden; path=/; max-age=${INTRO_HIDDEN_SECONDS}; samesite=lax`;
            // Reduced motion shortens the animation to nothing; leave at once rather than wait for it.
            setState(window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "gone" : "leaving");
          }}
          className="press absolute top-1.5 right-1.5 flex size-11 items-center justify-center rounded-full text-ink-muted transition-colors duration-150 hover:bg-surface-sunken hover:text-ink"
        >
          <IconClose className="size-[18px]" />
        </button>
      </div>
    </div>
  );
}

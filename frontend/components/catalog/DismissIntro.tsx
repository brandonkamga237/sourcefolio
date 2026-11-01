"use client";

import { useState, type ReactNode } from "react";
import { IconClose } from "@/components/icons";

interface DismissibleIntroProps {
  closeLabel: string;
  /** The page's heading, kept for screen readers once the band is closed. */
  heading: string;
  children: ReactNode;
}

// The band fades up and away; nothing is remembered, so a reload shows it again. The feed
// below stays where it is.
export function DismissibleIntro({ closeLabel, heading, children }: DismissibleIntroProps) {
  const [state, setState] = useState<"shown" | "leaving" | "gone">("shown");
  if (state === "gone") return <h1 className="sr-only">{heading}</h1>;

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

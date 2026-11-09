"use client";

import { useEffect } from "react";

/** One listener for every .spotlight card, on fine pointers only. */
export function PointerLight() {
  useEffect(() => {
    if (!window.matchMedia("(hover: hover) and (pointer: fine)").matches) return;
    const move = (event: PointerEvent) => {
      const card = (event.target as Element).closest?.<HTMLElement>(".spotlight");
      if (!card) return;
      const box = card.getBoundingClientRect();
      card.style.setProperty("--x", `${event.clientX - box.left}px`);
      card.style.setProperty("--y", `${event.clientY - box.top}px`);
    };
    document.addEventListener("pointermove", move, { passive: true });
    return () => document.removeEventListener("pointermove", move);
  }, []);
  return null;
}

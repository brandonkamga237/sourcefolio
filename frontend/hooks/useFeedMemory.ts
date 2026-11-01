"use client";

import { useEffect, useRef } from "react";
import { consumeBackNavigation } from "@/lib/back-navigation";

const MAX_AGE_MS = 30 * 60_000;

interface Remembered<T> {
  state: T;
  scrollY: number;
  at: number;
}

/**
 * Keeps a feed's loaded state and scroll position for the tab's session, under the page's
 * address (filters included), and gives them back when the visitor returns with Back: the
 * spec's rule that returning from a showcase finds the catalogue where it was left, even
 * past the pages loaded on request.
 */
export function useFeedMemory<T>(snapshot: T, restore: (state: T) => void) {
  const latest = useRef(snapshot);
  const scrollY = useRef(0);
  const restoreRef = useRef(restore);

  useEffect(() => {
    latest.current = snapshot;
    restoreRef.current = restore;
  });

  useEffect(() => {
    const key = `feed:${window.location.pathname}${window.location.search}`;
    if (consumeBackNavigation()) {
      const saved = read<T>(key);
      if (saved) {
        restoreRef.current(saved.state);
        // Twice a frame later: once the restored cards are on screen, and after the router's
        // own scroll handling, which would otherwise win.
        requestAnimationFrame(() => requestAnimationFrame(() => window.scrollTo({ top: saved.scrollY, behavior: "instant" })));
      }
    }

    let frame = 0;
    function onScroll() {
      cancelAnimationFrame(frame);
      frame = requestAnimationFrame(() => {
        scrollY.current = window.scrollY;
      });
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", onScroll);
      cancelAnimationFrame(frame);
      write(key, { state: latest.current, scrollY: scrollY.current, at: Date.now() });
    };
  }, []);
}

// Session storage can be full, disabled or private: the feed then simply starts at the top.
function read<T>(key: string): Remembered<T> | null {
  try {
    const raw = window.sessionStorage.getItem(key);
    if (!raw) return null;
    const saved = JSON.parse(raw) as Remembered<T>;
    return Date.now() - saved.at < MAX_AGE_MS ? saved : null;
  } catch {
    return null;
  }
}

function write<T>(key: string, value: Remembered<T>) {
  try {
    window.sessionStorage.setItem(key, JSON.stringify(value));
  } catch {
    // Nothing kept: Back returns to the top of the feed.
  }
}

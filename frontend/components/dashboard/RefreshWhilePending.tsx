"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

interface RefreshWhilePendingProps {
  everyMs?: number;
  maxAttempts?: number;
}

// Detection runs in the background right after sign-in and usually takes a few seconds: the
// page asks the server again until the scan exists, then stops. Bounded, so a stuck job never
// keeps a phone polling.
export function RefreshWhilePending({ everyMs = 3000, maxAttempts = 10 }: RefreshWhilePendingProps) {
  const router = useRouter();

  useEffect(() => {
    let attempts = 0;
    const timer = window.setInterval(() => {
      attempts += 1;
      router.refresh();
      if (attempts >= maxAttempts) window.clearInterval(timer);
    }, everyMs);
    return () => window.clearInterval(timer);
  }, [router, everyMs, maxAttempts]);

  return null;
}

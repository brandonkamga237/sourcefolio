"use client";

import Link from "next/link";
import { useEffect, useSyncExternalStore } from "react";
import { IconChevronLeft } from "@/components/icons";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

const KEY = "sourcefolio-last-ranking";

/** Called by the rankings page: a profile then leads back to the same sort and filters. */
export function RememberRanking({ href }: { href: string }) {
  useEffect(() => {
    try {
      sessionStorage.setItem(KEY, href);
    } catch {
      // Private browsing can refuse storage: the link then goes to the plain rankings.
    }
  }, [href]);
  return null;
}

const never = () => () => {};
function remembered(): string | null {
  try {
    return sessionStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function BackToRankings({ locale, label }: { locale: Locale; label: string }) {
  const href = useSyncExternalStore(never, remembered, () => null) ?? localizedPath(locale, "/rankings");
  return (
    <Link href={href} className="flex min-h-11 w-fit items-center gap-1 text-[15px] text-ink-soft hover:text-ink">
      <IconChevronLeft aria-hidden="true" className="size-4" />
      {label}
    </Link>
  );
}

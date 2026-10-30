"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { localizedPath, withoutLocale } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

interface PublishLinkProps {
  locale: Locale;
  label: string;
  className: string;
}

// The way in to Me, shown everywhere except in Me itself, where it would lead nowhere new.
export function PublishLink({ locale, label, className }: PublishLinkProps) {
  const pathname = withoutLocale(usePathname());
  if (pathname === "/me" || pathname.startsWith("/me/")) return null;
  return (
    <Link href={localizedPath(locale, "/me")} className={className}>
      {label}
    </Link>
  );
}

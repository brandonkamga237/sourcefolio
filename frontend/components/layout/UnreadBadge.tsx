"use client";

import { useQuery } from "@tanstack/react-query";
import { plural, type PluralTemplates } from "@/lib/plural";
import { fetchUnread, unreadKey } from "@/services/feedback";

interface UnreadBadgeProps {
  locale: string;
  /** Contains {count}: read by screen readers instead of the bare number. */
  label: PluralTemplates;
  className?: string;
}

// The author's new feedback, on the Me tab wherever they are: the spec's notification for each
// comment, inside the app. Read once in a while and when the window comes back; a visitor gets 0.
export function UnreadBadge({ locale, label, className = "" }: UnreadBadgeProps) {
  const { data: count = 0 } = useQuery({ queryKey: unreadKey, queryFn: fetchUnread, staleTime: 60_000, refetchInterval: 5 * 60_000 });
  if (count === 0) return null;
  return (
    <span
      role="status"
      aria-label={plural(label, count, locale)}
      className={`flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-like px-1 font-mono text-[11px] leading-none font-semibold text-white tabular-nums ${className}`}
    >
      {count > 99 ? "99+" : count}
    </span>
  );
}

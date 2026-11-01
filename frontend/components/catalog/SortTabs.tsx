"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

export interface SortTab {
  key: string;
  href: string;
  label: string;
  current: boolean;
}

interface SortTabsProps {
  label: string;
  tabs: SortTab[];
}

function Tabs({ label, tabs, query }: SortTabsProps & { query: string }) {
  return (
    <nav aria-label={label} className="flex shrink-0 gap-2">
      {tabs.map((tab) => (
        <Link
          key={tab.key}
          href={query ? `${tab.href}?${query}` : tab.href}
          aria-current={tab.current ? "page" : undefined}
          className={`press flex h-11 items-center rounded-full px-3.5 text-sm font-semibold transition-colors duration-150 ${
            tab.current ? "bg-ink text-ink-inverse" : "border border-line bg-surface text-ink hover:border-ink-muted"
          }`}
        >
          {tab.label}
        </Link>
      ))}
    </nav>
  );
}

/** The links before the address is known (first paint): the same tabs, filters not yet carried. */
export function StaticSortTabs(props: SortTabsProps) {
  return <Tabs {...props} query="" />;
}

// Switching order keeps the visitor's search and filters: they narrow, the order only sorts.
export function SortTabs(props: SortTabsProps) {
  const searchParams = useSearchParams();
  return <Tabs {...props} query={searchParams.toString()} />;
}

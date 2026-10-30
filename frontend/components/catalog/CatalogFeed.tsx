"use client";

import { useState } from "react";
import type { Locale } from "@/i18n/routing";
import { localizedPath } from "@/i18n/paths";
import type { CatalogPage, CatalogSort } from "@/services/catalog-types";
import { fetchCatalogPage } from "@/services/catalog-client";
import { ShowcaseCard } from "./ShowcaseCard";

interface CatalogFeedProps {
  locale: Locale;
  sort: CatalogSort;
  firstPage: CatalogPage;
  labels: {
    /** Contains {login}. */
    by: string;
    loadMore: string;
    loading: string;
    loadFailed: string;
  };
}

// The first page arrives rendered by the server; the next ones load on request, with a button
// rather than an endless scroll, so the footer stays reachable and a phone loads only what is
// asked for.
export function CatalogFeed({ locale, sort, firstPage, labels }: CatalogFeedProps) {
  const [cards, setCards] = useState(firstPage.items);
  const [cursor, setCursor] = useState(firstPage.nextCursor);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);

  async function loadMore() {
    if (!cursor) return;
    setLoading(true);
    setFailed(false);
    const page = await fetchCatalogPage(sort, cursor);
    setLoading(false);
    if (!page) {
      setFailed(true);
      return;
    }
    // The cursor guarantees no overlap; the filter only guards against a double tap.
    setCards((current) => [...current, ...page.items.filter((card) => !current.some((seen) => seen.slug === card.slug))]);
    setCursor(page.nextCursor);
  }

  return (
    <div className="flex flex-col gap-3">
      <ul className="grid gap-3 sm:grid-cols-2">
        {cards.map((card) => (
          <li key={card.slug}>
            <ShowcaseCard card={card} href={localizedPath(locale, `/p/${card.slug}`)} byLabel={labels.by.replace("{login}", card.ownerLogin)} />
          </li>
        ))}
      </ul>
      {cursor ? (
        <button
          type="button"
          onClick={loadMore}
          disabled={loading}
          aria-busy={loading}
          className="flex h-11 items-center justify-center rounded-control border border-ink px-5 text-sm font-semibold text-ink disabled:opacity-60"
        >
          {loading ? labels.loading : labels.loadMore}
        </button>
      ) : null}
      {failed ? (
        <p role="alert" className="text-[13px] text-ink-body">
          {labels.loadFailed}
        </p>
      ) : null}
    </div>
  );
}

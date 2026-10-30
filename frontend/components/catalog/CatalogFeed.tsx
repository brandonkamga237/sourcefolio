"use client";

import { useState, type MouseEvent } from "react";
import type { Locale } from "@/i18n/routing";
import { localizedPath } from "@/i18n/paths";
import type { CatalogPage, CatalogSort } from "@/services/catalog-types";
import { fetchCatalogPage } from "@/services/catalog-client";
import { ShowcaseCard } from "./ShowcaseCard";
import { ShowcasePanel, type ShowcasePanelLabels } from "./ShowcasePanel";

interface CatalogFeedProps {
  locale: Locale;
  sort: CatalogSort;
  firstPage: CatalogPage;
  labels: {
    loadMore: string;
    loading: string;
    loadFailed: string;
    panel: ShowcasePanelLabels;
  };
}

const WIDE = "(min-width: 1024px)";

// The first page arrives rendered by the server; the next ones load on request, with a button
// rather than an endless scroll, so a phone loads only what is asked for. On a wide screen the
// first card starts open in the side panel, as in the mockup.
export function CatalogFeed({ locale, sort, firstPage, labels }: CatalogFeedProps) {
  const [cards, setCards] = useState(firstPage.items);
  const [cursor, setCursor] = useState(firstPage.nextCursor);
  const [selected, setSelected] = useState<string | null>(firstPage.items[0]?.slug ?? null);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);
  const open = cards.find((card) => card.slug === selected) ?? null;

  function openInPanel(slug: string, event: MouseEvent<HTMLAnchorElement>) {
    if (!window.matchMedia(WIDE).matches || event.metaKey || event.ctrlKey) return;
    event.preventDefault();
    setSelected(slug);
  }

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
    <div className="flex items-start gap-7">
      <div className="flex min-w-0 flex-1 flex-col gap-4">
        <ul className="grid grid-cols-2 gap-3 sm:gap-4 md:[grid-template-columns:repeat(auto-fill,minmax(210px,1fr))]">
          {cards.map((card) => (
            <li key={card.slug}>
              <ShowcaseCard
                card={card}
                href={localizedPath(locale, `/p/${card.slug}`)}
                selected={open?.slug === card.slug}
                onOpen={(event) => openInPanel(card.slug, event)}
              />
            </li>
          ))}
        </ul>
        {cursor ? (
          <button
            type="button"
            onClick={loadMore}
            disabled={loading}
            aria-busy={loading}
            className="flex h-11 items-center justify-center self-center rounded-full border border-ink px-6 text-sm font-semibold text-ink disabled:opacity-60"
          >
            {loading ? labels.loading : labels.loadMore}
          </button>
        ) : null}
        {failed ? (
          <p role="alert" className="text-center text-[13px] text-ink-body">
            {labels.loadFailed}
          </p>
        ) : null}
      </div>

      {open ? (
        <aside className="sticky top-6 hidden w-[360px] shrink-0 lg:block xl:w-[400px]">
          <ShowcasePanel
            card={open}
            href={localizedPath(locale, `/p/${open.slug}`)}
            locale={locale}
            labels={labels.panel}
            onClose={() => setSelected(null)}
          />
        </aside>
      ) : null}
    </div>
  );
}

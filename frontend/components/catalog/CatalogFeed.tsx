"use client";

import { useCallback, useEffect, useState, type MouseEvent } from "react";
import type { Locale } from "@/i18n/routing";
import { localizedPath } from "@/i18n/paths";
import type { CatalogFilters, CatalogPage, CatalogSort } from "@/services/catalog-types";
import { useFeedMemory } from "@/hooks/useFeedMemory";
import { fetchCatalogPage } from "@/services/catalog-client";
import { ShowcaseCard, type ShowcaseCardLabels } from "./ShowcaseCard";
import { ShowcasePanel, type ShowcasePanelLabels } from "./ShowcasePanel";

interface CatalogFeedProps {
  locale: Locale;
  sort: CatalogSort;
  /** The visitor's filters; the next pages load under the same ones. */
  filters: CatalogFilters;
  firstPage: CatalogPage;
  labels: {
    loadMore: string;
    loading: string;
    loadFailed: string;
    panel: ShowcasePanelLabels;
    card: ShowcaseCardLabels;
  };
}

const WIDE = "(min-width: 1024px)";

// The first page arrives rendered by the server; the next ones load on request, with a button
// rather than an endless scroll, so a phone loads only what is asked for. On a wide screen the
// panel opens only when a card is chosen: until then the grid has the whole width to explore.
export function CatalogFeed({ locale, sort, filters, firstPage, labels }: CatalogFeedProps) {
  const [cards, setCards] = useState(firstPage.items);
  const [cursor, setCursor] = useState(firstPage.nextCursor);
  const [selected, setSelected] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);
  const [direction, setDirection] = useState<"next" | "prev" | "none">("none");
  // Back from a showcase finds the same cards (pages loaded on request included) and the same
  // place in the list.
  useFeedMemory({ cards, cursor, selected }, (saved) => {
    setCards(saved.cards);
    setCursor(saved.cursor);
    setSelected(saved.selected);
  });
  const openIndex = cards.findIndex((card) => card.slug === selected);
  const open = openIndex >= 0 ? cards[openIndex] : null;

  function openInPanel(slug: string, event: MouseEvent<HTMLAnchorElement>) {
    if (!window.matchMedia(WIDE).matches || event.metaKey || event.ctrlKey) return;
    event.preventDefault();
    setDirection("none");
    setSelected(slug);
  }

  async function loadMore() {
    if (!cursor) return;
    setLoading(true);
    setFailed(false);
    const page = await fetchCatalogPage(sort, cursor, filters);
    setLoading(false);
    if (!page) {
      setFailed(true);
      return;
    }
    // The cursor guarantees no overlap; the filter only guards against a double tap.
    setCards((current) => [...current, ...page.items.filter((card) => !current.some((seen) => seen.slug === card.slug))]);
    setCursor(page.nextCursor);
  }

  // Browse from the panel without reaching for the grid: the card follows into view, and the
  // next page loads by itself past the last card.
  const step = useCallback(
    (delta: 1 | -1) => {
      const target = cards[openIndex + delta];
      if (!target) return;
      setDirection(delta === 1 ? "next" : "prev");
      setSelected(target.slug);
      if (openIndex + delta === cards.length - 1 && cursor && !loading) void loadMore();
      document.querySelector(`[data-card="${CSS.escape(target.slug)}"]`)?.scrollIntoView({ block: "nearest", behavior: "smooth" });
    },
    // loadMore is redefined each render and reads the latest cursor itself.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [cards, openIndex, cursor, loading],
  );

  useEffect(() => {
    if (!open) return;
    function onKey(event: KeyboardEvent) {
      const target = event.target as HTMLElement | null;
      if (target?.closest("input, textarea, select, [contenteditable=true], [role=dialog]")) return;
      if (event.key === "ArrowRight") step(1);
      else if (event.key === "ArrowLeft") step(-1);
      else if (event.key === "Escape") setSelected(null);
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, step]);

  return (
    <div className="flex items-start gap-7">
      <div className="flex min-w-0 flex-1 flex-col gap-4">
        <ul className="grid grid-cols-2 gap-3 sm:gap-4 md:[grid-template-columns:repeat(auto-fill,minmax(210px,1fr))]">
          {cards.map((card, index) => (
            <li key={card.slug} data-card={card.slug} className="scroll-mt-28">
              <ShowcaseCard
                card={card}
                href={localizedPath(locale, `/p/${card.slug}`)}
                index={index}
                locale={locale}
                labels={labels.card}
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
            className="press flex h-11 items-center justify-center self-center rounded-full border border-ink bg-surface px-6 text-sm font-semibold text-ink transition-colors duration-150 hover:bg-ink hover:text-ink-inverse disabled:opacity-60"
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
        <aside className="sticky top-[148px] hidden w-[320px] shrink-0 lg:block xl:top-[84px] xl:w-[352px]">
          <ShowcasePanel
              card={open}
              position={{ index: openIndex, total: cards.length, more: cursor !== null }}
              direction={direction}
              onPrev={openIndex > 0 ? () => step(-1) : null}
              onNext={openIndex < cards.length - 1 ? () => step(1) : null}
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

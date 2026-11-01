"use client";

import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { IconFeedback } from "@/components/icons";
import { plural, type PluralTemplates } from "@/lib/plural";
import { fetchReceived, markFeedbackSeen, receivedKey, unreadKey } from "@/services/feedback";
import { FeedbackItem } from "./FeedbackItem";
import type { FeedbackLabels } from "./types";

export interface ReceivedFeedbackLabels {
  title: string;
  empty: string;
  fresh: string;
  /** Contains {count}. */
  unread: PluralTemplates;
}

interface ReceivedFeedbackProps {
  locale: string;
  labels: ReceivedFeedbackLabels;
  item: FeedbackLabels;
}

// What visitors said about the author's portfolio, in Me. Opening it reads it: what came since
// the last visit is marked new on screen, then the Me tab's badge clears.
export function ReceivedFeedback({ locale, labels, item }: ReceivedFeedbackProps) {
  const queryClient = useQueryClient();
  const received = useInfiniteQuery({
    queryKey: receivedKey,
    queryFn: async ({ pageParam }) => {
      const result = await fetchReceived(pageParam);
      if (!result.data) throw new Error(item.loadFailed);
      return result.data;
    },
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
  });
  const seen = useMutation({
    mutationFn: markFeedbackSeen,
    onSuccess: () => queryClient.setQueryData(unreadKey, 0),
  });
  const marked = useRef(false);

  // Marking it read only clears the badge: the list keeps its first answer, so the "new"
  // marks stay on screen for this visit.
  const first = received.data?.pages[0];

  useEffect(() => {
    if (marked.current || !first || first.unread === 0) return;
    marked.current = true;
    seen.mutate();
  }, [first, seen]);

  const items = received.data?.pages.flatMap((page) => page.items) ?? [];
  const seenAt = first?.seenAt ?? null;
  // Compared as dates: ISO strings with and without fractions of a second do not sort as text.
  const isFresh = (createdAt: string) => first !== undefined && (seenAt === null || Date.parse(createdAt) > Date.parse(seenAt));

  return (
    <section id="me-feedback" aria-labelledby="me-feedback-title" className="flex scroll-mt-20 flex-col gap-3 rounded-panel border border-line bg-surface p-4 sm:p-5">
      <h3 id="me-feedback-title" className="flex flex-wrap items-center gap-2 text-[17px] font-semibold">
        <IconFeedback className="size-5 text-accent-ink" />
        {labels.title}
        {first && first.total > 0 ? <span className="rounded-full bg-surface-sunken px-2 py-0.5 font-mono text-xs text-ink-body tabular-nums">{first.total}</span> : null}
        {first && first.unread > 0 ? <span className="text-[13px] font-semibold text-like">{plural(labels.unread, first.unread, locale)}</span> : null}
      </h3>

      {received.isPending ? (
        <div aria-hidden="true" className="h-20 animate-pulse rounded-control bg-surface-sunken" />
      ) : received.isError && items.length === 0 ? (
        <p role="alert" className="text-sm text-ink-body">{item.loadFailed}</p>
      ) : items.length === 0 ? (
        <p className="text-sm leading-normal text-ink-muted">{labels.empty}</p>
      ) : (
        <ul className="flex flex-col divide-y divide-line">
          {items.map((feedback) => (
            <FeedbackItem
              key={feedback.id}
              item={feedback}
              locale={locale}
              labels={item}
              onRemoved={() => queryClient.invalidateQueries({ queryKey: receivedKey })}
              fresh={isFresh(feedback.createdAt) ? labels.fresh : undefined}
            />
          ))}
        </ul>
      )}

      {received.hasNextPage ? (
        <button
          type="button"
          onClick={() => received.fetchNextPage()}
          disabled={received.isFetchingNextPage}
          aria-busy={received.isFetchingNextPage}
          className="press h-11 rounded-control border border-line bg-surface px-4 text-sm font-semibold text-ink disabled:opacity-50"
        >
          {item.more}
        </button>
      ) : null}
    </section>
  );
}

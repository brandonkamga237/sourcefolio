"use client";

import { useInfiniteQuery, useQuery, useQueryClient } from "@tanstack/react-query";
import { IconFeedback } from "@/components/icons";
import { feedbackKey, fetchFeedback } from "@/services/feedback";
import { fetchLikes, likesKey } from "@/services/likes";
import { FeedbackForm } from "./FeedbackForm";
import { FeedbackItem } from "./FeedbackItem";
import type { FeedbackLabels } from "./labels";

interface FeedbackSectionProps {
  slug: string;
  locale: string;
  labels: FeedbackLabels;
}

// Read in the browser: the page around it is cached for every visitor, while "mine" and the
// form depend on who reads. Whether the reader is the author comes from the like state the
// page already asks for, so no extra request.
export function FeedbackSection({ slug, locale, labels }: FeedbackSectionProps) {
  const queryClient = useQueryClient();
  const { data: likes } = useQuery({
    queryKey: likesKey(slug),
    queryFn: async () => (await fetchLikes(slug)).data ?? { count: 0, liked: false, own: false },
  });
  const feedback = useInfiniteQuery({
    queryKey: feedbackKey(slug),
    queryFn: async ({ pageParam }) => {
      const result = await fetchFeedback(slug, pageParam);
      if (!result.data) throw new Error(labels.loadFailed);
      return result.data;
    },
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
  });

  const refresh = () => queryClient.invalidateQueries({ queryKey: feedbackKey(slug) });
  const items = feedback.data?.pages.flatMap((page) => page.items) ?? [];
  const total = feedback.data?.pages[0]?.total ?? null;

  return (
    <section id="feedback" aria-labelledby="feedback-title" className="flex scroll-mt-20 flex-col gap-4">
      <header className="flex flex-col gap-1">
        <h2 id="feedback-title" className="flex items-center gap-2 text-lg font-semibold">
          <IconFeedback className="size-5 text-accent-ink" />
          {labels.title}
          {total ? <span className="rounded-full bg-surface-sunken px-2 py-0.5 font-mono text-xs text-ink-body tabular-nums">{total}</span> : null}
        </h2>
        {likes && !likes.own ? <p className="text-sm leading-normal text-ink-body">{labels.lead}</p> : null}
      </header>

      {likes ? (
        likes.own ? (
          <p className="rounded-card border border-dashed border-line p-3.5 text-sm text-ink-body">{labels.own}</p>
        ) : (
          <FeedbackForm slug={slug} locale={locale} labels={labels} onPosted={refresh} />
        )
      ) : null}

      {feedback.isPending ? (
        <div aria-hidden="true" className="flex flex-col gap-3">
          {[0, 1].map((row) => (
            <div key={row} className="flex gap-3">
              <div className="size-9 shrink-0 animate-pulse rounded-full bg-surface-sunken" />
              <div className="h-16 flex-1 animate-pulse rounded-control bg-surface-sunken" />
            </div>
          ))}
        </div>
      ) : feedback.isError && items.length === 0 ? (
        <p role="alert" className="text-sm text-ink-body">{labels.loadFailed}</p>
      ) : items.length === 0 ? (
        likes ? <p className="text-sm text-ink-muted">{likes.own ? labels.emptyOwn : labels.empty}</p> : null
      ) : (
        <ul className="flex flex-col divide-y divide-line border-t border-line">
          {items.map((item) => (
            <FeedbackItem key={item.id} item={item} locale={locale} labels={labels} onRemoved={refresh} />
          ))}
        </ul>
      )}

      {feedback.hasNextPage ? (
        <button
          type="button"
          onClick={() => feedback.fetchNextPage()}
          disabled={feedback.isFetchingNextPage}
          aria-busy={feedback.isFetchingNextPage}
          className="press h-11 rounded-control border border-line bg-surface px-4 text-sm font-semibold text-ink disabled:opacity-50"
        >
          {labels.more}
        </button>
      ) : null}
    </section>
  );
}

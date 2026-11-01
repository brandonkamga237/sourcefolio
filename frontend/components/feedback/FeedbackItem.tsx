"use client";

import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Avatar } from "@/components/members/Avatar";
import { sinceLabel } from "@/lib/relative-time";
import { deleteFeedback, type Feedback } from "@/services/feedback";
import type { FeedbackLabels } from "./labels";

interface FeedbackItemProps {
  item: Feedback;
  locale: string;
  labels: FeedbackLabels;
  onRemoved: () => void;
}

// Shown as written: member content is never translated in V1, so the language it was written
// in is named when it differs from the page's.
export function FeedbackItem({ item, locale, labels, onRemoved }: FeedbackItemProps) {
  const [confirming, setConfirming] = useState(false);
  const removal = useMutation({
    mutationFn: async () => {
      const result = await deleteFeedback(item.id);
      if (result.status !== 204) throw new Error(result.message ?? labels.removeFailed);
    },
    onSuccess: onRemoved,
  });

  return (
    <li className="flex gap-3 py-4">
      <Avatar login={item.authorLogin} size={36} className="mt-0.5" />
      <div className="flex min-w-0 flex-1 flex-col gap-1.5">
        <p className="flex flex-wrap items-center gap-x-2 gap-y-1 text-[13px]">
          <span className="font-semibold break-all text-ink">{item.authorLogin}</span>
          {item.mine ? <span className="text-ink-muted">· {labels.you}</span> : null}
          <time dateTime={item.createdAt} className="text-ink-muted">
            {sinceLabel(item.createdAt, locale)}
          </time>
          {item.angle ? (
            <span className="rounded-full bg-accent-soft px-2 py-0.5 text-xs font-semibold text-accent-ink">{labels.angles[item.angle]}</span>
          ) : null}
        </p>
        <p lang={item.language} className="text-[15px] leading-normal break-words whitespace-pre-line text-ink-body">
          {item.body}
        </p>
        {item.language !== locale && labels.writtenIn[item.language] ? (
          <p className="text-xs text-ink-muted">{labels.writtenIn[item.language]}</p>
        ) : null}
        {item.mine ? (
          <div className="-ml-3 flex flex-wrap items-center gap-1">
            {confirming ? (
              <>
                <button
                  type="button"
                  onClick={() => removal.mutate()}
                  disabled={removal.isPending}
                  className="press h-11 rounded-control px-3 text-[13px] font-semibold text-danger disabled:opacity-50"
                >
                  {labels.confirmRemove}
                </button>
                <button type="button" onClick={() => setConfirming(false)} className="press h-11 rounded-control px-3 text-[13px] font-semibold text-ink-body">
                  {labels.cancel}
                </button>
              </>
            ) : (
              <button type="button" onClick={() => setConfirming(true)} className="press h-11 rounded-control px-3 text-[13px] font-semibold text-ink-muted hover:text-ink">
                {labels.remove}
              </button>
            )}
            {removal.isError ? (
              <p role="alert" className="text-xs text-danger">
                {removal.error.message}
              </p>
            ) : null}
          </div>
        ) : null}
      </div>
    </li>
  );
}

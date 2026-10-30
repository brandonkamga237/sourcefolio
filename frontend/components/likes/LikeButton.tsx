"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { IconHeart } from "@/components/icons";
import { fetchLikes, likesKey, setLiked, type LikeState } from "@/services/likes";

export interface LikeButtonLabels {
  /** Contains {count}. */
  like: string;
  /** Contains {count}. */
  unlike: string;
  /** Contains {count}. Read on the author's own portfolio, where the button is disabled. */
  own: string;
  failed: string;
}

interface LikeButtonProps {
  slug: string;
  labels: LikeButtonLabels;
  /** Shows the count next to the heart. */
  showCount?: boolean;
}

const AUTO_LIKE = "like";

// Optimistic: the heart fills at once and rolls back if the API refuses. A visitor is sent to
// GitHub sign-in and brought back here with ?like=1, where the like is applied once (spec:
// the action runs automatically on return).
export function LikeButton({ slug, labels, showCount }: LikeButtonProps) {
  const queryClient = useQueryClient();
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const autoLiked = useRef(false);

  const { data } = useQuery({
    queryKey: likesKey(slug),
    queryFn: async () => (await fetchLikes(slug)).data ?? { count: 0, liked: false, own: false },
  });

  const mutation = useMutation({
    mutationFn: async (liked: boolean) => {
      const result = await setLiked(slug, liked);
      if (result.status === 401) {
        // A full navigation on purpose: sign-in is a backend route, not a Next.js page.
        // eslint-disable-next-line @next/next/no-location-assign-relative-destination
        window.location.assign(`/oauth2/authorization/github?returnTo=${encodeURIComponent(`${pathname}?${AUTO_LIKE}=1`)}`);
        throw new Error("signing in");
      }
      if (!result.data) throw new Error(result.message ?? labels.failed);
      return result.data;
    },
    onMutate: async (liked) => {
      setError(null);
      await queryClient.cancelQueries({ queryKey: likesKey(slug) });
      const previous = queryClient.getQueryData<LikeState>(likesKey(slug));
      if (previous) {
        queryClient.setQueryData<LikeState>(likesKey(slug), { ...previous, count: previous.count + (liked ? 1 : -1), liked });
      }
      return { previous };
    },
    onError: (failure, _liked, context) => {
      if (context?.previous) queryClient.setQueryData(likesKey(slug), context.previous);
      if (failure.message !== "signing in") setError(failure.message);
    },
    onSuccess: (state) => queryClient.setQueryData(likesKey(slug), state),
  });

  useEffect(() => {
    if (autoLiked.current || searchParams.get(AUTO_LIKE) !== "1" || !data) return;
    autoLiked.current = true;
    router.replace(pathname, { scroll: false });
    if (!data.liked && !data.own) mutation.mutate(true);
  }, [data, mutation, pathname, router, searchParams]);

  const state = data ?? { count: 0, liked: false, own: false };
  const label = (state.own ? labels.own : state.liked ? labels.unlike : labels.like).replace("{count}", String(state.count));

  return (
    <div className="flex flex-col gap-1">
      <button
        type="button"
        onClick={() => mutation.mutate(!state.liked)}
        disabled={state.own}
        aria-pressed={state.own ? undefined : state.liked}
        aria-label={label}
        title={label}
        className={`flex h-[46px] min-w-[46px] items-center justify-center gap-1.5 rounded-control border px-3 text-sm font-semibold transition-[transform,background-color,border-color] duration-200 active:scale-95 disabled:cursor-not-allowed disabled:border-dashed disabled:text-ink-muted disabled:active:scale-100 ${
          state.liked ? "border-accent bg-accent-soft text-accent-ink" : "border-line bg-surface text-ink"
        }`}
      >
        {/* Pops when the reader fills it, not when a page opens on a heart already filled. */}
        <IconHeart
          key={String(state.liked)}
          className={`size-5 ${state.liked ? "fill-current" : ""} ${state.liked && mutation.variables === true ? "animate-pop" : ""}`}
        />
        {showCount ? <span className="font-mono tabular-nums">{state.count}</span> : null}
      </button>
      {error ? (
        <p role="alert" className="text-xs text-ink-body">
          {error}
        </p>
      ) : null}
    </div>
  );
}

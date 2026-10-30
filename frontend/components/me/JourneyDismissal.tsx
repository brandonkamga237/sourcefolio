"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { IconClose } from "@/components/icons";
import { JOURNEY_COOKIE } from "@/lib/journey";

// Read by the server, so a dismissed card is not drawn at all rather than drawn then hidden.
function setDismissed(dismissed: boolean) {
  document.cookie = dismissed
    ? `${JOURNEY_COOKIE}=dismissed; path=/; max-age=31536000; samesite=lax`
    : `${JOURNEY_COOKIE}=; path=/; max-age=0; samesite=lax`;
}

interface DismissJourneyProps {
  label: string;
}

/** Hides the "all set" card; it comes back by itself if a step needs doing again. */
export function DismissJourney({ label }: DismissJourneyProps) {
  const router = useRouter();
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      onClick={() => {
        setDismissed(true);
        router.refresh();
      }}
      className="press flex size-11 shrink-0 items-center justify-center rounded-full text-ink-muted transition-colors duration-150 hover:bg-surface-sunken hover:text-ink"
    >
      <IconClose className="size-[18px]" />
    </button>
  );
}

/** A step needs doing again: the next time everything is done, the card shows once more. */
export function ForgetJourneyDismissal() {
  useEffect(() => setDismissed(false), []);
  return null;
}

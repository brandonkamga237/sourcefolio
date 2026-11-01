import type { PluralTemplates } from "@/lib/plural";
import type { FeedbackAngle } from "@/services/feedback";

// Raw strings, filled in the browser: client components carry no ICU parser.
export interface FeedbackLabels {
  title: string;
  lead: string;
  angleLabel: string;
  angles: Record<FeedbackAngle, string>;
  bodyLabel: string;
  placeholder: string;
  /** Contains {count} and {max}. */
  counter: string;
  /** Contains {count}. */
  missing: PluralTemplates;
  submit: string;
  sending: string;
  signInHint: string;
  posted: string;
  failed: string;
  own: string;
  empty: string;
  emptyOwn: string;
  loadFailed: string;
  more: string;
  you: string;
  /** Under a minute old: "this minute" reads oddly in French. */
  justNow: string;
  remove: string;
  confirmRemove: string;
  cancel: string;
  removeFailed: string;
  writtenIn: Record<string, string>;
}

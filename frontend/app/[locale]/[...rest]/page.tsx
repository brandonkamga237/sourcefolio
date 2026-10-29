import { notFound } from "next/navigation";

// Any path under a locale that no route claims renders the localised not-found page, inside
// the app shell, instead of Next.js's bare English 404.
export default function CatchAll() {
  notFound();
}

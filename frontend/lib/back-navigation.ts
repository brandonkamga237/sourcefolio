// Next.js moves between pages with history.pushState; a Back or Forward fires popstate just
// before the previous page renders again. Remembering that moment lets a page tell "the
// visitor came back" from "the visitor arrived", without guessing from timing.
let cameBack = false;

if (typeof window !== "undefined") {
  window.addEventListener("popstate", () => {
    cameBack = true;
  });
}

/** True once after a Back or Forward; reading it resets it. */
export function consumeBackNavigation(): boolean {
  const value = cameBack;
  cameBack = false;
  return value;
}

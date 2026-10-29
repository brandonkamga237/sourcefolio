---
name: frontend-page
description: Create or change a page, route or component in the SourceFolio frontend (Next.js App Router, React, TypeScript, Tailwind, Radix, TanStack Query, next-intl), following the routing, services layer, i18n, performance and project conventions. Use for any new screen, route, sheet or reusable component.
---

# Adding a page

Load the `design-system` skill for every visual decision.

## Where things go

- Route → `app/[locale]/<segment>/page.tsx` (Server Component), with its `loading.tsx`
  (skeleton) and, if it has its own data errors, `error.tsx`
- The four tabs: Discover (`[locale]/page.tsx`), Search (`[locale]/search`), My portfolio
  (`[locale]/me/portfolio`), Me (`[locale]/me`); a showcase page lives at a stable, shareable
  URL (`[locale]/p/[slug]`), and on desktop it also opens as a side panel over the catalogue
  (parallel + intercepting route) without losing the grid
- Shell (sidebar on `lg`, bottom tabs below) → `app/[locale]/layout.tsx` +
  `components/layout/`
- Reusable component → `components/<area>/` (`catalog`, `showcase`, `feedback`,
  `onboarding`, `dashboard`, `layout`…)
- Radix primitive wrapped with our tokens → `components/ui/` (`Sheet`, `Dialog`, `Menu`,
  `Tabs`, `ToggleGroup`)
- Icon → `icons/<name>.svg`, generated into `components/icons/` (never edited)
- Data → `services/<area>.ts`; hook → `hooks/useXxx.ts`; pure helper → `lib/`
- Strings → `messages/fr.json` and `messages/en.json`, same keys in both, grouped by screen

## Non-negotiable conventions

- **Locale in every URL**: `/fr/...` and `/en/...`, routing defined once in `i18n/routing.ts`;
  links go through next-intl's `Link`, never a bare `<a href="/...">` for internal pages. The
  default locale comes from `Accept-Language`, then English; a member's choice is saved on the
  account.
- **No literal text in JSX**: `const t = useTranslations('catalog')` (client) or
  `await getTranslations('catalog')` (server). Both files get the key in the same commit; a
  missing key fails the build check.
- **Import through `@/`** — never `../../..`.
- **No `fetch` in a component.** Server Components call `services/` functions directly;
  Client Components use them through TanStack Query (`useQuery`, `useInfiniteQuery` for the
  feed, `useMutation` with optimistic updates for like). Query keys are exported next to the
  function.
- **Generated types only**: response and request types come from `@/lib/api/schema`.
- **Never hardcode an API URL**: `lib/api/http.ts` knows `API_INTERNAL_URL` (server) and
  the same-origin `/api` (browser).
- **Tailwind with tokens only**, no inline style unless computed at runtime.
- **Typed props**: `interface XxxProps { ... }`.
- **Forms**: React Hook Form + Zod schema whose messages are translation keys. The onboarding
  is one question per screen, saved on each answer, never asking an answered question again.
- Actions that need a session (like, comment) from a visitor start GitHub sign-in with a
  `returnTo` and replay the action on return.
- External portfolio links: `target="_blank" rel="noopener"`, and the click is reported to the
  stats endpoint (`navigator.sendBeacon`). Never an iframe.
- Share: `navigator.share` when available, else copy the link; the shared URL carries the
  locale so the OG image is in the right language.

## Mobile-grade behaviour

- Scroll position of the catalogue is restored when coming back from a showcase (keep the
  infinite query in the cache, restore with the router's scroll memory or a saved offset).
- Skeletons for every loading state; never a full-screen spinner.
- Bottom sheets (filters, comments) are Radix `Dialog` styled as sheets, with the input above
  the keyboard (`visualViewport`), closable by swipe.
- Touch targets ≥ 44 px, `prefers-reduced-motion` honoured, dark theme from the system.
- Screenshots: `next/image` with `sizes`, lazy below the fold, priority for the first two
  cards; with `navigator.connection?.saveData` they load on tap.

## SEO on a public page

Every public route exports `generateMetadata` (title, description, canonical, `alternates.
languages` for hreflang, Open Graph) — see the `seo-page` skill for the full list, the
sitemap and the OG image.

## Verify
```
cd frontend && pnpm typecheck && pnpm lint && pnpm build
```
`pnpm build` prints the first-load JS per route: stay under 150 KB compressed for the catalogue
and the showcase page. `pnpm` only, never `npm`.

---
name: design-system
description: The SourceFolio visual language taken from the V1 mockups — colour, radius, spacing and type tokens for Tailwind, the component anatomy (card, chip, panel, action bar, sidebar, bottom tabs), and the hand-drawn icon and logo set to keep pixel-identical through SVGR. Use before building or restyling any screen or component, when adding an icon, or when a colour, radius or font size has to be chosen.
---

# Design system

The reference is the mockup "Écrans V1 – Desktop · Découvrir" (desktop, 1440 px). The mobile
screens follow the same tokens with the bottom tab bar instead of the sidebar. When a value is
not here, take it from the mockup, never invent a near neighbour.

## Tokens (Tailwind v4, `app/globals.css`)

```css
@theme {
  /* Surfaces */
  --color-canvas: #F5F5F1;        /* page background */
  --color-surface: #FFFFFF;       /* cards, panels, inputs */
  --color-surface-raised: #FBFBF8;/* sidebar */
  --color-surface-sunken: #F0F0EB;/* segmented control track */

  /* Ink */
  --color-ink: #121412;           /* headings, primary button background */
  --color-ink-body: #2E312C;      /* paragraphs */
  --color-ink-soft: #3C3F3A;      /* nav items, tag text */
  --color-ink-muted: #5B5F58;     /* meta, placeholders, counts */

  /* Lines */
  --color-line: #E2E2DC;          /* card and input borders */
  --color-line-soft: #ECECE6;     /* separators inside a panel */

  /* Accent */
  --color-accent: #2B45E0;        /* main action ("Voir le site"), selected card border, links */
  --color-accent-hover: #1C2FA8;
  --color-accent-ink: #1F33B3;    /* text on accent-soft */
  --color-accent-soft: #E9ECFD;   /* active nav item, active filter chip */
  --color-accent-line: #C9D0FA;   /* active filter chip border */

  /* Status */
  --color-success: #1D7A4B;       /* "En ligne" dot */
  --color-success-ink: #155C39;

  /* Radius */
  --radius-tag: 7px;              /* technology tags */
  --radius-segment: 9px;          /* segmented control buttons */
  --radius-control: 12px;         /* buttons, inputs, nav items */
  --radius-card: 18px;            /* showcase cards */
  --radius-panel: 22px;           /* detail side panel, sheets */
  /* chips and pills: rounded-full */

  /* Type */
  --font-display: var(--font-bricolage), sans-serif;
  --font-sans: var(--font-geist), system-ui, sans-serif;
  --font-mono: var(--font-geist-mono), monospace;
}
```

Fonts are loaded with `next/font/google` (`Bricolage_Grotesque` 600/700, `Geist` 400/500/600,
`Geist_Mono` 400/500) as CSS variables on `<html>`. Never a `<link>` to Google Fonts.

Dark mode is not drawn yet. `app/globals.css` redefines the same token names for
`prefers-color-scheme: dark` (unless `<html data-theme="light">`) and for
`<html data-theme="dark">`, the member's choice in "Moi". Its values are a proposal to validate
before the first release. Components only ever use the token names, so the theme is one block
of CSS.

## Type scale

| Use | Classes |
| --- | --- |
| Page title (h1) | `font-display font-bold text-[40px] leading-none tracking-[-0.035em]` (28 px on mobile) |
| Panel title (h2) | `font-display font-bold text-2xl tracking-[-0.025em]` |
| Wordmark | `font-display font-bold text-[21px] tracking-[-0.02em]` |
| Card title, nav item | `text-[15px] font-semibold` / `font-medium` |
| Body | `text-sm leading-normal text-ink-body` |
| Meta | `text-[13px] text-ink-muted` |
| Counts, handles, tags, language switch | `font-mono text-[13px]` / `text-xs` |

## Spacing and sizes

- Controls are 44–46 px high (touch target); icon buttons are 40–46 px squares.
- Gaps: 4 (nav list), 6–8 (tags, actions), 10 (filter row), 16 (card grid), 18–28 (sections).
- Desktop: sidebar 220 px, content padding `32px 32px 48px 40px`, card grid
  `grid-cols-[repeat(auto-fill,minmax(250px,1fr))] gap-4`, detail panel ≥ 360 px beside the grid.
- Breakpoint for the desktop layout (sidebar + panel): `lg` (1024 px). Below it: bottom tab bar
  in the thumb zone, the detail is a full page, filters open in a bottom sheet.

## Components (anatomy from the mockup)

- **Showcase card** — `bg-surface border border-line rounded-card overflow-hidden`; preview at
  `aspect-[16/10]` (desktop screenshot on desktop, mobile screenshot on phones); below it
  `p-3.5` with the name (15 px semibold) and "Spécialité · Pays" (13 px muted), flag and at most
  three technologies. Selected: `border-2 border-accent`. The like count shows on the showcase page and in the Ranking, not on Discover cards.
- **Filter chip** — `h-11 px-3.5 rounded-full border border-line bg-surface text-sm font-medium`;
  active: `bg-accent-soft border-accent-line text-accent-ink` with a close icon; "Filtres" chip:
  `bg-ink text-white` with the filters icon. "Filtres" opens a sheet with the live count
  ("Voir 124 portfolios").
- **Search field** — `h-[46px] rounded-control border border-line bg-surface px-3.5`, search
  icon 18 px muted, visually hidden label.
- **Detail panel** — `rounded-panel border border-line bg-surface`; screenshot on top with a
  close button (40 px, white 94 %) and the status pill ("En ligne · vérifié il y a 3 h", green
  dot); avatar 46 px initials; actions row: primary "Voir le site" (`bg-accent text-white
  rounded-control h-[46px]`, external icon), secondary "Code" (`border border-ink`), like icon
  button (`aria-pressed`). On mobile this row is the fixed bottom action bar.
- **Tags (detected stack)** — `font-mono text-xs text-ink-soft border border-line px-2 py-1
  rounded-tag`.
- **Sidebar nav item** — `h-11 px-3 rounded-control gap-3`, icon 20 px; current:
  `bg-accent-soft text-accent-ink font-semibold`, `aria-current="page"`.
- **Language switch** — segmented control on `bg-surface-sunken p-1 rounded-control`, active
  segment white with `shadow-[0_1px_2px_rgba(18,20,18,0.12)]`, `aria-pressed`.
- **Primary CTA** ("Publier ton portfolio") — `h-11 rounded-control bg-ink text-white`.

Every interactive element keeps a visible focus ring (`focus-visible:outline-2
focus-visible:outline-accent`).

## Icons and logo

All icons are 24×24, `fill="none" stroke="currentColor" stroke-linecap="round"
stroke-linejoin="round"`, drawn for this product. They live as files in
`frontend/icons/<name>.svg` and `pnpm icons` turns them into components through SVGR
(`components/icons/<Name>.tsx`, exported as `Icon<Name>` from `@/components/icons`; generated,
never edited by hand, ignored by ESLint). Stroke width is part
of the file: keep the mockup's.

| File | Stroke | Content |
| --- | --- | --- |
| `logo-mark.svg` | 2.4 | `<path d="M8 8l-4 4 4 4M16 8l4 4-4 4"/>` on a 26 px `bg-ink rounded-[7px]` square, white |
| `discover.svg` | 1.75 (1.9 when active) | `<circle cx="12" cy="12" r="9"/><path d="M15.5 8.5l-2 5-5 2 2-5z"/>` |
| `search.svg` | 1.75 | `<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>` |
| `portfolio.svg` | 1.75 | `<rect x="3" y="4" width="18" height="16" rx="2.5"/><path d="M3 9h18M8 14h5"/>` |
| `me.svg` | 1.75 | `<circle cx="12" cy="8" r="4"/><path d="M4 20c1.5-4 4.5-6 8-6s6.5 2 8 6"/>` |
| `filters.svg` | 2 | `<path d="M4 7h10M18 7h2M4 17h4M12 17h8"/><circle cx="16" cy="7" r="2"/><circle cx="10" cy="17" r="2"/>` |
| `close.svg` | 2 | `<path d="M6 6l12 12M18 6L6 18"/>` |
| `external.svg` | 2 | `<path d="M7 17L17 7M9 7h8v8"/>` |
| `code.svg` | 2 | same path as `logo-mark.svg` |
| `heart.svg` | 1.8 | `<path d="M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.5-7 10-7 10z"/>` |

The wordmark is the text "sourcefolio" in the wordmark style next to the mark, not an image.
A new icon is drawn in the same grid and stroke family, added as a file, then generated; never
pulled from Lucide, Heroicons or any pack. Decorative icons get `aria-hidden="true"`; an
icon-only button gets an `aria-label` from the messages files.

## Motion

Use the `emil-design-eng`, `animate` and `mobile-native` skills. Required motions from the
spec: shared transition card → detail (the screenshot grows), sheets from the bottom, pull to
refresh, swipe back. All of them respect `prefers-reduced-motion`.

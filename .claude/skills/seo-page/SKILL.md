---
name: seo-page
description: Make a SourceFolio public page indexable and shareable — server-rendered metadata, hreflang for /fr and /en, canonical URL, JSON-LD, sitemap per language, and the Open Graph image generated for WhatsApp, X and LinkedIn in the URL's language. Use whenever a public page or a new kind of shareable content is added.
---

# SEO and share previews

Pages are rendered on the server by Next.js, so crawlers receive real HTML: there is no bot
snapshot to maintain. A public page is indexable and shareable once the four points below are
done. Missing one leaves it invisible or with a blank WhatsApp preview, with no visible error.

## 1. Metadata — `generateMetadata` in the route

```ts
export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { locale, slug } = await params;
  const t = await getTranslations({ locale, namespace: 'showcase.meta' });
  const showcase = await getShowcase(slug);
  return {
    title: t('title', { name: showcase.displayName }),
    description: t('description', { specialty: showcase.specialty, country: showcase.country }),
    alternates: {
      canonical: `/${locale}/p/${slug}`,
      languages: { fr: `/fr/p/${slug}`, en: `/en/p/${slug}`, 'x-default': `/en/p/${slug}` },
    },
    openGraph: { type: 'profile', locale: locale === 'fr' ? 'fr_FR' : 'en_US' },
  };
}
```

- `metadataBase` is set once in the root layout from `SITE_URL`.
- Title and description come from the messages files, in the page's language. Member content
  (bio) is used as written, never translated.
- A showcase that is not `PUBLISHED` returns `robots: { index: false }` and its page shows the
  banner of the spec; a `REMOVED` one is a 404 for visitors.

## 2. JSON-LD

Render one `<script type="application/ld+json">` from the server component: `ProfilePage`
with a `Person` (name, `sameAs`: the site and the GitHub profile) for a showcase page,
`CollectionPage` for the catalogue and country pages, `WebSite` with a `SearchAction` on the
home. Escape `<` in the JSON.

## 3. Open Graph image — `opengraph-image.tsx` next to the page

`next/og` `ImageResponse`, 1200×630, generated per locale: the screenshot, name, specialty,
flag and the wordmark, using the design-system tokens and the Bricolage/Geist fonts loaded as
`ArrayBuffer`. Keep it under 300 KB (WhatsApp drops larger previews) and cache it with the
showcase's last check time in the URL so a new screenshot refreshes the preview.

## 4. Sitemap — `app/sitemap.ts`

One entry per public URL and per language, with `alternates.languages`; showcases come from a
paged backend endpoint that returns only `PUBLISHED` slugs and their `lastModified`. Add a
static page by hand. `robots.ts` points to it and disallows `/fr/me`, `/en/me` and `/api`.

## Verify

```
curl -s http://localhost:3000/fr/p/<slug> | grep -E '<title>|hreflang|og:image|ld\+json'
curl -sI http://localhost:3000/fr/p/<slug>/opengraph-image | grep -i content-length
```
In production, paste the URL in a WhatsApp chat to yourself, and check it in the LinkedIn Post
Inspector.

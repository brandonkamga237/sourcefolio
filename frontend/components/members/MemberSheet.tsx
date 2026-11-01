"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { useQuery } from "@tanstack/react-query";
import Image from "next/image";
import Link from "next/link";
import { useState, type ReactNode } from "react";
import { PreviewArt } from "@/components/catalog/PreviewArt";
import { IconClose, IconCode, IconExternal, IconGithub, IconHeart, IconStar } from "@/components/icons";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryName, flagOf } from "@/lib/countries";
import { compactNumber, plural, type PluralTemplates } from "@/lib/plural";
import { siteLabel } from "@/lib/site-label";
import { fetchMember, memberKey } from "@/services/members";
import { Avatar } from "./Avatar";

export interface MemberSheetLabels {
  /** Contains {login}: the trigger's accessible name. */
  open: string;
  close: string;
  loading: string;
  failed: string;
  /** Contains {date}. */
  since: string;
  likesReceived: PluralTemplates;
  stars: PluralTemplates;
  specialtiesTitle: string;
  noSpecialty: string;
  noCountry: string;
  portfolioTitle: string;
  openPage: string;
  visitSite: string;
  viewCode: string;
  githubProfile: string;
  specialtyNames: Record<string, string>;
  /** Contains {role}: worded as the member's own claim. */
  roleClaim: string;
  roleNames: Record<string, string>;
}

interface MemberSheetProps {
  login: string;
  locale: Locale;
  labels: MemberSheetLabels;
  /** What the visitor taps: the author's name and initials on a card, panel or page. */
  children: ReactNode;
  triggerClassName?: string;
}

// The open community: tapping an author shows who they are, from what the catalogue knows,
// without leaving the feed. A sheet from the bottom on a phone, a centred card on desktop.
export function MemberSheet({ login, locale, labels, children, triggerClassName = "" }: MemberSheetProps) {
  const [open, setOpen] = useState(false);
  const { data, isPending, isError } = useQuery({
    queryKey: memberKey(login),
    queryFn: async () => {
      const result = await fetchMember(login);
      if (!result.data) throw new Error(result.message ?? labels.failed);
      return result.data;
    },
    enabled: open,
    staleTime: 60_000,
  });

  return (
    <Dialog.Root open={open} onOpenChange={setOpen}>
      <Dialog.Trigger className={triggerClassName} aria-label={labels.open.replace("{login}", login)}>
        {children}
      </Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-ink/40 backdrop-blur-[2px] data-[state=open]:animate-[fade-in_200ms_ease-out]" />
        <Dialog.Content className="fixed inset-x-0 bottom-0 z-50 flex max-h-[92dvh] flex-col overflow-hidden rounded-t-panel border border-line bg-surface shadow-[0_-12px_40px_-12px_rgba(18,20,18,0.3)] data-[state=open]:animate-[sheet-up_320ms_var(--ease-out-soft)] sm:inset-x-auto sm:top-1/2 sm:bottom-auto sm:left-1/2 sm:max-h-[88dvh] sm:w-[min(760px,calc(100vw-48px))] sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-panel sm:data-[state=open]:animate-[pop-in_240ms_var(--ease-out-soft)]">
          <div className="relative h-24 shrink-0 bg-[linear-gradient(120deg,var(--color-accent-soft),var(--color-surface-sunken))]">
            <span aria-hidden="true" className="absolute top-2 left-1/2 h-1 w-10 -translate-x-1/2 rounded-full bg-ink/15 sm:hidden" />
            <Dialog.Close
              aria-label={labels.close}
              className="press absolute top-3 right-3 flex size-11 items-center justify-center rounded-full bg-surface/90 text-ink transition-colors duration-150 hover:bg-surface"
            >
              <IconClose className="size-[18px]" />
            </Dialog.Close>
          </div>

          {/* One column on a phone; on a wide screen the person on the left and their portfolio on
              the right, so everything shows at once without scrolling the sheet. */}
          <div className={`relative -mt-12 grid gap-4 overflow-y-auto px-5 pb-[calc(1.25rem+env(safe-area-inset-bottom))] sm:grid-cols-2 sm:gap-x-6 sm:pb-6 ${areas}`}>
            <div className="flex items-end gap-3 [grid-area:id]">
              <Avatar login={login} size={88} className="border-4 border-surface" />
              <div className="flex min-w-0 flex-col pb-1">
                <Dialog.Title className="truncate font-display text-[24px] leading-tight font-bold tracking-[-0.025em]">{data?.login ?? login}</Dialog.Title>
                {data?.headline ? <p className="line-clamp-2 text-sm leading-snug font-medium text-ink-body">{data.headline}</p> : null}
                {data?.role ? (
                  <p className="truncate text-[13px] font-semibold text-accent-ink">
                    {labels.roleClaim.replace("{role}", labels.roleNames[data.role] ?? data.role)}
                  </p>
                ) : null}
                <Dialog.Description className="truncate text-[13px] text-ink-muted">
                  {data ? (data.countryCode ? `${flagOf(data.countryCode)} ${countryName(data.countryCode, locale)}` : labels.noCountry) : labels.loading}
                </Dialog.Description>
              </div>
            </div>

            {isPending ? (
              <div aria-hidden="true" className="flex flex-col gap-3 [grid-area:spec] sm:col-span-2">
                <div className="h-8 w-2/3 animate-pulse rounded-full bg-surface-sunken" />
                <div className="h-16 animate-pulse rounded-control bg-surface-sunken" />
                <div className="h-40 animate-pulse rounded-card bg-surface-sunken" />
              </div>
            ) : isError || !data ? (
              <p role="alert" className="rounded-control bg-surface-sunken p-3 text-sm text-ink-body [grid-area:spec] sm:col-span-2">{labels.failed}</p>
            ) : (
              <>
                <section aria-label={labels.specialtiesTitle} className="flex flex-wrap content-start gap-1.5 [grid-area:spec]">
                  {data.specialties.length === 0 ? (
                    <span className="text-[13px] text-ink-muted">{labels.noSpecialty}</span>
                  ) : (
                    data.specialties.map((slug, index) => (
                      <span
                        key={slug}
                        className={`flex h-8 items-center gap-1.5 rounded-full px-3 text-[13px] font-semibold ${
                          index === 0 ? "bg-ink text-ink-inverse" : "border border-line bg-surface text-ink"
                        }`}
                      >
                        <span className="font-mono text-[11px] opacity-60">{index + 1}</span>
                        {labels.specialtyNames[slug] ?? slug}
                      </span>
                    ))
                  )}
                </section>

                <dl className="grid grid-cols-3 divide-x divide-line-soft self-start rounded-control border border-line-soft bg-surface-raised text-center [grid-area:stats]">
                  <div className="flex flex-col gap-0.5 px-2 py-2.5">
                    <dt className="order-2 text-xs text-ink-muted">{plural(labels.likesReceived, data.likesReceived, locale).replace(/^\S+\s/, "")}</dt>
                    <dd className="order-1 flex items-center justify-center gap-1 font-mono text-base font-semibold tabular-nums">
                      <IconHeart aria-hidden="true" className="size-4 fill-like text-like" />
                      {compactNumber(data.likesReceived, locale)}
                    </dd>
                  </div>
                  <div className="flex flex-col gap-0.5 px-2 py-2.5">
                    <dt className="order-2 flex items-center justify-center gap-1 text-xs text-ink-muted">
                      <IconGithub aria-hidden="true" className="size-3" />
                      {plural(labels.stars, data.showcase.repositoryStars ?? 0, locale).replace(/^\S+\s/, "")}
                    </dt>
                    <dd className="order-1 flex items-center justify-center gap-1 font-mono text-base font-semibold tabular-nums">
                      <IconStar aria-hidden="true" className="size-4 fill-star text-star" />
                      {data.showcase.repositoryStars != null ? compactNumber(data.showcase.repositoryStars, locale) : "–"}
                    </dd>
                  </div>
                  <div className="flex flex-col justify-center gap-0.5 px-2 py-2.5">
                    <dd className="text-[13px] leading-tight font-semibold">
                      {labels.since.replace("{date}", new Intl.DateTimeFormat(locale, { month: "short", year: "numeric" }).format(new Date(data.onSourceFolioSince)))}
                    </dd>
                  </div>
                </dl>

                <section aria-label={labels.portfolioTitle} className="self-start overflow-hidden rounded-card border border-line [grid-area:port] sm:mt-14">
                  <div className="relative aspect-[2/1] overflow-hidden bg-surface-sunken">
                    {data.showcase.previewImageUrl ? (
                      <Image src={data.showcase.previewImageUrl} alt="" fill sizes="460px" className="object-cover object-top" />
                    ) : (
                      <PreviewArt slug={data.showcase.slug} siteUrl={data.showcase.siteUrl} size="card" aspectClass="h-full" />
                    )}
                  </div>
                  <div className="flex flex-col gap-3 p-3">
                    <div className="flex min-w-0 flex-col">
                      <span className="truncate font-display text-[18px] font-bold tracking-[-0.02em]">{siteLabel(data.showcase.siteUrl)}</span>
                      <span className="truncate font-mono text-xs text-ink-muted">{data.showcase.repository}</span>
                    </div>
                    <div className="grid grid-cols-[1fr_auto_auto] gap-2">
                      <Link
                        href={localizedPath(locale, `/p/${data.showcase.slug}`)}
                        onClick={() => setOpen(false)}
                        className="press flex h-11 items-center justify-center rounded-control bg-accent px-3 text-sm font-semibold text-white transition-colors duration-150 hover:bg-accent-hover"
                      >
                        {labels.openPage}
                      </Link>
                      <a href={data.showcase.siteUrl} target="_blank" rel="noopener noreferrer" aria-label={labels.visitSite} title={labels.visitSite} className={iconAction}>
                        <IconExternal className="size-4" />
                      </a>
                      <a href={data.showcase.repositoryUrl} target="_blank" rel="noopener noreferrer" aria-label={labels.viewCode} title={labels.viewCode} className={iconAction}>
                        <IconCode className="size-4" />
                      </a>
                    </div>
                  </div>
                </section>

                <a
                  href={data.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="press flex h-11 items-center justify-center gap-2 self-start rounded-control border border-line text-sm font-semibold text-ink transition-colors duration-150 [grid-area:gh] hover:border-ink"
                >
                  <IconGithub className="size-4" />
                  {labels.githubProfile}
                </a>
              </>
            )}
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}

// Named areas keep the phone's reading order (person, specialties, numbers, portfolio, GitHub)
// while the wide layout moves the portfolio to its own column.
const areas =
  "[grid-template-areas:'id'_'spec'_'stats'_'port'_'gh'] sm:[grid-template-areas:'id_port'_'spec_port'_'stats_port'_'gh_port']";

const iconAction = "press flex size-11 items-center justify-center rounded-control border border-line text-ink transition-colors duration-150 hover:border-ink";

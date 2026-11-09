import { connection } from "next/server";
import { getFormatter, getTranslations } from "next-intl/server";
import { Suspense } from "react";
import { GlobeLazy } from "@/components/globe/GlobeLazy";
import { AfricaMosaic } from "@/components/map/AfricaMosaic";
import { JsonLd } from "@/components/seo/JsonLd";
import { Accordion } from "@/components/ui/Accordion";
import { Segmented } from "@/components/ui/Segmented";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import { countryName } from "@/lib/countries";
import { EXAMPLE_CITIES, EXAMPLE_PROFILE, EXAMPLE_PROJECTS, EXAMPLE_ROWS } from "@/lib/examples";
import { normalizeCity, placeCounts } from "@/lib/map";
import type { ProfileCard } from "@/services/contribution-types";
import { getCityCounts } from "@/services/map";
import { getPublicProfile } from "@/services/public-profiles";
import { getAcceptedProjects, getRankings, type RankingRow } from "@/services/rankings";
import { Anatomy } from "./Anatomy";
import { ExampleTag } from "./ExampleTag";
import { HandleField } from "./HandleField";
import { HeroBuild } from "./HeroBuild";
import { HowItWorks } from "./HowItWorks";
import { ProblemSplit } from "./ProblemSplit";
import { ProofBand } from "./ProofBand";
import { RankingPreview, type PreviewRow } from "./RankingPreview";
import { TalentField } from "./TalentField";
import { Transparency } from "./Transparency";
import { WaitlistForm } from "./WaitlistForm";
import { WaitlistLeaveForm } from "./WaitlistLeaveForm";

export type Audience = "tech" | "recruit";

const FAQ = ["what", "private", "score", "beginner", "remove"] as const;
const section = "wrap flex flex-col gap-10 py-16 lg:gap-14 lg:py-28";

async function safely<T>(read: Promise<T>, fallback: T, what: string): Promise<T> {
  try {
    return await read;
  } catch (error) {
    console.error(`The landing page could not read ${what}`, error);
    return fallback;
  }
}

async function load() {
  await connection();
  const [ranking, cities, projects] = await Promise.all([
    safely(getRankings({ sort: "regularity", country: null, language: null, cursor: null }), { items: [], nextCursor: null, total: 0 }, "the rankings"),
    safely(getCityCounts(), [], "the cities"),
    safely(getAcceptedProjects(), [], "the projects"),
  ]);
  const first = ranking.items[0] ? await safely(getPublicProfile(ranking.items[0].login), null, "the first profile") : null;
  return {
    card: (first ?? EXAMPLE_PROFILE) as ProfileCard,
    cardExample: !first,
    rows: ranking.items.length > 0 ? ranking.items : EXAMPLE_ROWS,
    rowsExample: ranking.items.length === 0,
    cities: cities.length > 0 ? cities : EXAMPLE_CITIES,
    citiesExample: cities.length === 0,
    projects: projects.length > 0 ? projects : EXAMPLE_PROJECTS,
    projectsExample: projects.length === 0,
  };
}

async function Intro({ locale, audience }: { locale: Locale; audience: Audience }) {
  const t = await getTranslations({ locale, namespace: "home" });
  return (
    <>
      <Segmented
        label={t("switch.label")}
        current={audience}
        transitionName="audience-pill"
        options={[
          { key: "tech", label: t("switch.tech"), href: localizedPath(locale, "/") },
          { key: "recruit", label: t("switch.recruit"), href: localizedPath(locale, "/recruiters") },
        ]}
      />
      <h1 className="t-display">{t(`hero.${audience}.title`)}</h1>
      <p className="max-w-md text-[18px] leading-relaxed text-ink-soft">{t(`hero.${audience}.lead`)}</p>
    </>
  );
}

async function Body({ locale, audience }: { locale: Locale; audience: Audience }) {
  const [data, t, card, rankings, method, recruiters, format, auth] = await Promise.all([
    load(),
    getTranslations({ locale, namespace: "home" }),
    getTranslations({ locale, namespace: "profileCard" }),
    getTranslations({ locale, namespace: "rankings" }),
    getTranslations({ locale, namespace: "landing.method" }),
    getTranslations({ locale, namespace: "recruiters" }),
    getFormatter({ locale }),
    getTranslations({ locale, namespace: "nav" }),
  ]);
  const handle = { field: t("field.field"), placeholder: t("field.placeholder"), submit: t("field.submit"), invalid: t("field.invalid"), missing: t("field.missing"), signIn: auth("publish"), failed: t("field.failed") };
  const indicators = { regularity: card("regularity"), volume: card("volume"), stars: card("stars"), external: card("elsewhere"), languages: card("languages"), seniority: card("seniority") };
  const placeOf = (row: RankingRow) => [row.city, row.country ? countryName(row.country, locale) : null].filter(Boolean).join(" · ");
  const previewRows: PreviewRow[] = data.rows.map((row) => ({
    login: row.login,
    name: row.name,
    place: placeOf(row),
    months: row.months,
    value: {
      regularity: `${row.activeMonths}/12`,
      contributions: format.number(row.publicContributions),
      stars: format.number(row.stars),
      external: format.number(row.mergedPullRequestsElsewhere),
      seniority: String(new Date(row.accountCreatedAt).getUTCFullYear()),
    },
    order: {
      regularity: row.activeMonths * 1e6 + row.publicContributions,
      contributions: row.publicContributions,
      stars: row.stars,
      external: row.mergedPullRequestsElsewhere,
      seniority: -new Date(row.accountCreatedAt).getTime(),
    },
  }));
  const placed = placeCounts(data.cities);
  const globeCities = placed.cities.map(({ city, count }, index) => ({
    id: `city${index}`,
    latitude: 38 - 2 * (city.row + 0.5),
    longitude: -18 + 2 * (city.column + 0.5),
    label: `${locale === "en" ? (city.nameEn ?? city.name) : city.name}, ${countryName(city.country, locale)}`,
    count: t("globe.profiles", { count }),
    size: Math.min(0.09, 0.035 + count * 0.006),
    country: city.country,
    login: data.rows.find((row) => row.country === city.country && row.city && normalizeCity(row.city) === normalizeCity(city.name))?.login ?? null,
  }));

  const hero = (
    <HeroBuild
      locale={locale}
      intro={<Intro locale={locale} audience={audience} />}
      initial={data.card}
      example={data.cardExample}
      labels={{ handle, indicators, example: t("example") }}
    />
  );
  const proof = <ProofBand projects={data.projects} example={data.projectsExample} labels={{ title: t("proof.title"), pause: t("proof.pause"), play: t("proof.play"), example: t("example") }} />;
  const problem = <ProblemSplit card={data.card} labels={{ title: t("problem.title"), cv: t("problem.cv"), profile: t("problem.profile") }} />;
  const anatomy = (
    <section className={section}>
      <h2 className="t-h2">{t("anatomy.title")}</h2>
      <Anatomy locale={locale} card={data.card} example={data.cardExample} labels={{ indicators, tablist: t("anatomy.title"), example: t("example"), merged: t("anatomy.merged"), joined: t("anatomy.joined") }} />
    </section>
  );
  const steps = (
    <section className={section}>
      <h2 className="t-h2">{t("steps.title")}</h2>
      <HowItWorks steps={[t("steps.find"), t("steps.propose"), t("steps.accept"), t("steps.publish")]} merged={t("steps.merged")} published={t("steps.published")} />
    </section>
  );
  const globe = (
    <section className="border-y border-line bg-surface-raised">
      <div className="wrap grid items-center gap-8 py-16 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)] lg:py-20">
        <div className="flex flex-col items-start gap-4">
          <h2 className="t-h2">{t("globe.title")}</h2>
          {data.citiesExample ? <ExampleTag label={t("example")} /> : null}
          <ul className="flex w-full max-w-sm flex-col divide-y divide-line">
            {placed.cities.slice(0, 5).map(({ city, count }) => (
              <li key={`${city.country}:${city.name}`} className="flex min-h-11 items-center justify-between gap-3 text-[14px]">
                <span className="truncate">{locale === "en" ? (city.nameEn ?? city.name) : city.name}</span>
                <span className="font-mono text-ink-muted tabular-nums">{count}</span>
              </li>
            ))}
          </ul>
        </div>
        <GlobeLazy locale={locale} cities={globeCities} example={data.citiesExample} label={t("globe.picture")} open={t("globe.open")} fallback={<AfricaMosaic locale={locale} cities={placed.cities} label={t("globe.picture")} />} />
      </div>
    </section>
  );
  const preview = (
    <section className={section}>
      <h2 className="t-h2">{t("rankings.title")}</h2>
      <RankingPreview
        locale={locale}
        rows={previewRows}
        example={data.rowsExample}
        labels={{
          tabs: t("rankings.title"),
          example: t("example"),
          all: t("rankings.all"),
          sort: { regularity: rankings("sort.regularity"), contributions: rankings("sort.contributions"), stars: rankings("sort.stars"), external: rankings("sort.external"), seniority: rankings("sort.seniority") },
          measures: { regularity: rankings("measures.regularity"), contributions: rankings("measures.contributions"), stars: rankings("measures.stars"), external: rankings("measures.external"), seniority: rankings("measures.seniority") },
        }}
      />
    </section>
  );
  const transparency = (
    <Transparency
      title={t("transparency")}
      readsTitle={method("readsTitle")}
      notReadsTitle={method("notReadsTitle")}
      reads={[method("reads.contributions"), method("reads.seniority"), method("reads.stars"), method("reads.languages"), method("reads.external")]}
      notReads={[method("notReads.private"), method("notReads.linkedin"), method("notReads.strangers"), method("notReads.score")]}
    />
  );
  const faq = (
    <section id="faq" className="wrap flex max-w-3xl scroll-mt-20 flex-col gap-8 py-16 lg:py-24">
      <h2 className="t-h2">{t("faq.title")}</h2>
      <Accordion items={FAQ.map((key) => ({ key, question: t(`faq.${key}.q`), answer: t(`faq.${key}.a`) }))} />
    </section>
  );
  const waitlist = (
    <div id="waitlist" className="flex scroll-mt-20 flex-col gap-3 rounded-panel border border-line bg-surface p-5">
      <p className="t-h3">{recruiters("waitlist.title")}</p>
      <WaitlistForm labels={{ email: recruiters("waitlist.email"), placeholder: recruiters("waitlist.placeholder"), consent: recruiters("waitlist.consent"), submit: recruiters("waitlist.submit"), sending: recruiters("waitlist.sending"), done: recruiters("waitlist.done"), failed: recruiters("waitlist.failed") }} />
      <WaitlistLeaveForm labels={{ summary: recruiters("waitlist.leave.summary"), email: recruiters("waitlist.email"), submit: recruiters("waitlist.leave.submit"), sending: recruiters("waitlist.sending"), done: recruiters("waitlist.leave.done"), failed: recruiters("waitlist.leave.failed") }} />
    </div>
  );
  const explore = (
    <section className={section}>
      <h2 className="t-h2">{t("recruit.title")}</h2>
      <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,1fr)]">
        <div className="flex flex-col gap-3">
          {data.rowsExample ? <div className="w-fit"><ExampleTag label={t("example")} /></div> : null}
          <TalentField
            locale={locale}
            example={data.rowsExample}
            talents={data.rows.map(({ login, city, languages, activeMonths }) => ({ login, city, languages, activeMonths }))}
            labels={{ language: recruiters("field.language"), allLanguages: recruiters("field.allLanguages"), city: recruiters("field.city"), allCities: recruiters("field.allCities"), rhythm: recruiters("field.rhythm"), allRhythms: recruiters("field.allRhythms"), steady: recruiters("field.steady"), regular: recruiters("field.regular"), occasional: recruiters("field.occasional"), shown: recruiters.raw("field.shown") as string, noMatch: recruiters("field.noMatch"), months: recruiters.raw("field.months") as string }}
          />
        </div>
        {waitlist}
      </div>
    </section>
  );
  const close = (
    <section className="border-t border-line bg-surface-raised">
      <div className="wrap flex max-w-2xl flex-col gap-6 py-16 lg:py-24">
        <h2 className="t-h2">{t(`close.${audience}`)}</h2>
        {audience === "tech" ? <HandleField locale={locale} labels={handle} /> : <a href="#waitlist" className="btn btn-primary w-fit">{recruiters("waitlist.submit")}</a>}
      </div>
    </section>
  );

  return audience === "tech" ? (
    <>
      {hero}
      {proof}
      {problem}
      {anatomy}
      {steps}
      {globe}
      {preview}
      {transparency}
      {faq}
      {close}
    </>
  ) : (
    <>
      {hero}
      {explore}
      {preview}
      {globe}
      {anatomy}
      {transparency}
      {faq}
      {close}
    </>
  );
}

export async function Landing({ locale, audience }: { locale: Locale; audience: Audience }) {
  const meta = await getTranslations({ locale, namespace: "meta" });
  const site = process.env.SITE_URL ?? "http://localhost:3000";
  return (
    <>
      {audience === "tech" ? <JsonLd data={{ "@context": "https://schema.org", "@type": "WebSite", name: "SourceFolio", url: `${site}/${locale}`, description: meta("description"), inLanguage: locale }} /> : null}
      <Suspense
        fallback={
          <section className="wrap grid items-center gap-10 pt-8 pb-14 lg:grid-cols-[minmax(0,1fr)_minmax(0,600px)] lg:gap-16 lg:py-20">
            <div className="flex min-w-0 flex-col gap-6"><Intro locale={locale} audience={audience} /></div>
            <div className="h-96 animate-pulse rounded-panel bg-surface-sunken" />
          </section>
        }
      >
        <Body locale={locale} audience={audience} />
      </Suspense>
    </>
  );
}

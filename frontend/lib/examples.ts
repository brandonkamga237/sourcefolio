import type { CalendarCell } from "@/lib/calendar";
import type { ContributionDay, ProfileCard } from "@/services/contribution-types";
import type { CityCount } from "@/services/map";
import type { RankingRow } from "@/services/rankings";

// The only place fictional data may live. Every screen drawing it shows the "Example" label, and
// real data replaces it as soon as a profile is published. No real person or organisation here.

function exampleYear(): (ContributionDay & CalendarCell)[] {
  const start = Date.UTC(2025, 10, 2);
  return Array.from({ length: 364 }, (_, day) => {
    const week = Math.floor(day / 7);
    const wave = (day * 7 + week * 3) % 11;
    const level = (wave > 7 + (40 - Math.min(40, week)) / 12 ? 0 : Math.min(4, Math.floor(wave / 2 + week / 30))) as CalendarCell["level"];
    return { date: new Date(start + day * 86_400_000).toISOString().slice(0, 10), contributions: level * 2, level };
  });
}

const MONTHS = [2, 5, 0, 7, 9, 4, 12, 8, 15, 11, 18, 14];

export const EXAMPLE_LOGIN = "exemple";

export const EXAMPLE_PROFILE: ProfileCard = {
  login: EXAMPLE_LOGIN,
  name: "Amina Diallo",
  country: "SN",
  city: "Dakar",
  bio: null,
  linkedInUrl: null,
  indicators: {
    analyzedAt: "2026-11-01T10:00:00Z",
    name: "Amina Diallo",
    bio: null,
    location: null,
    accountCreatedAt: "2019-03-14T10:00:00Z",
    publicContributions: 412,
    activeMonths: 11,
    months: MONTHS.map((contributions, index) => ({ month: `2026-${String(index + 1).padStart(2, "0")}`, contributions })),
    calendar: exampleYear(),
    stars: 37,
    followers: 21,
    languages: [
      { name: "TypeScript", color: "#3178c6", bytes: 620 },
      { name: "Go", color: "#00add8", bytes: 260 },
      { name: "Shell", color: "#89e051", bytes: 120 },
    ],
    mergedPullRequestsElsewhere: 14,
    externalProjects: [
      { repository: "exemple/passerelle-paiement", url: "", mergedPullRequests: 8 },
      { repository: "exemple/cartes-hors-ligne", url: "", mergedPullRequests: 6 },
    ],
    topRepositories: [
      { name: "sdk-mobile-money", description: null, url: "", stars: 31, primaryLanguage: "Go" },
      { name: "agenda-sms", description: null, url: "", stars: 6, primaryLanguage: "TypeScript" },
    ],
    proofs: { profile: "", repositories: "", mergedElsewhere: "" },
  },
};

const row = (position: number, login: string, name: string, country: string, city: string, activeMonths: number, publicContributions: number, stars: number, mergedPullRequestsElsewhere: number, created: string, months: number[], languages: string[]): RankingRow => ({
  position, login, name, country, city, accountCreatedAt: created, publicContributions, activeMonths, months, stars, followers: 4, mergedPullRequestsElsewhere, languages,
});

export const EXAMPLE_ROWS: RankingRow[] = [
  row(1, "exemple-amina", "Amina Diallo", "SN", "Dakar", 11, 412, 37, 14, "2019-03-14T00:00:00Z", MONTHS, ["TypeScript", "Go"]),
  row(2, "exemple-kwame", "Kwame Boateng", "GH", "Accra", 10, 688, 12, 5, "2017-06-02T00:00:00Z", [4, 0, 6, 9, 12, 3, 8, 0, 14, 7, 10, 5], ["Python"]),
  row(3, "exemple-nadia", "Nadia Benali", "MA", "Casablanca", 9, 305, 84, 3, "2021-01-20T00:00:00Z", [0, 3, 5, 0, 8, 6, 0, 4, 9, 2, 7, 3], ["Kotlin", "Java"]),
  row(4, "exemple-tendai", "Tendai Moyo", "ZW", "Harare", 7, 190, 6, 9, "2020-09-11T00:00:00Z", [0, 0, 2, 4, 0, 6, 3, 0, 5, 0, 8, 1], ["Rust"]),
  row(5, "exemple-ines", "Inès Mbarga", "CM", "Douala", 5, 74, 2, 1, "2023-02-05T00:00:00Z", [0, 0, 0, 2, 0, 1, 0, 3, 0, 0, 2, 4], ["PHP"]),
];

export const EXAMPLE_CITIES: CityCount[] = [
  { country: "SN", city: "Dakar", count: 4 },
  { country: "GH", city: "Accra", count: 6 },
  { country: "MA", city: "Casablanca", count: 3 },
  { country: "ZW", city: "Harare", count: 2 },
  { country: "CM", city: "Douala", count: 5 },
  { country: "KE", city: "Nairobi", count: 7 },
  { country: "NG", city: "Lagos", count: 9 },
];

export const EXAMPLE_PROJECTS: readonly string[] = [
  "exemple/passerelle-paiement", "exemple/cartes-hors-ligne", "exemple/agenda-sms", "exemple/sdk-mobile-money", "exemple/traducteur-wolof", "exemple/registre-ouvert", "exemple/bibliotheque-sante", "exemple/meteo-agricole",
];

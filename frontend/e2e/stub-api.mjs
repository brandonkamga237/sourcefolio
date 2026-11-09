// A stand-in for the backend, so screens can be captured with known data and no database.
// STUB_EMPTY=1 answers as production does before anyone has published.
import http from "node:http";

const empty = process.env.STUB_EMPTY === "1";
const PEOPLE = [
  ["ada-nkeng", "Ada Nkeng", "CM", "Douala", 12, 1284, [3, 9, 4, 14, 22, 6, 18, 30, 12, 8, 16, 21], 37, 14],
  ["kofi-mensah", "Kofi Mensah", "GH", "Accra", 11, 903, [0, 5, 7, 3, 11, 9, 4, 20, 6, 8, 12, 10], 120, 6],
  ["wanjiru-k", "Wanjiru Kamau", "KE", "Nairobi", 10, 644, [2, 0, 8, 3, 9, 0, 4, 7, 6, 8, 5, 3], 8, 9],
  ["tunde-dev", "Tunde Bakare", "NG", "Lagos", 8, 412, [0, 0, 3, 5, 0, 7, 2, 0, 6, 4, 9, 1], 54, 2],
  ["awa-sarr", "Awa Sarr", "SN", "Dakar", 5, 96, [0, 0, 0, 4, 0, 2, 0, 6, 0, 0, 1, 3], 2, 1],
];
const rows = PEOPLE.map(([login, name, country, city, activeMonths, publicContributions, months, stars, mergedPullRequestsElsewhere], index) => ({
  position: index + 1, login, name, country, city, accountCreatedAt: "2019-02-01T00:00:00Z", publicContributions, activeMonths, months, stars, followers: 3,
  mergedPullRequestsElsewhere, languages: ["TypeScript", "Go"],
}));
const calendar = Array.from({ length: 364 }, (_, day) => {
  const wave = (day * 7 + Math.floor(day / 7) * 3) % 11;
  const level = wave > 8 ? 0 : Math.min(4, Math.floor(wave / 2));
  return { date: new Date(Date.UTC(2025, 10, 9) + day * 86_400_000).toISOString().slice(0, 10), contributions: level * 2, level };
});
const card = (row) => ({
  login: row.login, name: row.name, country: row.country, city: row.city, sector: "fintech", bio: "API en Go, paiements mobiles.", linkedInUrl: null,
  indicators: {
    analyzedAt: "2026-11-08T10:00:00Z", name: row.name, bio: null, location: null, accountCreatedAt: row.accountCreatedAt,
    publicContributions: row.publicContributions, activeMonths: row.activeMonths,
    months: row.months.map((contributions, index) => ({ month: `2026-${String(index + 1).padStart(2, "0")}`, contributions })), calendar,
    stars: row.stars, followers: row.followers,
    languages: [{ name: "TypeScript", color: "#3178c6", bytes: 600 }, { name: "Go", color: "#00ADD8", bytes: 300 }, { name: "Shell", color: "#89e051", bytes: 100 }],
    mergedPullRequestsElsewhere: row.mergedPullRequestsElsewhere,
    externalProjects: [{ repository: "example/payments", url: "https://github.com/example/payments", mergedPullRequests: 4 }],
    topRepositories: [{ name: "mobile-money-sdk", description: "SDK de paiement mobile.", url: "https://github.com/example/sdk", stars: 31, primaryLanguage: "Go" }],
    proofs: { profile: "https://github.com/example", repositories: "https://github.com/example?tab=repositories", mergedElsewhere: "https://github.com/pulls" },
  },
});

http
  .createServer((request, response) => {
    const { pathname } = new URL(request.url, "http://stub");
    const json = (status, body) => {
      response.writeHead(status, { "content-type": "application/json" });
      response.end(JSON.stringify(body));
    };
    const profile = pathname.match(/^\/api\/profiles\/([^/]+)$/);
    if (profile) {
      const row = empty ? undefined : rows.find((candidate) => candidate.login === profile[1]);
      return row ? json(200, card(row)) : json(404, {});
    }
    if (pathname === "/api/rankings") return json(200, empty ? { items: [], nextCursor: null, total: 0 } : { items: rows, nextCursor: null, total: rows.length });
    if (pathname === "/api/rankings/facets") return json(200, empty ? { countries: [], languages: [] } : { countries: [{ value: "CM", count: 1 }], languages: [{ value: "Go", count: 5 }] });
    if (pathname === "/api/rankings/cities") return json(200, empty ? [] : [{ country: "NG", city: "Lagos", count: 6 }, { country: "CM", city: "Douala", count: 3 }, { country: "KE", city: "Nairobi", count: 2 }]);
    if (pathname === "/api/rankings/projects") return json(200, empty ? [] : [{ repository: "example/payments", contributors: 3 }]);
    return json(401, {});
  })
  .listen(3101, "127.0.0.1");

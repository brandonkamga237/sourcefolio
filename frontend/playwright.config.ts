import { defineConfig } from "@playwright/test";

const site = "http://127.0.0.1:3100";

export default defineConfig({
  testDir: "e2e",
  outputDir: "test-results",
  timeout: 90_000,
  workers: 2,
  // The Chrome already on the machine: no browser download in development.
  use: { baseURL: site, channel: "chrome" },
  webServer: [
    { command: "node e2e/stub-api.mjs", url: "http://127.0.0.1:3101/api/rankings", reuseExistingServer: true },
    {
      command: "pnpm build && pnpm exec next start -H 127.0.0.1 -p 3100",
      url: `${site}/robots.txt`,
      timeout: 400_000,
      reuseExistingServer: true,
      env: { API_INTERNAL_URL: "http://127.0.0.1:3101", SITE_URL: site },
    },
  ],
});

import { expect, test } from "@playwright/test";

const ROUTES = [
  ["home", "/"],
  ["recruiters", "/recruiters"],
  ["rankings", "/rankings"],
  ["map", "/map"],
  ["me", "/me"],
  ["profile", "/u/ada-nkeng"],
] as const;
const LOCALES = ["fr", "en"] as const;
const THEMES = ["light", "dark"] as const;
const VIEWPORTS = [
  ["desktop", 1440, 900],
  ["mobile", 390, 844],
] as const;

for (const [name, route] of ROUTES) {
  for (const locale of LOCALES) {
    for (const colorScheme of THEMES) {
      for (const [device, width, height] of VIEWPORTS) {
        test(`${name} ${locale} ${colorScheme} ${device}`, async ({ browser, baseURL }, info) => {
          test.skip(name === "profile" && process.env.STUB_EMPTY === "1", "no profile exists when nothing is published");
          // A phone's real pixel density: dot-based drawings read very differently at 1x.
          const context = await browser.newContext({ baseURL, viewport: { width, height }, colorScheme, deviceScaleFactor: device === "mobile" ? 2 : 1 });
          const page = await context.newPage();
          await page.goto(`/${locale}${route === "/" ? "" : route}`);
          await page.waitForLoadState("networkidle");
          const total = await page.evaluate(() => document.documentElement.scrollHeight);
          for (let y = 0; y < total; y += height / 2) {
            await page.evaluate((top) => window.scrollTo(0, top), y);
            await page.waitForTimeout(80);
          }
          await page.evaluate(() => window.scrollTo(0, 0));
          await page.waitForTimeout(400);
          await page.screenshot({ path: `e2e/.shots/${process.env.STUB_EMPTY === "1" ? "empty-" : ""}${name}-${locale}-${colorScheme}-${device}.jpg`, type: "jpeg", quality: 70, fullPage: true, scale: "css" });
          const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
          expect(overflow, `${info.title} overflows sideways`).toBeLessThanOrEqual(0);
          await context.close();
        });
      }
    }
  }
}

for (const [name, route] of ROUTES) {
  test(`${name} fits a 360 px screen`, async ({ page }) => {
    test.skip(name === "profile" && process.env.STUB_EMPTY === "1", "no profile exists when nothing is published");
    await page.setViewportSize({ width: 360, height: 740 });
    await page.goto(`/fr${route === "/" ? "" : route}`);
    await page.waitForLoadState("networkidle");
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(overflow).toBeLessThanOrEqual(0);
  });
}

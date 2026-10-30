// SourceFolio is open source: its own repository's stars, read from GitHub's public API.
import { cacheLife } from "next/cache";

export const PROJECT_REPOSITORY_URL = "https://github.com/brandonkamga237/sourcefolio";

/** null when GitHub cannot be reached: the badge then shows without a number. */
export async function getProjectStars(): Promise<number | null> {
  "use cache";
  // An hour keeps well under the 60 anonymous calls GitHub allows per hour and per server.
  cacheLife("hours");
  try {
    const response = await fetch("https://api.github.com/repos/brandonkamga237/sourcefolio", {
      headers: { accept: "application/vnd.github+json" },
      signal: AbortSignal.timeout(3000),
    });
    if (!response.ok) return null;
    const body: { stargazers_count?: unknown } = await response.json();
    return typeof body.stargazers_count === "number" ? body.stargazers_count : null;
  } catch {
    return null;
  }
}

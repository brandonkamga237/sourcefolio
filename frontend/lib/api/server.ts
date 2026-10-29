import { cookies, headers } from "next/headers";

export interface ApiResult<T> {
  /** The HTTP status, or 0 when the API could not be reached at all. */
  status: number;
  data: T | null;
}

// Server Components reach the API inside the Docker network, not through Traefik. The member's
// session cookie and language go along, so the API answers for them, in their language.
export async function serverGet<T>(path: string): Promise<ApiResult<T>> {
  // Outside the try on purpose: during prerendering these reject to tell Next.js the caller is
  // dynamic, and that signal must reach it.
  const [cookieStore, headerStore] = await Promise.all([cookies(), headers()]);
  try {
    const response = await fetch(`${process.env.API_INTERNAL_URL ?? "http://localhost:8080"}${path}`, {
      headers: {
        accept: "application/json",
        cookie: cookieStore.toString(),
        "accept-language": headerStore.get("accept-language") ?? "",
      },
      cache: "no-store",
    });
    return { status: response.status, data: response.ok ? ((await response.json()) as T) : null };
  } catch (error) {
    console.error(`GET ${path} could not reach the API`, error);
    return { status: 0, data: null };
  }
}

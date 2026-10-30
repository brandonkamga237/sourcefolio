export interface ClientResult<T> {
  /** The HTTP status, or 0 when the API could not be reached at all. */
  status: number;
  data: T | null;
  /** The API's message in the member's language, when it refused the request. */
  message: string | null;
}

const CSRF_COOKIE = "XSRF-TOKEN";

function readCsrfToken(): string | undefined {
  return document.cookie
    .split("; ")
    .find((entry) => entry.startsWith(`${CSRF_COOKIE}=`))
    ?.slice(CSRF_COOKIE.length + 1);
}

// The API sets the XSRF-TOKEN cookie on any GET it answers. A page reached through server
// rendering may not have it yet in the browser: one GET first, then the protected write.
async function clientWrite<T>(method: "POST" | "PUT", path: string, body?: unknown): Promise<ClientResult<T>> {
  try {
    if (!readCsrfToken()) {
      await fetch("/api/me", { credentials: "same-origin" });
    }
    const response = await fetch(path, {
      method,
      credentials: "same-origin",
      headers: {
        "X-XSRF-TOKEN": decodeURIComponent(readCsrfToken() ?? ""),
        ...(body === undefined ? {} : { "content-type": "application/json" }),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const json = response.status === 204 ? null : await response.json().catch(() => null);
    return response.ok
      ? { status: response.status, data: json as T, message: null }
      : { status: response.status, data: null, message: typeof json?.message === "string" ? json.message : null };
  } catch {
    return { status: 0, data: null, message: null };
  }
}

export function clientPost<T>(path: string, body?: unknown): Promise<ClientResult<T>> {
  return clientWrite<T>("POST", path, body);
}

export function clientPut<T>(path: string, body: unknown): Promise<ClientResult<T>> {
  return clientWrite<T>("PUT", path, body);
}

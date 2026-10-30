/** A site as people say it: "nekwalla.dev", "nekwalla.github.io/blog". */
export function siteLabel(siteUrl: string): string {
  const url = new URL(siteUrl);
  return url.pathname === "/" ? url.host : `${url.host}${url.pathname.replace(/\/$/, "")}`;
}

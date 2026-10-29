interface GitHubSignInProps {
  label: string;
  permission: string;
}

// A plain link, not next/link: signing in leaves the app for GitHub and comes back through the
// backend (/oauth2 is routed to it in development and in production).
export function GitHubSignIn({ label, permission }: GitHubSignInProps) {
  return (
    <div className="flex flex-col gap-2">
      {/* eslint-disable-next-line @next/next/no-html-link-for-pages -- /oauth2 is a backend route: it needs a full page load */}
      <a
        href="/oauth2/authorization/github"
        className="flex h-[46px] items-center justify-center rounded-control bg-ink px-5 text-[15px] font-semibold text-ink-inverse"
      >
        {label}
      </a>
      <p className="text-center text-[13px] text-ink-muted">{permission}</p>
    </div>
  );
}

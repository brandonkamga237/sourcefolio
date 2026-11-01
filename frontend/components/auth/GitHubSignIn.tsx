interface GitHubSignInProps {
  label: string;
  permission: string;
  /** The page to come back to once signed in, with its locale: Me, so the member picks their portfolio right away. */
  returnTo: string;
}

// A plain link, not next/link: signing in leaves the app for GitHub and comes back through the
// backend (/oauth2 is routed to it in development and in production).
export function GitHubSignIn({ label, permission, returnTo }: GitHubSignInProps) {
  return (
    <div className="flex flex-col gap-2">
      <a
        href={`/oauth2/authorization/github?returnTo=${encodeURIComponent(returnTo)}`}
        className="press flex h-[46px] items-center justify-center rounded-control bg-ink px-5 text-[15px] font-semibold text-ink-inverse"
      >
        {label}
      </a>
      <p className="text-center text-[13px] text-ink-muted">{permission}</p>
    </div>
  );
}

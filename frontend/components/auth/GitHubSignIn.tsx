import { IconGithub } from "@/components/icons";

interface GitHubSignInProps {
  label: string;
  permission: string;
  /** The page to come back to once signed in, with its locale: My profile, where the analysis starts. */
  returnTo: string;
}

// A plain link, not next/link: signing in leaves the app for GitHub and comes back through the
// backend (/oauth2 is routed to it in development and in production).
export function GitHubSignIn({ label, permission, returnTo }: GitHubSignInProps) {
  return (
    <div className="flex flex-col items-start gap-2">
      <a href={`/oauth2/authorization/github?returnTo=${encodeURIComponent(returnTo)}`} className="btn btn-primary min-h-14 px-7 text-[17px]">
        <IconGithub aria-hidden="true" className="size-5" />
        {label}
      </a>
      <p className="text-[15px] text-ink-muted">{permission}</p>
    </div>
  );
}

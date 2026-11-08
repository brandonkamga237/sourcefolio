import { getTranslations } from "next-intl/server";
import type { ExternalProject } from "@/services/contribution-types";

interface ExternalProjectsProps {
  projects: ExternalProject[];
}

/** Pull requests merged in repositories the member does not own: work others accepted. */
export async function ExternalProjects({ projects }: ExternalProjectsProps) {
  const t = await getTranslations("profileCard");
  return (
    <section aria-labelledby="elsewhere-title" className="flex flex-col gap-2 rounded-card border border-line bg-surface p-4">
      <h2 id="elsewhere-title" className="text-[15px] font-semibold">{t("elsewhereTitle")}</h2>
      {projects.length === 0 ? (
        <p className="text-[13px] leading-snug text-ink-muted">{t("noElsewhere")}</p>
      ) : (
        <ul className="flex flex-col divide-y divide-line-soft">
          {projects.map((project) => (
            <li key={project.repository} className="flex min-h-11 items-center justify-between gap-3 py-2">
              <a href={project.url} target="_blank" rel="noopener noreferrer" className="truncate text-[14px] text-accent hover:underline">
                {project.repository}
              </a>
              <span className="shrink-0 text-[13px] text-ink-muted">{t("mergedCount", { count: project.mergedPullRequests })}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

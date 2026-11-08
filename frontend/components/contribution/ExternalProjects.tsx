import { getTranslations } from "next-intl/server";
import type { ExternalProject } from "@/services/contribution-types";

interface ExternalProjectsProps {
  projects: ExternalProject[];
}

/** Pull requests merged in repositories the member does not own: work others accepted. */
export async function ExternalProjects({ projects }: ExternalProjectsProps) {
  const t = await getTranslations("profileCard");
  return (
    <section aria-labelledby="elsewhere-title" className="flex flex-col gap-3">
      <h2 id="elsewhere-title" className="text-[16px] font-medium">{t("elsewhereTitle")}</h2>
      {projects.length === 0 ? (
        <p className="text-[13px] leading-snug text-ink-muted">{t("noElsewhere")}</p>
      ) : (
        <ul className="flex flex-col divide-y divide-line-soft border-y border-line-soft">
          {projects.map((project) => (
            <li key={project.repository} className="flex min-h-11 items-center justify-between gap-3 py-2">
              <a href={project.url} target="_blank" rel="noopener noreferrer" className="link truncate text-[14px]">
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

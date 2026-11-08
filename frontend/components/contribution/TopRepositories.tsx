import { getFormatter, getTranslations } from "next-intl/server";
import { IconStar } from "@/components/icons";
import type { TopRepository } from "@/services/contribution-types";

interface TopRepositoriesProps {
  repositories: TopRepository[];
}

export async function TopRepositories({ repositories }: TopRepositoriesProps) {
  const [t, format] = await Promise.all([getTranslations("profileCard"), getFormatter()]);
  if (repositories.length === 0) return null;
  return (
    <section aria-labelledby="repositories-title" className="flex flex-col gap-3">
      <h2 id="repositories-title" className="text-[24px] font-semibold tracking-[-0.02em]">{t("topRepositories")}</h2>
      <ul className="flex flex-col divide-y divide-line-soft border-y border-line-soft">
        {repositories.map((repository) => (
          <li key={repository.url}>
            <a href={repository.url} target="_blank" rel="noopener noreferrer" className="flex min-h-20 flex-col gap-1 py-4 transition-colors duration-150 hover:bg-surface-raised @xl:flex-row @xl:items-center @xl:gap-4">
              <span className="min-w-0 flex-1">
                <span className="block truncate text-[18px] font-semibold">{repository.name}</span>
                {repository.description ? <span className="line-clamp-1 text-[16px] text-ink-muted">{repository.description}</span> : null}
              </span>
              <span className="flex shrink-0 items-center gap-3 text-[16px] text-ink-muted">
                {repository.primaryLanguage ? <span>{repository.primaryLanguage}</span> : null}
                <span className="flex items-center gap-1 tabular-nums">
                  <IconStar aria-hidden="true" className="size-4 text-star" />
                  {format.number(repository.stars)}
                </span>
              </span>
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}

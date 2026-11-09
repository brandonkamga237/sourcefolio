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
    <section aria-labelledby="repositories-title" className="flex flex-col gap-3 border-t border-line pt-5">
      <h2 id="repositories-title" className="t-h3">{t("topRepositories")}</h2>
      <ul className="grid gap-x-8 @3xl:grid-cols-2">
        {repositories.map((repository) => (
          <li key={repository.url}>
            <a href={repository.url} target="_blank" rel="noopener noreferrer" className="flex min-h-14 items-center gap-4 border-b border-line-soft py-3 hover:bg-surface-raised">
              <span className="min-w-0 flex-1">
                <span className="block truncate text-[16px] font-semibold text-accent">{repository.name}</span>
                {repository.description ? <span className="line-clamp-1 text-[14px] text-ink-muted">{repository.description}</span> : null}
              </span>
              <span className="flex shrink-0 items-center gap-3 text-[14px] text-ink-muted">
                {repository.primaryLanguage ? <span>{repository.primaryLanguage}</span> : null}
                <span className="flex items-center gap-1 tabular-nums"><IconStar aria-hidden="true" className="size-4 text-star" />{format.number(repository.stars)}</span>
              </span>
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}

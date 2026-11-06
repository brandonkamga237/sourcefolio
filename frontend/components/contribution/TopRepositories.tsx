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
      <h2 id="repositories-title" className="text-[15px] font-semibold">{t("topRepositories")}</h2>
      <ul className="grid gap-3 @xl:grid-cols-2 @3xl:grid-cols-3">
        {repositories.map((repository) => (
          <li key={repository.url}>
            <a
              href={repository.url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex h-full flex-col gap-2 rounded-card border border-line bg-surface p-4 transition-colors hover:border-accent"
            >
              <span className="truncate text-[14px] font-semibold text-accent">{repository.name}</span>
              {repository.description ? <span className="line-clamp-2 text-[13px] leading-snug text-ink-soft">{repository.description}</span> : null}
              <span className="mt-auto flex items-center gap-3 text-[12px] text-ink-muted">
                {repository.primaryLanguage ? <span>{repository.primaryLanguage}</span> : null}
                <span className="flex items-center gap-1">
                  <IconStar aria-hidden="true" className="size-3.5 text-star" />
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

import { getFormatter, getTranslations } from "next-intl/server";
import type { LanguageShare } from "@/services/contribution-types";

interface LanguageBarProps {
  languages: LanguageShare[];
  proofUrl: string;
}

const SHOWN = 4;

/** Bytes of code by language, the four largest and the rest together, as GitHub counts them. */
export async function LanguageBar({ languages, proofUrl }: LanguageBarProps) {
  const [t, format] = await Promise.all([getTranslations("profileCard"), getFormatter()]);
  const total = languages.reduce((sum, language) => sum + language.bytes, 0);
  const shown = languages.slice(0, SHOWN);
  const rest = languages.slice(SHOWN).reduce((sum, language) => sum + language.bytes, 0);
  const parts = [
    ...shown.map((language) => ({ name: language.name, color: language.color, bytes: language.bytes })),
    ...(rest > 0 ? [{ name: t("otherLanguages"), color: null, bytes: rest }] : []),
  ];

  return (
    <section aria-labelledby="languages-title" className="glow-card flex flex-col gap-5 p-6 @xl:p-8">
      <div className="flex items-baseline justify-between gap-2">
        <h2 id="languages-title" className="text-[24px] font-semibold tracking-[-0.02em]">{t("languages")}</h2>
        <a href={proofUrl} target="_blank" rel="noopener noreferrer" className="link text-[16px]">
          {t("viewOnGitHub")}
        </a>
      </div>
      {total === 0 ? (
        <p className="text-[16px] text-ink-muted">{t("noLanguages")}</p>
      ) : (
        <>
          <div className="flex h-3 overflow-hidden rounded-full bg-surface-sunken" aria-hidden="true">
            {parts.map((part) => (
              <span
                key={part.name}
                className={part.color ? "h-full" : "h-full bg-ink-muted"}
                style={{ width: `${(part.bytes / total) * 100}%`, ...(part.color ? { backgroundColor: part.color } : {}) }}
              />
            ))}
          </div>
          <ul className="flex flex-wrap gap-x-4 gap-y-1 text-[16px]">
            {parts.map((part) => (
              <li key={part.name} className="flex items-center gap-1.5">
                <span
                  aria-hidden="true"
                  className={`size-2.5 rounded-full ${part.color ? "" : "bg-ink-muted"}`}
                  style={part.color ? { backgroundColor: part.color } : undefined}
                />
                <span className="font-medium">{part.name}</span>
                <span className="text-ink-muted">{format.number(part.bytes / total, { style: "percent", maximumFractionDigits: 0 })}</span>
              </li>
            ))}
          </ul>
          <p className="text-[14px] text-ink-muted">{t("languagesNote")}</p>
        </>
      )}
    </section>
  );
}

-- Discover filters on the source repository's languages, read from GitHub, instead of the
-- interests authors declare, which say little about the portfolio itself.
CREATE INDEX catalog_entries_repository_languages ON catalog_entries USING gin (repository_languages);
DROP INDEX catalog_entries_specialties;

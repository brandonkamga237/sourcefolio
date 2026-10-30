-- GitHub stars of a showcase's repository, read by an hourly job. Null until the first read, or
-- once the repository is no longer public.
ALTER TABLE source_repos
    ADD COLUMN stars            INTEGER CHECK (stars >= 0),
    ADD COLUMN stars_checked_at TIMESTAMPTZ;

-- The hourly job takes the repositories read longest ago first, never read ones before all.
CREATE INDEX source_repos_stars_checked_at ON source_repos (stars_checked_at NULLS FIRST);

-- The catalogue's copy, shown on the card's detail panel.
ALTER TABLE catalog_entries ADD COLUMN repository_stars INTEGER;

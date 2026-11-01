-- Discover filters by the author's country and by interest: an index for each. The text search
-- (login, site, headline) scans the table, fine for a catalogue of thousands; pg_trgm becomes
-- worth it well beyond.
CREATE INDEX catalog_entries_country_code ON catalog_entries (country_code);
CREATE INDEX catalog_entries_specialties ON catalog_entries USING gin (specialties);

-- The catalogue's copy of each author's public profile, fed by the profile module's events:
-- cards show the country and the first specialty, the member popup shows all of it, and the
-- Discover filters will read them without joining another module's table.
ALTER TABLE catalog_entries
    ADD COLUMN country_code CHAR(2),
    ADD COLUMN specialties  VARCHAR(20)[] NOT NULL DEFAULT '{}';

-- Profiles answered before this copy existed.
UPDATE catalog_entries c
SET country_code = p.country_code,
    specialties  = p.specialties
FROM developer_profiles p
WHERE p.member_id = c.owner_user_id;

-- The member popup finds an author by login; GitHub logins are case-insensitive.
CREATE INDEX catalog_entries_owner_login ON catalog_entries (lower(owner_login));

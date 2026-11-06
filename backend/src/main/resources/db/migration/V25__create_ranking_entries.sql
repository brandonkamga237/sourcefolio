-- The rankings' read model (docs/spec-v2.md): one row per member, fed by two events, the
-- analysis of their GitHub data and the changes of their profile. A row is listed once it has
-- both an analysis and the member's consent (published). Rankings sort on one indicator at a
-- time; hand-entered fields (city) are shown, never sorted on.
CREATE TABLE ranking_entries (
    member_id            UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    login                VARCHAR(39),
    name                 VARCHAR(255),
    country_code         CHAR(2),
    city                 VARCHAR(60),
    published            BOOLEAN NOT NULL DEFAULT FALSE,
    analyzed_at          TIMESTAMPTZ,
    account_created_at   TIMESTAMPTZ,
    public_contributions INTEGER NOT NULL DEFAULT 0,
    active_months        SMALLINT NOT NULL DEFAULT 0,
    -- Contributions of the last twelve months, oldest first, for the regularity strip.
    months               INTEGER[] NOT NULL DEFAULT '{}',
    stars                INTEGER NOT NULL DEFAULT 0,
    followers            INTEGER NOT NULL DEFAULT 0,
    merged_elsewhere     INTEGER NOT NULL DEFAULT 0,
    -- The member's main languages, largest first, as GitHub names them.
    languages            VARCHAR(100)[] NOT NULL DEFAULT '{}'
);

-- Only listed rows are ever read; the country filter is the most common.
CREATE INDEX ranking_entries_listed ON ranking_entries (country_code) WHERE published AND analyzed_at IS NOT NULL;
CREATE INDEX ranking_entries_languages ON ranking_entries USING GIN (languages);

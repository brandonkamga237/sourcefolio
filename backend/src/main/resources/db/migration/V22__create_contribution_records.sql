-- A member's analysis of their public GitHub data: the latest reading of the six indicators of
-- docs/spec-v2.md. One row per member, rewritten at each reading. The scalar indicators are
-- columns for the rankings to come; what only the profile shows (calendar, languages, projects,
-- repositories) is one JSON document, never queried.
CREATE TABLE contribution_records (
    member_id              UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    -- GitHub logins are at most 39 characters; the login follows renames, the id does not change.
    login                  VARCHAR(39) NOT NULL,
    requested_at           TIMESTAMPTZ NOT NULL,
    failed_at              TIMESTAMPTZ,
    analyzed_at            TIMESTAMPTZ,
    account_created_at     TIMESTAMPTZ,
    public_contributions   INTEGER CHECK (public_contributions >= 0),
    active_months          SMALLINT CHECK (active_months BETWEEN 0 AND 12),
    stars                  INTEGER CHECK (stars >= 0),
    followers              INTEGER CHECK (followers >= 0),
    merged_elsewhere       INTEGER CHECK (merged_elsewhere >= 0),
    details                TEXT,
    CHECK ((analyzed_at IS NULL) = (details IS NULL))
);

-- The weekly sweep picks the rows whose last reading or last request is oldest. GREATEST skips
-- nulls, so a row never read is due a week after it was asked for.
CREATE INDEX contribution_records_due ON contribution_records ((GREATEST(analyzed_at, requested_at)));

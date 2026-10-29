-- The last portfolio detection of each member. One scan row per member, so "not scanned yet"
-- and "scanned, nothing found" stay distinct.
CREATE TABLE showcase_candidate_scans (
    member_id  UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    scanned_at TIMESTAMPTZ NOT NULL
);

-- What the scan found, best first. Replaced as a whole on every scan.
CREATE TABLE showcase_candidates (
    id               UUID PRIMARY KEY,
    member_id        UUID          NOT NULL REFERENCES showcase_candidate_scans (member_id) ON DELETE CASCADE,
    position         SMALLINT      NOT NULL,
    repository_owner VARCHAR(100)  NOT NULL,
    repository_name  VARCHAR(100)  NOT NULL,
    site_url         VARCHAR(1000) NOT NULL,
    reason           VARCHAR(20)   NOT NULL
        CHECK (reason IN ('USER_PAGES', 'NAMED_REPOSITORY', 'HOMEPAGE', 'PROJECT_PAGES')),
    -- Also the index for reading and deleting a member's candidates (leading column).
    CONSTRAINT showcase_candidates_member_position UNIQUE (member_id, position)
);

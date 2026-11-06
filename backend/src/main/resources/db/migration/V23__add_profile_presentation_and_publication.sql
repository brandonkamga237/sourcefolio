-- What a member writes on their profile (docs/spec-v2.md): shown, never ranked. A profile is
-- private until published_at is set; unpublishing clears it.
ALTER TABLE developer_profiles
    ADD COLUMN city         VARCHAR(60),
    ADD COLUMN bio          VARCHAR(160),
    -- The LinkedIn identifier only (linkedin.com/in/<handle>); LinkedIn itself is never read.
    ADD COLUMN linkedin     VARCHAR(100),
    ADD COLUMN published_at TIMESTAMPTZ;

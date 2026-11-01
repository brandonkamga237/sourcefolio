-- What the hourly GitHub read now keeps besides stars: the repository's own description, its
-- main languages (names, largest share first) and when code was last pushed. Null or empty
-- until the first read, and again when the repository can no longer be read.
ALTER TABLE source_repos
    ADD COLUMN description VARCHAR(350),
    ADD COLUMN languages   VARCHAR(40)[] NOT NULL DEFAULT '{}',
    ADD COLUMN pushed_at   TIMESTAMPTZ;

-- The catalogue's copy, for the detail panel.
ALTER TABLE catalog_entries
    ADD COLUMN repository_description VARCHAR(350),
    ADD COLUMN repository_languages   VARCHAR(40)[] NOT NULL DEFAULT '{}',
    ADD COLUMN repository_pushed_at   TIMESTAMPTZ;

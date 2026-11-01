-- The one role a member says they work in ("I am DevOps"), shown as their claim on cards and
-- profiles and offered as a Discover filter next to the stack read from GitHub. Optional.
ALTER TABLE developer_profiles ADD COLUMN role VARCHAR(20)
    CHECK (role IN ('frontend', 'backend', 'fullstack', 'mobile', 'devops', 'data-ai', 'security', 'design',
                    'qa', 'game', 'embedded', 'learning'));

-- The catalogue's copy, filtered on: indexed like the country.
ALTER TABLE catalog_entries ADD COLUMN author_role VARCHAR(20);
CREATE INDEX catalog_entries_author_role ON catalog_entries (author_role);

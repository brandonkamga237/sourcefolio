-- The catalogue's own copy of what it lists: the discovery module reads it without joining the
-- showcase or account tables, and it is fed by their events. Only published showcases are here.
CREATE TABLE catalog_entries (
    showcase_id    UUID PRIMARY KEY REFERENCES showcases (id) ON DELETE CASCADE,
    slug           VARCHAR(100)  NOT NULL,
    site_url       VARCHAR(1000) NOT NULL,
    repository     VARCHAR(201)  NOT NULL,
    repository_url VARCHAR(1000) NOT NULL,
    owner_user_id  UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    owner_login    VARCHAR(100)  NOT NULL,
    published_at   TIMESTAMPTZ   NOT NULL
);

-- Recent: keyset pagination on (published_at, showcase_id), newest first.
CREATE INDEX catalog_entries_recent ON catalog_entries (published_at DESC, showcase_id DESC);
-- A GitHub rename updates every card of the member.
CREATE INDEX catalog_entries_owner_user_id ON catalog_entries (owner_user_id);

-- Showcases published before the catalogue existed.
INSERT INTO catalog_entries (showcase_id, slug, site_url, repository, repository_url, owner_user_id, owner_login, published_at)
SELECT s.id, s.slug, s.site_url, r.owner || '/' || r.name, 'https://github.com/' || r.owner || '/' || r.name,
       s.owner_user_id, a.login, s.published_at
FROM showcases s
JOIN source_repos r ON r.showcase_id = s.id
JOIN auth_identities a ON a.user_id = s.owner_user_id AND a.provider = 'GITHUB'
WHERE s.status = 'PUBLISHED' AND s.published_at IS NOT NULL;

-- What members publish. One published showcase per member is a business rule, not a constraint
-- here: the model leaves room for several kinds and several showcases later.
CREATE TABLE showcases (
    id            UUID PRIMARY KEY,
    owner_user_id UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kind          VARCHAR(20)   NOT NULL CHECK (kind IN ('PORTFOLIO')),
    site_url      VARCHAR(1000) NOT NULL,
    status        VARCHAR(20)   NOT NULL
        CHECK (status IN ('PUBLISHED', 'UNAVAILABLE', 'NEEDS_FIX', 'REMOVED')),
    created_at    TIMESTAMPTZ   NOT NULL,
    published_at  TIMESTAMPTZ
);

CREATE INDEX showcases_owner_user_id ON showcases (owner_user_id, created_at DESC);

-- The public repository a showcase is built from.
CREATE TABLE source_repos (
    showcase_id UUID PRIMARY KEY REFERENCES showcases (id) ON DELETE CASCADE,
    provider    VARCHAR(20)  NOT NULL CHECK (provider IN ('GITHUB')),
    owner       VARCHAR(100) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    is_public   BOOLEAN      NOT NULL
);

-- Same repository, same showcase: a repository is never published twice. GitHub names are
-- case-insensitive, so Nekwalla/Portfolio and nekwalla/portfolio are one repository.
CREATE UNIQUE INDEX source_repos_repository ON source_repos (provider, lower(owner), lower(name));

-- Every status change, so the current status can be explained and restored.
CREATE TABLE showcase_status_events (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    showcase_id UUID        NOT NULL REFERENCES showcases (id) ON DELETE CASCADE,
    from_status VARCHAR(20)
        CHECK (from_status IN ('PUBLISHED', 'UNAVAILABLE', 'NEEDS_FIX', 'REMOVED')),
    to_status   VARCHAR(20) NOT NULL
        CHECK (to_status IN ('PUBLISHED', 'UNAVAILABLE', 'NEEDS_FIX', 'REMOVED')),
    reason      VARCHAR(40) NOT NULL,
    actor       VARCHAR(20) NOT NULL CHECK (actor IN ('SYSTEM', 'OWNER', 'MODERATOR')),
    at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX showcase_status_events_showcase_id ON showcase_status_events (showcase_id, at DESC, id DESC);

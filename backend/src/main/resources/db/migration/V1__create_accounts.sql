-- A member, independent of how they sign in. The email is optional: asked when publishing.
CREATE TABLE users (
    id         UUID PRIMARY KEY,
    language   VARCHAR(2)  NOT NULL CHECK (language IN ('fr', 'en')),
    email      VARCHAR(320),
    created_at TIMESTAMPTZ NOT NULL
);

-- One row per provider account. The provider's own id identifies the person; the login only
-- follows renames, so it is neither unique nor used to look anyone up.
CREATE TABLE auth_identities (
    id               UUID PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL CHECK (provider IN ('GITHUB')),
    provider_user_id VARCHAR(100) NOT NULL,
    login            VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT auth_identities_provider_account UNIQUE (provider, provider_user_id)
);

CREATE INDEX auth_identities_user_id ON auth_identities (user_id);

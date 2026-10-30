-- What a member declares about themselves, asked one question at a time after publishing.
-- Every field is optional: a profile row exists from the first answer.
CREATE TABLE developer_profiles (
    member_id    UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    -- ISO 3166-1 alpha-2, translated at display time.
    country_code CHAR(2),
    -- A slug, translated at display time.
    specialty    VARCHAR(20)
        CHECK (specialty IN ('frontend', 'backend', 'fullstack', 'mobile', 'devops', 'data-ai', 'game',
                             'embedded', 'security', 'other')),
    updated_at   TIMESTAMPTZ NOT NULL
);

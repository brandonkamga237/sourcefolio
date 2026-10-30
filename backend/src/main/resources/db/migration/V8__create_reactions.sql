-- Likes. A like is a row, never a counter: one per member and showcase, added or removed in one
-- statement, so two taps can never count twice. The type leaves room for other reactions.
CREATE TABLE reactions (
    user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    showcase_id UUID        NOT NULL REFERENCES showcases (id) ON DELETE CASCADE,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('LIKE')),
    created_at  TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, showcase_id, type)
);

-- Counting a showcase's likes.
CREATE INDEX reactions_showcase_id ON reactions (showcase_id, type);

-- The catalogue's copy of the count, fed by the engagement module's events, for the Ranking tab.
ALTER TABLE catalog_entries ADD COLUMN like_count INTEGER NOT NULL DEFAULT 0;

CREATE INDEX catalog_entries_ranking ON catalog_entries (like_count DESC, published_at DESC, showcase_id DESC);

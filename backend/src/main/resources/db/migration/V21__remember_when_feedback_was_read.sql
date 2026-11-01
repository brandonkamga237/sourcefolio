-- When an author last opened the feedback they received: what came after is new, and their Me
-- tab shows how much. One row per member, written when they read it.
CREATE TABLE feedback_reads (
    member_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    seen_at   TIMESTAMPTZ NOT NULL
);

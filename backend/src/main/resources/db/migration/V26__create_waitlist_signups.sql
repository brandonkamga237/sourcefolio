-- Recruiters waiting for access (docs/spec-v2.md). A row only exists with their consent, so
-- consented_at is both the proof of it and the date of the sign-up.
CREATE TABLE waitlist_signups (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(320) NOT NULL,
    consented_at TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX waitlist_signups_email ON waitlist_signups (lower(email));

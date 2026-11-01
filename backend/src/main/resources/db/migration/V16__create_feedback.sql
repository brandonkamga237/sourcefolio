-- Guided feedback on a portfolio (spec: an optional angle and 10 to 1,000 characters), written
-- by a member in their language and never translated. The author's login is a copy, followed on
-- GitHub renames, so a list of feedback needs no join with another module's tables. Deleting
-- the showcase or the member's account deletes their feedback (spec: "ses commentaires sont
-- supprimés").
CREATE TABLE feedback (
    id           UUID PRIMARY KEY,
    showcase_id  UUID         NOT NULL REFERENCES showcases (id) ON DELETE CASCADE,
    author_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    author_login VARCHAR(100) NOT NULL,
    angle        VARCHAR(20)
        CHECK (angle IN ('DESIGN', 'CLARITY', 'PERFORMANCE', 'MOBILE', 'CODE')),
    body         VARCHAR(1000) NOT NULL CHECK (char_length(body) BETWEEN 10 AND 1000),
    language     VARCHAR(5)   NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'VISIBLE' CHECK (status IN ('VISIBLE', 'HIDDEN')),
    created_at   TIMESTAMPTZ  NOT NULL
);

-- A showcase's feedback, newest first, read page by page.
CREATE INDEX feedback_showcase ON feedback (showcase_id, created_at DESC, id DESC);
-- Renames and account deletion find a member's feedback.
CREATE INDEX feedback_author ON feedback (author_id);

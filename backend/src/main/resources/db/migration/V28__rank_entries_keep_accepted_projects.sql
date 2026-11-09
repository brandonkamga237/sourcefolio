-- Repositories someone else owns where the member has a merged pull request. Copied here so the
-- site can show where published profiles contribute, without reading every member's snapshot.
ALTER TABLE ranking_entries
    ADD COLUMN external_repositories VARCHAR(200)[] NOT NULL DEFAULT '{}';

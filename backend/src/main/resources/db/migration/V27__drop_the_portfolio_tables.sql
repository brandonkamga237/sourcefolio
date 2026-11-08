-- The portfolio catalogue left the product (docs/spec-v2.md) and its data goes with it. Children
-- first and without CASCADE: anything still depending on these tables must stop this loudly.
DROP TABLE feedback_reads;
DROP TABLE feedback;
DROP TABLE reactions;
DROP TABLE catalog_entries;
DROP TABLE showcase_status_events;
DROP TABLE source_repos;
DROP TABLE showcases;
DROP TABLE showcase_candidates;
DROP TABLE showcase_candidate_scans;

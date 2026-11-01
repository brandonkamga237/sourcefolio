-- The member's other public repositories, "owner/name", most recently pushed first: the ones
-- the scan found no site for. Me lists them so a member whose portfolio was not detected can
-- pick it once its GitHub "Website" field is filled. Older scans have none until the next one.
ALTER TABLE showcase_candidate_scans
    ADD COLUMN repositories_without_site VARCHAR(201)[] NOT NULL DEFAULT '{}';

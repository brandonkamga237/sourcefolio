-- A member names up to three specialties, most important first: an ordered array replaces the
-- single column. The order is the meaning, so it is kept as written; the application refuses
-- duplicates, the check refuses unknown slugs and more than three.
ALTER TABLE developer_profiles
    ADD COLUMN specialties VARCHAR(20)[] NOT NULL DEFAULT '{}'
        CHECK (cardinality(specialties) <= 3
            AND specialties <@ ARRAY['frontend', 'backend', 'fullstack', 'mobile', 'devops', 'data-ai', 'game',
                                     'embedded', 'security', 'other']::VARCHAR(20)[]);

-- The one specialty answered so far becomes the first of the list.
UPDATE developer_profiles SET specialties = ARRAY[specialty]::VARCHAR(20)[] WHERE specialty IS NOT NULL;

ALTER TABLE developer_profiles DROP COLUMN specialty;

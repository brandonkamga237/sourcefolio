-- Members now pick up to four interests, most important first, among fifty (the ten first ones
-- unchanged), and write their own headline. The check follows the list the application knows.
ALTER TABLE developer_profiles DROP CONSTRAINT IF EXISTS developer_profiles_specialties_check;

ALTER TABLE developer_profiles
    ADD CONSTRAINT developer_profiles_specialties_check
        CHECK (cardinality(specialties) <= 4
            AND specialties <@ ARRAY[
                'frontend', 'backend', 'fullstack', 'mobile', 'devops', 'data-ai', 'game', 'embedded', 'security', 'other',
                'desktop', 'api', 'web3', 'low-code', 'machine-learning', 'data-engineering', 'data-analysis',
                'computer-vision', 'nlp', 'generative-ai', 'cloud', 'sre', 'networking', 'database', 'ui-design',
                'ux-design', 'product', 'design-systems', 'motion-design', 'accessibility', 'creative-coding', 'testing',
                'performance', 'open-source', 'documentation', 'fintech', 'healthtech', 'edtech', 'ecommerce', 'agritech',
                'iot', 'robotics', 'ar-vr', 'tech-lead', 'freelance', 'teaching', 'devrel', 'community',
                'entrepreneurship', 'research']::VARCHAR(20)[]);

-- A line the member writes about themselves ("Front-end React à Douala"), shown under their name.
ALTER TABLE developer_profiles ADD COLUMN headline VARCHAR(80);

-- The catalogue's copy, for cards and the member popup.
ALTER TABLE catalog_entries ADD COLUMN headline VARCHAR(80);

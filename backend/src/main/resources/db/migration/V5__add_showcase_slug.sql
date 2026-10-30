-- The readable part of a showcase's public address (/fr/p/nekwalla), shared on WhatsApp. Taken
-- from the GitHub login at publication and kept afterwards: a login can change, a shared link
-- must not break.
ALTER TABLE showcases ADD COLUMN slug VARCHAR(100);

-- Showcases published before this migration take their owner's stored login. Two members can
-- hold the same stored login (one renamed on GitHub and has not signed in since), so later
-- duplicates get -2, -3 in publication order, as new publications do.
WITH named AS (
    SELECT s.id,
           lower(a.login) AS login,
           row_number() OVER (PARTITION BY lower(a.login) ORDER BY s.created_at, s.id) AS rank
    FROM showcases s
    JOIN auth_identities a ON a.user_id = s.owner_user_id AND a.provider = 'GITHUB'
)
UPDATE showcases s
SET slug = CASE WHEN named.rank = 1 THEN named.login ELSE named.login || '-' || named.rank END
FROM named
WHERE named.id = s.id;

UPDATE showcases SET slug = id::text WHERE slug IS NULL;

ALTER TABLE showcases ALTER COLUMN slug SET NOT NULL;

CREATE UNIQUE INDEX showcases_slug ON showcases (slug);

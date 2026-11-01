-- Cards show how much feedback a portfolio received, as they show likes: a copy kept by events,
-- seeded here from the feedback already written.
ALTER TABLE catalog_entries ADD COLUMN feedback_count INTEGER NOT NULL DEFAULT 0;

UPDATE catalog_entries e
SET feedback_count = counted.total
FROM (SELECT showcase_id, count(*) AS total FROM feedback WHERE status = 'VISIBLE' GROUP BY showcase_id) counted
WHERE counted.showcase_id = e.showcase_id;

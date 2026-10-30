-- The preview the author chose for their card: an image, and an optional short video. The key
-- finds the object in the bucket (to replace or delete it), the URL is where browsers read it.
ALTER TABLE showcases
    ADD COLUMN preview_image_key VARCHAR(300),
    ADD COLUMN preview_image_url VARCHAR(1000),
    ADD COLUMN preview_video_key VARCHAR(300),
    ADD COLUMN preview_video_url VARCHAR(1000);

-- The catalogue's copy, fed by ShowcasePreviewChanged.
ALTER TABLE catalog_entries
    ADD COLUMN preview_image_url VARCHAR(1000),
    ADD COLUMN preview_video_url VARCHAR(1000);

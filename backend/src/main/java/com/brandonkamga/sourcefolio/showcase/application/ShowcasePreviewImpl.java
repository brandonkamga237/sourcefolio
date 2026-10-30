package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewFile;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import com.brandonkamga.sourcefolio.storage.application.MediaStore;
import com.brandonkamga.sourcefolio.storage.application.StoredMedia;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShowcasePreviewImpl implements ShowcasePreview {

    private static final String FOLDER = "previews";

    private final ShowcaseStore showcases;
    private final MediaStore media;
    private final ShowcaseEvents events;

    public ShowcasePreviewImpl(ShowcaseStore showcases, MediaStore media, ShowcaseEvents events) {
        this.showcases = showcases;
        this.media = media;
        this.events = events;
    }

    @Override
    public Showcase changeImage(UUID memberId, byte[] content) {
        PreviewFile file = PreviewFile.image(content).orElseThrow(() -> new BadRequestException("preview.image-invalid"));
        return replace(memberId, file, Showcase::previewImage, Showcase::withPreviewImage);
    }

    @Override
    public Showcase changeVideo(UUID memberId, byte[] content) {
        PreviewFile file = PreviewFile.video(content).orElseThrow(() -> new BadRequestException("preview.video-invalid"));
        double seconds = file.durationSeconds().orElseThrow(() -> new BadRequestException("preview.video-invalid"));
        if (seconds > PreviewFile.MAX_VIDEO_SECONDS) {
            throw new BadRequestException("preview.video-too-long");
        }
        return replace(memberId, file, Showcase::previewVideo, Showcase::withPreviewVideo);
    }

    @Override
    public Showcase removeImage(UUID memberId) {
        return clear(memberId, Showcase::previewImage, Showcase::withPreviewImage);
    }

    @Override
    public Showcase removeVideo(UUID memberId) {
        return clear(memberId, Showcase::previewVideo, Showcase::withPreviewVideo);
    }

    // Stored first, saved second, the old object deleted last: a failure leaves at worst an
    // orphan object in the bucket, never a showcase pointing to a missing file.
    private Showcase replace(UUID memberId, PreviewFile file, Function<Showcase, PreviewMedia> current,
                             BiFunction<Showcase, PreviewMedia, Showcase> with) {
        Showcase showcase = requireShowcase(memberId);
        StoredMedia stored = media.store(FOLDER + "/" + showcase.id(), file.content(), file.contentType(), file.extension());
        Showcase saved = showcases.save(with.apply(showcase, new PreviewMedia(stored.key(), stored.url())));
        deleteQuietly(current.apply(showcase));
        announce(saved);
        return saved;
    }

    private Showcase clear(UUID memberId, Function<Showcase, PreviewMedia> current, BiFunction<Showcase, PreviewMedia, Showcase> with) {
        Showcase showcase = requireShowcase(memberId);
        if (current.apply(showcase) == null) {
            return showcase;
        }
        Showcase saved = showcases.save(with.apply(showcase, null));
        deleteQuietly(current.apply(showcase));
        announce(saved);
        return saved;
    }

    private Showcase requireShowcase(UUID memberId) {
        return showcases.findByOwner(memberId).orElseThrow(() -> new NotFoundException("preview.none"));
    }

    // An object left behind costs a few kilobytes; failing the member's change for it would not be fair.
    private void deleteQuietly(PreviewMedia previous) {
        if (previous == null) {
            return;
        }
        try {
            media.delete(previous.key());
        } catch (RuntimeException ignored) {
            // The new preview is saved; the old object stays in the bucket.
        }
    }

    private void announce(Showcase saved) {
        events.previewChanged(new ShowcasePreviewChanged(saved.id().value(),
                saved.previewImage() == null ? null : saved.previewImage().url(),
                saved.previewVideo() == null ? null : saved.previewVideo().url()));
    }
}

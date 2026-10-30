package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.shared.exception.AppException;
import com.brandonkamga.sourcefolio.showcase.application.ShowcasePublicationImplTest.InMemoryShowcaseStore;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.storage.application.MediaStore;
import com.brandonkamga.sourcefolio.storage.application.StoredMedia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowcasePreviewImplTest {

    private static final UUID MEMBER = UUID.fromString("0b8f2c1e-4d6a-4f6e-9a77-3c2d1e0f9a11");
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2};

    private final InMemoryShowcaseStore showcases = new InMemoryShowcaseStore();
    private final List<String> stored = new ArrayList<>();
    private final List<String> deleted = new ArrayList<>();
    private boolean deleteFails;
    private final MediaStore media = new MediaStore() {
        @Override
        public StoredMedia store(String folder, byte[] content, String contentType, String extension) {
            String key = folder + "/" + stored.size() + "." + extension;
            stored.add(key + " " + contentType);
            return new StoredMedia(key, "https://media.test/" + key);
        }

        @Override
        public void delete(String key) {
            if (deleteFails) throw new IllegalStateException("bucket down");
            deleted.add(key);
        }
    };
    private final List<ShowcasePreviewChanged> announced = new ArrayList<>();
    private final ShowcasePreview preview = new ShowcasePreviewImpl(showcases, media, new ShowcaseEvents() {
        @Override
        public void showcasePublished(ShowcasePublished event) {
        }

        @Override
        public void previewChanged(ShowcasePreviewChanged event) {
            announced.add(event);
        }
    });

    @Test
    @DisplayName("a new image is stored, saved on the showcase, announced, and the previous one deleted")
    void changeImage() {
        Showcase showcase = publish();

        preview.changeImage(MEMBER, JPEG);
        Showcase second = preview.changeImage(MEMBER, JPEG);

        assertThat(stored).containsExactly("previews/" + showcase.id() + "/0.jpg image/jpeg", "previews/" + showcase.id() + "/1.jpg image/jpeg");
        assertThat(showcases.rows.get(showcase.id().value()).previewImage().url()).isEqualTo("https://media.test/previews/" + showcase.id() + "/1.jpg");
        assertThat(second.previewImage()).isEqualTo(showcases.rows.get(showcase.id().value()).previewImage());
        assertThat(deleted).containsExactly("previews/" + showcase.id() + "/0.jpg");
        assertThat(announced).last().extracting(ShowcasePreviewChanged::imageUrl).isEqualTo(second.previewImage().url());
    }

    @Test
    @DisplayName("a file that is not an accepted image is refused before anything is stored")
    void invalidImage() {
        publish();

        assertThatThrownBy(() -> preview.changeImage(MEMBER, "<svg/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("preview.image-invalid");
        assertThat(stored).isEmpty();
    }

    @Test
    @DisplayName("a video longer than 15 seconds is refused; one of 15 seconds is kept")
    void videoDuration() {
        publish();

        assertThatThrownBy(() -> preview.changeVideo(MEMBER, mp4(1000, 20_000)))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("preview.video-too-long");
        assertThat(stored).isEmpty();

        assertThat(preview.changeVideo(MEMBER, mp4(1000, 15_000)).previewVideo()).isNotNull();
    }

    @Test
    @DisplayName("removing the video deletes it and announces a preview without video; removing nothing changes nothing")
    void removeVideo() {
        Showcase showcase = publish();
        preview.changeVideo(MEMBER, mp4(1000, 5_000));
        announced.clear();

        preview.removeVideo(MEMBER);
        preview.removeVideo(MEMBER);

        assertThat(showcases.rows.get(showcase.id().value()).previewVideo()).isNull();
        assertThat(deleted).hasSize(1);
        assertThat(announced).singleElement().extracting(ShowcasePreviewChanged::videoUrl).isNull();
    }

    @Test
    @DisplayName("a bucket that fails to delete the old image does not fail the change")
    void deleteFailureIgnored() {
        publish();
        preview.changeImage(MEMBER, JPEG);
        deleteFails = true;

        assertThat(preview.changeImage(MEMBER, JPEG).previewImage().key()).endsWith("/1.jpg");
    }

    @Test
    @DisplayName("a member without a showcase cannot set a preview")
    void noShowcase() {
        assertThatThrownBy(() -> preview.changeImage(MEMBER, JPEG))
                .isInstanceOf(AppException.class)
                .extracting(thrown -> ((AppException) thrown).messageKey()).isEqualTo("preview.none");
    }

    private Showcase publish() {
        return showcases.save(Showcase.publishVerified(MEMBER, new ShowcaseSlug("nekwalla"), new ShowcaseCandidate(
                new RepositoryCoordinates("nekwalla", "nekwalla.github.io"), new SiteUrl("https://nekwalla.github.io/"),
                CandidateReason.USER_PAGES), Instant.parse("2026-10-30T09:00:00Z")));
    }

    private static byte[] mp4(int timescale, int duration) {
        ByteBuffer mvhd = ByteBuffer.allocate(20).put(new byte[4]).putInt(0).putInt(0).putInt(timescale).putInt(duration);
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        file.writeBytes(box("ftyp", "isom\0\0\0\0".getBytes(StandardCharsets.ISO_8859_1)));
        file.writeBytes(box("moov", box("mvhd", mvhd.array())));
        return file.toByteArray();
    }

    private static byte[] box(String type, byte[] payload) {
        return ByteBuffer.allocate(8 + payload.length).putInt(8 + payload.length)
                .put(type.getBytes(StandardCharsets.ISO_8859_1)).put(payload).array();
    }
}

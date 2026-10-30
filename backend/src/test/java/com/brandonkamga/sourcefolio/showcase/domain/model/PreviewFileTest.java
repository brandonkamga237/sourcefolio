package com.brandonkamga.sourcefolio.showcase.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;

class PreviewFileTest {

    @Test
    @DisplayName("JPEG, PNG and WebP are recognised by their first bytes")
    void images() {
        assertThat(PreviewFile.image(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0))).hasValueSatisfying(file -> assertThat(file.contentType()).isEqualTo("image/jpeg"));
        assertThat(PreviewFile.image(bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0))).hasValueSatisfying(file -> assertThat(file.extension()).isEqualTo("png"));
        assertThat(PreviewFile.image(ascii("RIFF\0\0\0\0WEBPVP8 "))).hasValueSatisfying(file -> assertThat(file.contentType()).isEqualTo("image/webp"));
    }

    @Test
    @DisplayName("anything else is refused, whatever its name or claimed type: an SVG, a script, an empty file, a too big image")
    void refusedImages() {
        assertThat(PreviewFile.image(ascii("<svg xmlns=\"http://www.w3.org/2000/svg\"><script/></svg>"))).isEmpty();
        assertThat(PreviewFile.image(ascii("GIF89a"))).isEmpty();
        assertThat(PreviewFile.image(new byte[0])).isEmpty();
        byte[] big = new byte[PreviewFile.MAX_IMAGE_BYTES + 1];
        big[0] = (byte) 0xFF; big[1] = (byte) 0xD8; big[2] = (byte) 0xFF;
        assertThat(PreviewFile.image(big)).isEmpty();
    }

    @Test
    @DisplayName("an MP4 gives its duration from the movie header, in both header versions")
    void videoDuration() {
        assertThat(PreviewFile.video(mp4(0, 1000, 12_000)).orElseThrow().durationSeconds()).hasValue(12.0);
        assertThat(PreviewFile.video(mp4(1, 600, 9_000)).orElseThrow().durationSeconds()).hasValue(15.0);
    }

    @Test
    @DisplayName("a file that is not an MP4, or an MP4 without movie header, has no accepted duration")
    void refusedVideos() {
        assertThat(PreviewFile.video(ascii("RIFF\0\0\0\0AVI LIST"))).isEmpty();
        byte[] headerless = box("ftyp", ascii("isom\0\0\0\0"));
        assertThat(PreviewFile.video(headerless).orElseThrow().durationSeconds()).isEmpty();
    }

    // ftyp, then moov containing mvhd with the given version, timescale and duration.
    private static byte[] mp4(int version, int timescale, long duration) {
        ByteBuffer mvhd = ByteBuffer.allocate(version == 1 ? 32 : 20);
        mvhd.put((byte) version).put(new byte[3]);
        if (version == 1) {
            mvhd.putLong(0).putLong(0).putInt(timescale).putLong(duration);
        } else {
            mvhd.putInt(0).putInt(0).putInt(timescale).putInt((int) duration);
        }
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        file.writeBytes(box("ftyp", ascii("isom\0\0\0\0")));
        file.writeBytes(box("moov", box("mvhd", mvhd.array())));
        return file.toByteArray();
    }

    private static byte[] box(String type, byte[] payload) {
        return ByteBuffer.allocate(8 + payload.length).putInt(8 + payload.length).put(ascii(type)).put(payload).array();
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) result[index] = (byte) values[index];
        return result;
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}

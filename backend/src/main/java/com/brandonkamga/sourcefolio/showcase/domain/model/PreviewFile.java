package com.brandonkamga.sourcefolio.showcase.domain.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * An uploaded preview, recognised by its first bytes rather than by its name or the type the
 * browser claims: a renamed file or a forged content type is refused (spec: image of 5 MB at
 * most, MP4 video of 8 MB and 15 seconds at most).
 */
public record PreviewFile(Kind kind, String contentType, String extension, byte[] content) {

    public enum Kind { IMAGE, VIDEO }

    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    public static final int MAX_VIDEO_BYTES = 8 * 1024 * 1024;
    public static final double MAX_VIDEO_SECONDS = 15.5;

    /** An image the spec accepts, or empty. */
    public static Optional<PreviewFile> image(byte[] content) {
        if (content == null || content.length == 0 || content.length > MAX_IMAGE_BYTES) {
            return Optional.empty();
        }
        if (startsWith(content, 0, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF)) {
            return Optional.of(new PreviewFile(Kind.IMAGE, "image/jpeg", "jpg", content));
        }
        if (startsWith(content, 0, (byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G', (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A)) {
            return Optional.of(new PreviewFile(Kind.IMAGE, "image/png", "png", content));
        }
        if (ascii(content, 0, "RIFF") && ascii(content, 8, "WEBP")) {
            return Optional.of(new PreviewFile(Kind.IMAGE, "image/webp", "webp", content));
        }
        return Optional.empty();
    }

    /** An MP4 video of the accepted size, or empty. Its duration is checked separately. */
    public static Optional<PreviewFile> video(byte[] content) {
        if (content == null || content.length < 12 || content.length > MAX_VIDEO_BYTES || !ascii(content, 4, "ftyp")) {
            return Optional.empty();
        }
        return Optional.of(new PreviewFile(Kind.VIDEO, "video/mp4", "mp4", content));
    }

    /**
     * The duration in seconds read from the movie header (moov/mvhd), or empty when the file has
     * none, which a real MP4 always has.
     */
    public OptionalDouble durationSeconds() {
        int moov = findBox(content, 0, content.length, "moov");
        if (moov < 0) {
            return OptionalDouble.empty();
        }
        int moovEnd = Math.min(content.length, moov + boxSize(content, moov));
        int mvhd = findBox(content, moov + 8, moovEnd, "mvhd");
        if (mvhd < 0 || mvhd + 9 > content.length) {
            return OptionalDouble.empty();
        }
        ByteBuffer header = ByteBuffer.wrap(content, mvhd + 8, content.length - mvhd - 8);
        int version = header.get() & 0xFF;
        // Version 1 stores times and duration on 64 bits: 32 bytes up to the duration, else 20.
        if (header.remaining() < (version == 1 ? 31 : 19)) {
            return OptionalDouble.empty();
        }
        header.position(header.position() + 3);
        long timescale;
        long duration;
        if (version == 1) {
            header.position(header.position() + 16);
            timescale = Integer.toUnsignedLong(header.getInt());
            duration = header.getLong();
        } else {
            header.position(header.position() + 8);
            timescale = Integer.toUnsignedLong(header.getInt());
            duration = Integer.toUnsignedLong(header.getInt());
        }
        return timescale == 0 ? OptionalDouble.empty() : OptionalDouble.of((double) duration / timescale);
    }

    // Boxes are laid end to end: size (4 bytes, big-endian), type (4 ASCII bytes). A size of 1
    // means a 64-bit size follows, 0 means the box runs to the end.
    private static int findBox(byte[] bytes, int from, int to, String type) {
        int position = from;
        while (position + 8 <= to) {
            if (ascii(bytes, position + 4, type)) {
                return position;
            }
            int size = boxSize(bytes, position);
            if (size < 8) {
                return -1;
            }
            position += size;
        }
        return -1;
    }

    private static int boxSize(byte[] bytes, int position) {
        long size = Integer.toUnsignedLong(ByteBuffer.wrap(bytes, position, 4).getInt());
        if (size == 1 && position + 16 <= bytes.length) {
            size = ByteBuffer.wrap(bytes, position + 8, 8).getLong();
        } else if (size == 0) {
            size = bytes.length - position;
        }
        return (int) Math.min(size, bytes.length - position);
    }

    private static boolean startsWith(byte[] bytes, int offset, byte... prefix) {
        if (bytes.length < offset + prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (bytes[offset + index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private static boolean ascii(byte[] bytes, int offset, String text) {
        return startsWith(bytes, offset, text.getBytes(StandardCharsets.US_ASCII));
    }
}

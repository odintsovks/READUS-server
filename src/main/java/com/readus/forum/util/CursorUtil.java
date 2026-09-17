package com.readus.forum.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Opaque cursor helpers. Keyset cursors encode (created_at, id) for
 * (created_at DESC, id DESC) listings; offset cursors paginate the scored feed.
 */
public final class CursorUtil {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String OFFSET_PREFIX = "offset:";

    private CursorUtil() {
    }

    public record KeysetCursor(long epochMillis, UUID id) {
    }

    public static String encodeKeyset(Instant createdAt, UUID id) {
        String raw = createdAt.toEpochMilli() + "|" + id;
        return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** Decodes a keyset cursor, or returns {@code null} when blank/invalid (treated as first page). */
    public static KeysetCursor decodeKeyset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
            int sep = raw.indexOf('|');
            if (sep < 0) {
                return null;
            }
            return new KeysetCursor(Long.parseLong(raw.substring(0, sep)), UUID.fromString(raw.substring(sep + 1)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String encodeOffset(int offset) {
        return ENCODER.encodeToString((OFFSET_PREFIX + offset).getBytes(StandardCharsets.UTF_8));
    }

    /** Decodes an offset cursor, or returns 0 when blank/invalid. */
    public static int decodeOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String raw = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
            if (!raw.startsWith(OFFSET_PREFIX)) {
                return 0;
            }
            return Integer.parseInt(raw.substring(OFFSET_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}

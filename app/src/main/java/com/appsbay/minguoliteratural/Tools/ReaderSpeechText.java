package com.appsbay.minguoliteratural.Tools;

import java.util.ArrayList;
import java.util.List;

/** Break long chapters into utterances that Android TTS can accept. */
public final class ReaderSpeechText {
    private ReaderSpeechText() {}

    public static List<String> chunks(String text, int maxLength) {
        ArrayList<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty() || maxLength < 1) return chunks;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxLength);
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))
                    && Character.isLowSurrogate(text.charAt(end))) {
                end = end - start == 1 ? end + 1 : end - 1;
            }
            if (end < text.length()) {
                // Prefer a sentence boundary, but do not make very short utterances.
                for (int i = end - 1; i >= start + maxLength / 3; i--) {
                    char c = text.charAt(i);
                    if ("。！？；.!?;\n".indexOf(c) >= 0) {
                        end = i + 1;
                        break;
                    }
                }
            }
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) chunks.add(chunk);
            start = end;
        }
        return chunks;
    }
}

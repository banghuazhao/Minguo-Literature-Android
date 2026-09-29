package com.appsbay.minguoliteratural;

import com.appsbay.minguoliteratural.Tools.ReaderSpeechText;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ReaderSpeechTextTest {
    @Test public void chineseSentencesStayWithinTtsLimit() {
        String source = "第一句很长很长。第二句也很长很长！第三句结束。";
        List<String> chunks = ReaderSpeechText.chunks(source, 16);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().allMatch(chunk -> chunk.length() <= 16));
        assertEquals(source, String.join("", chunks));
        assertTrue(chunks.get(0).endsWith("。"));
    }

    @Test public void emptyTextCreatesNoUtterances() {
        assertTrue(ReaderSpeechText.chunks("", 240).isEmpty());
        assertTrue(ReaderSpeechText.chunks(null, 240).isEmpty());
    }

    @Test public void supplementaryCharactersStayTogether() {
        String source = "第一句𠀀第二句。";
        List<String> chunks = ReaderSpeechText.chunks(source, 4);
        assertEquals(source, String.join("", chunks));
        assertTrue(chunks.stream().noneMatch(chunk ->
                Character.isHighSurrogate(chunk.charAt(chunk.length() - 1)) ||
                        Character.isLowSurrogate(chunk.charAt(0))));
    }
}

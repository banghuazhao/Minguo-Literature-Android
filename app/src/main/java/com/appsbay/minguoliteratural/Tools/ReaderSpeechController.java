package com.appsbay.minguoliteratural.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.appsbay.minguoliteratural.Model.Book;
import com.appsbay.minguoliteratural.R;

import java.util.List;
import java.util.Locale;

/** Keeps speech state and voice preferences separate from the reader view. */
public final class ReaderSpeechController {
    public enum State { IDLE, INITIALIZING, PLAYING, PAUSED, ERROR }

    private static final String PREFS = "Reader Speech";
    private static final String KEY_VOICE = "voice";
    private static final String KEY_RATE = "rate";
    public static final String[] VOICE_TAGS = {"auto", "zh-CN", "zh-TW", "zh-HK"};

    private final Activity activity;
    private final Book book;
    private final SharedPreferences preferences;
    private TextToSpeech engine;
    private boolean ready;
    private boolean released;
    private State state = State.IDLE;
    private int errorRes = R.string.tts_start_failed;
    private List<String> chunks;
    private int chunkIndex;
    private int token;
    private String activeUtterance;
    private Runnable listener;

    public ReaderSpeechController(Activity activity, Book book) {
        this.activity = activity;
        this.book = book;
        preferences = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public State getState() { return state; }
    public int getErrorRes() { return errorRes; }
    public float getRate() { return preferences.getFloat(KEY_RATE, 1.0f); }
    public int getVoiceIndex() {
        String tag = preferences.getString(KEY_VOICE, VOICE_TAGS[0]);
        for (int i = 0; i < VOICE_TAGS.length; i++) {
            if (VOICE_TAGS[i].equals(tag)) return i;
        }
        return 0;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
        notifyChanged();
    }

    public void setRate(float rate) {
        float clamped = Math.max(0.6f, Math.min(1.5f, rate));
        preferences.edit().putFloat(KEY_RATE, clamped).apply();
        if (ready && engine != null) engine.setSpeechRate(clamped);
        if (state == State.PLAYING) repeatCurrentChunk();
        notifyChanged();
    }

    public void setVoiceIndex(int index) {
        if (index < 0 || index >= VOICE_TAGS.length) return;
        preferences.edit().putString(KEY_VOICE, VOICE_TAGS[index]).apply();
        if (ready && (state == State.PLAYING || state == State.PAUSED)) {
            if (!configureVoice()) return;
            if (state == State.PLAYING) repeatCurrentChunk();
        }
        notifyChanged();
    }

    public void start(String text) {
        if (released) return;
        chunks = ReaderSpeechText.chunks(text,
                Math.max(1, Math.min(240, TextToSpeech.getMaxSpeechInputLength() - 1)));
        if (chunks.isEmpty()) {
            fail(R.string.tts_no_text);
            return;
        }
        chunkIndex = 0;
        invalidateUtterance();
        if (engine != null) engine.stop();
        state = State.INITIALIZING;
        notifyChanged();
        if (ready) {
            beginSpeaking();
        } else if (engine == null) {
            engine = new TextToSpeech(activity.getApplicationContext(), status ->
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (released || state != State.INITIALIZING) return;
                        if (status != TextToSpeech.SUCCESS || engine == null) {
                            if (engine != null) {
                                engine.shutdown();
                                engine = null;
                            }
                            fail(R.string.tts_start_failed);
                            return;
                        }
                        ready = true;
                        engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override public void onStart(String utteranceId) {}
                            @Override public void onDone(String utteranceId) {
                                activity.runOnUiThread(() -> advance(utteranceId));
                            }
                            @Override public void onError(String utteranceId) {
                                activity.runOnUiThread(() -> {
                                    if (activeUtterance.equals(utteranceId)) fail(R.string.tts_start_failed);
                                });
                            }
                        });
                        beginSpeaking();
                    }));
        }
    }

    private void beginSpeaking() {
        if (!configureVoice()) return;
        engine.setSpeechRate(getRate());
        state = State.PLAYING;
        speakCurrentChunk();
    }

    private boolean configureVoice() {
        String selected = VOICE_TAGS[getVoiceIndex()];
        String[] candidates = "auto".equals(selected)
                ? (book.getBookType().name().endsWith("_Fan")
                    ? new String[]{"zh-TW", "zh-HK", "zh-CN", "zh"}
                    : new String[]{"zh-CN", "zh-TW", "zh-HK", "zh"})
                : new String[]{selected};
        for (String tag : candidates) {
            Locale locale = Locale.forLanguageTag(tag);
            int required = locale.getCountry().isEmpty()
                    ? TextToSpeech.LANG_AVAILABLE : TextToSpeech.LANG_COUNTRY_AVAILABLE;
            if (engine.isLanguageAvailable(locale) >= required
                    && engine.setLanguage(locale) >= required) {
                return true;
            }
        }
        fail(R.string.tts_voice_unavailable);
        return false;
    }

    public void pause() {
        if (state != State.PLAYING) return;
        state = State.PAUSED;
        invalidateUtterance();
        if (engine != null) engine.stop();
        notifyChanged();
    }

    public void resume() {
        if (state != State.PAUSED || engine == null) return;
        if (!configureVoice()) return;
        engine.setSpeechRate(getRate());
        state = State.PLAYING;
        speakCurrentChunk();
    }

    public void stop() {
        invalidateUtterance();
        if (engine != null) engine.stop();
        if (!ready && engine != null) {
            engine.shutdown();
            engine = null;
        }
        state = State.IDLE;
        chunks = null;
        chunkIndex = 0;
        notifyChanged();
    }

    public void shutdown() {
        released = true;
        listener = null;
        stop();
        if (engine != null) {
            engine.shutdown();
            engine = null;
        }
        ready = false;
    }

    private void repeatCurrentChunk() {
        invalidateUtterance();
        engine.stop();
        speakCurrentChunk();
    }

    private void speakCurrentChunk() {
        if (state != State.PLAYING || chunks == null || chunkIndex >= chunks.size()) return;
        activeUtterance = "reader-" + (++token);
        int result = engine.speak(chunks.get(chunkIndex), TextToSpeech.QUEUE_FLUSH,
                null, activeUtterance);
        if (result == TextToSpeech.ERROR) fail(R.string.tts_start_failed);
        else notifyChanged();
    }

    private void advance(String utteranceId) {
        if (released || state != State.PLAYING || !utteranceId.equals(activeUtterance)) return;
        chunkIndex++;
        if (chunks == null || chunkIndex >= chunks.size()) {
            stop();
        } else {
            speakCurrentChunk();
        }
    }

    private void fail(int messageRes) {
        invalidateUtterance();
        if (engine != null) engine.stop();
        errorRes = messageRes;
        state = State.ERROR;
        notifyChanged();
    }

    private void invalidateUtterance() {
        activeUtterance = "invalid-" + (++token);
    }

    private void notifyChanged() {
        if (listener != null) listener.run();
    }
}

package com.appsbay.minguoliteratural;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.appsbay.minguoliteratural.Controller.BookChapterActivity;
import com.appsbay.minguoliteratural.Controller.BookPagerActivity;
import com.appsbay.minguoliteratural.Model.Book;
import com.appsbay.minguoliteratural.Model.BookChapter;
import com.appsbay.minguoliteratural.Model.BookLibrary;
import com.appsbay.minguoliteratural.Model.BookStore;
import com.appsbay.minguoliteratural.Tools.BookOpener;
import com.appsbay.minguoliteratural.Tools.ReadingProgressHelper;
import com.appsbay.minguoliteratural.Tools.TinyDB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReaderFlowInstrumentedTest {
    private Context context;
    private final Map<String, Map<String, ?>> snapshots = new HashMap<>();
    private static final String[] PREFS = {
            "default", "Language Preference", "Bookmarks", "Reading Progress",
            "Continue Reading", "Reader Speech"
    };

    private SharedPreferences prefs(String name) {
        return "default".equals(name) ? PreferenceManager.getDefaultSharedPreferences(context)
                : context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.appsbay.minguoliteratural", context.getPackageName());
        for (String name : PREFS) {
            snapshots.put(name, new HashMap<>(prefs(name).getAll()));
            prefs(name).edit().clear().commit();
        }
    }

    @After
    @SuppressWarnings("unchecked")
    public void tearDown() {
        for (String name : PREFS) {
            SharedPreferences.Editor editor = prefs(name).edit().clear();
            for (Map.Entry<String, ?> entry : snapshots.get(name).entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) editor.putString(entry.getKey(), (String) value);
                else if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
                else if (value instanceof Long) editor.putLong(entry.getKey(), (Long) value);
                else if (value instanceof Float) editor.putFloat(entry.getKey(), (Float) value);
                else if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
                else if (value instanceof Set) editor.putStringSet(entry.getKey(), (Set<String>) value);
            }
            editor.commit();
        }
        BookStore.shared.updateLanguage(context);
    }

    private Book edition(int script, String id) {
        context.getSharedPreferences("Language Preference", Context.MODE_PRIVATE)
                .edit().putInt("language", script).commit();
        BookStore.shared.updateLanguage(context);
        for (Book book : BookStore.shared.getBooks(context)) {
            if (id.equals(book.getCatalogId())) return book;
        }
        throw new AssertionError("Missing " + id + " in script " + script);
    }

    @Test
    public void favoritesAndReadingSurviveScriptSwitch() {
        Book simplified = edition(0, "边城");
        Book traditional = edition(1, "边城");
        assertNotEquals(simplified.getEditionId(), traditional.getEditionId());
        assertEquals(simplified.getCatalogId(), traditional.getCatalogId());

        ArrayList<String> legacyFavorites = new ArrayList<>();
        legacyFavorites.add(simplified.getName());
        legacyFavorites.add(traditional.getName());
        new TinyDB(context).putListString(BookLibrary.BookLibrary, legacyFavorites);
        context.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE).edit()
                .putInt(simplified.getName(), 2).commit();
        context.getSharedPreferences("Reading Progress", Context.MODE_PRIVATE).edit()
                .putInt(simplified.getName() + "_total", 12)
                .putFloat(simplified.getName() + "_scroll", 0.4f).commit();
        context.getSharedPreferences("Continue Reading", Context.MODE_PRIVATE).edit()
                .putString("lastBookName", simplified.getName())
                .putInt("lastChapterIndex", 2).commit();

        edition(0, "边城");
        assertEquals(1, BookLibrary.shared.books(context).size());
        assertEquals(simplified.getName(), BookLibrary.shared.books(context).get(0).getName());
        assertEquals(2, ReadingProgressHelper.getChapterIndex(context, simplified));
        assertEquals(12, ReadingProgressHelper.getTotalChapters(context, simplified));
        assertEquals(0.4f, ReadingProgressHelper.getScrollFraction(context, simplified), 0.001f);
        assertEquals(simplified.getName(), ReadingProgressHelper.getContinueReadingBook(context).getName());

        edition(1, "边城");
        assertEquals(1, BookLibrary.shared.books(context).size());
        assertEquals(traditional.getName(), BookLibrary.shared.books(context).get(0).getName());
        assertTrue(BookLibrary.shared.have(context, traditional));
        assertEquals(-1, ReadingProgressHelper.getChapterIndex(context, traditional));
        assertEquals(traditional.getName(), ReadingProgressHelper.getContinueReadingBook(context).getName());
        assertEquals(0, ReadingProgressHelper.getContinueChapterIndex(context));
        ReadingProgressHelper.markChapterOpened(context, traditional, 1, "二", "第二章", 12);
        assertEquals(1, ReadingProgressHelper.getContinueChapterIndex(context));

        edition(0, "边城");
        assertEquals(2, ReadingProgressHelper.getChapterIndex(context, simplified));
        assertEquals(1, ReadingProgressHelper.getChapterIndex(context, traditional));
        assertEquals(2, ReadingProgressHelper.getContinueChapterIndex(context));
        BookLibrary.shared.remove(context, traditional);
        assertFalse(BookLibrary.shared.have(context, simplified));
    }

    @Test
    public void chapterScreenOpensBundledTraditionalText() {
        Book traditional = edition(1, "朝花夕拾");
        assertEquals("朝花夕拾繁体", traditional.getFileName());
        Intent intent = new Intent(context, BookChapterActivity.class);
        intent.putExtra(BookOpener.EXTRA_BOOK, traditional);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<BookChapterActivity> ignored = ActivityScenario.launch(intent)) {
            // The chapter list is loaded from a bundled JSON file on a background dispatcher.
            Throwable last = null;
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    onView(withId(R.id.book_chapter_recycler_view)).check(matches(isDisplayed()));
                    return;
                } catch (AssertionError | RuntimeException error) {
                    last = error;
                    try { Thread.sleep(100); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }
            }
            throw new AssertionError(last);
        }
    }

    @Test
    public void traditionalChapterFieldNamesLoad() {
        Book traditional = edition(1, "野草");
        Intent intent = new Intent(context, BookChapterActivity.class);
        intent.putExtra(BookOpener.EXTRA_BOOK, traditional);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<BookChapterActivity> ignored = ActivityScenario.launch(intent)) {
            Throwable last = null;
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    onView(withId(R.id.book_chapter_recycler_view)).check(matches(isDisplayed()));
                    return;
                } catch (AssertionError | RuntimeException error) {
                    last = error;
                    try { Thread.sleep(100); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }
            }
            throw new AssertionError(last);
        }
    }

    @Test
    public void readAloudSettingsOpenWithSavedVoiceAndSpeed() {
        Book book = edition(1, "野草");
        prefs("Reader Speech").edit().putString("voice", "zh-TW")
                .putFloat("rate", 1.2f).commit();
        Intent intent = new Intent(context, BookPagerActivity.class);
        intent.putExtra("book", book);
        intent.putExtra("bookChapter", new BookChapter("第一章", "第一章", "這是一段朗讀測試。"));
        intent.putExtra("chapterIndex", 0);
        intent.putExtra("totalChapters", 1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<BookPagerActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.nav_book_pager_more)).perform(click());
            onView(withId(R.id.sheet_action_tts)).perform(click());
            onView(withId(R.id.speech_voice)).check(matches(isDisplayed()));
            onView(withId(R.id.speech_voice)).check((view, error) -> {
                if (error != null) throw error;
                assertEquals(2, ((Spinner) view).getSelectedItemPosition());
            });
            onView(withId(R.id.speech_rate)).check((view, error) -> {
                if (error != null) throw error;
                assertEquals(1.2f,
                        ((com.google.android.material.slider.Slider) view).getValue(), 0.01f);
            });
            onView(withId(R.id.speech_toggle)).perform(click());
            onView(withId(R.id.speech_status)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void resumeOpensTheSelectedEditionChapter() {
        Book simplified = edition(0, "边城");
        ReadingProgressHelper.markChapterOpened(context, simplified, 2,
                "第三章", "第三章", 8);
        Book traditional = edition(1, "边城");
        BookLibrary.shared.save(context, simplified);
        ReadingProgressHelper.markChapterOpened(context, traditional, 1,
                "第二章", "第二章", 8);
        assertEquals(traditional.getName(), BookLibrary.shared.books(context).get(0).getName());

        Intent intent = new Intent(context, BookChapterActivity.class);
        intent.putExtra(BookOpener.EXTRA_BOOK, traditional);
        intent.putExtra(BookOpener.EXTRA_AUTO_OPEN_CHAPTER, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<BookChapterActivity> ignored = ActivityScenario.launch(intent)) {
            Throwable last = null;
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    onView(withId(R.id.book_pager_title))
                            .check(matches(withText("第二章")));
                    assertEquals(1, ReadingProgressHelper.getChapterIndex(context, traditional));
                    assertEquals(2, ReadingProgressHelper.getChapterIndex(context, simplified));
                    pressBack();
                    return;
                } catch (AssertionError | RuntimeException error) {
                    last = error;
                    try { Thread.sleep(100); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }
            }
            throw new AssertionError(last);
        }
    }
}

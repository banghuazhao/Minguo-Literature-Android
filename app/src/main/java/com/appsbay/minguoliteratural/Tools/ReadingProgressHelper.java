package com.appsbay.minguoliteratural.Tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.appsbay.minguoliteratural.Model.Book;
import com.appsbay.minguoliteratural.Model.BookStore;

public final class ReadingProgressHelper {

    private static final String CONTINUE_PREFS = "Continue Reading";
    private static final String PROGRESS_PREFS = "Reading Progress";
    private static final String BOOKMARKS_PREFS = "Bookmarks";
    private static final String KEY_BOOK = "lastBookName";
    private static final String KEY_BOOK_ID = "lastBookId";
    private static final String KEY_EDITION_ID = "lastEditionId";
    private static final String KEY_CHAPTER_INDEX = "lastChapterIndex";
    private static final String KEY_CHAPTER_NUMBER = "lastChapterNumberName";
    private static final String KEY_CHAPTER_NAME = "lastChapterName";
    private static final String KEY_UPDATED_AT = "lastUpdatedAt";
    private static final String EDITION_PREFIX = "edition:";

    private ReadingProgressHelper() {}

    private static String editionKey(Book book) {
        return EDITION_PREFIX + book.getEditionId();
    }

    /** Copy legacy title-keyed progress once for each edition. Keep old keys for safe upgrades. */
    private static void migrate(Context context, Book book) {
        String key = editionKey(book);
        String name = book.getName();
        SharedPreferences bookmarks = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
        if (!bookmarks.contains(key) && bookmarks.contains(name)) {
            bookmarks.edit().putInt(key, bookmarks.getInt(name, -1)).apply();
        }
        SharedPreferences progress = context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = progress.edit();
        boolean changed = false;
        if (!progress.contains(totalKey(key)) && progress.contains(totalKey(name))) {
            editor.putInt(totalKey(key), progress.getInt(totalKey(name), 0));
            changed = true;
        }
        if (!progress.contains(scrollKey(key)) && progress.contains(scrollKey(name))) {
            editor.putFloat(scrollKey(key), progress.getFloat(scrollKey(name), 0f));
            changed = true;
        }
        if (changed) editor.apply();
    }

    public static void saveSession(Context context, Book book, int chapterIndex,
                                   String chapterNumberName, String chapterName,
                                   int totalChapters, float scrollFraction) {
        if (context == null || book == null) return;
        migrate(context, book);
        context.getSharedPreferences(CONTINUE_PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_BOOK_ID, book.getCatalogId())
                .putString(KEY_EDITION_ID, book.getEditionId())
                .putInt(KEY_CHAPTER_INDEX, chapterIndex)
                .putString(KEY_CHAPTER_NUMBER, chapterNumberName)
                .putString(KEY_CHAPTER_NAME, chapterName)
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .apply();
        context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE).edit()
                .putInt(totalKey(editionKey(book)), totalChapters)
                .putFloat(scrollKey(editionKey(book)), scrollFraction)
                .apply();
    }

    public static void markChapterOpened(Context context, Book book, int chapterIndex,
                                         String chapterNumberName, String chapterName,
                                         int totalChapters) {
        if (context == null || book == null) return;
        int previousIndex = getChapterIndex(context, book);
        float scroll = previousIndex == chapterIndex && hasScrollFraction(context, book)
                ? getScrollFraction(context, book) : 0f;
        if (!hasScrollFraction(context, book)) {
            String legacyKey = "bookName: " + book.getName() + ", chapterNumber: "
                    + chapterNumberName + ", chapterName: " + chapterName;
            scroll = context.getSharedPreferences(legacyKey, Context.MODE_PRIVATE)
                    .getFloat(legacyKey, 0f);
        }
        context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE).edit()
                .putInt(editionKey(book), chapterIndex).apply();
        saveSession(context, book, chapterIndex, chapterNumberName, chapterName, totalChapters, scroll);
    }

    public static float getScrollFraction(Context context, Book book) {
        if (context == null || book == null) return 0f;
        migrate(context, book);
        return context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE)
                .getFloat(scrollKey(editionKey(book)), 0f);
    }

    public static boolean hasScrollFraction(Context context, Book book) {
        if (context == null || book == null) return false;
        migrate(context, book);
        return context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE)
                .contains(scrollKey(editionKey(book)));
    }

    public static void saveTotalChapters(Context context, Book book, int totalChapters) {
        if (context == null || book == null || totalChapters <= 0) return;
        migrate(context, book);
        context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE).edit()
                .putInt(totalKey(editionKey(book)), totalChapters).apply();
    }

    @Nullable
    public static Book getContinueReadingBook(Context context) {
        if (context == null) return null;
        SharedPreferences prefs = context.getSharedPreferences(CONTINUE_PREFS, Context.MODE_PRIVATE);
        String id = prefs.getString(KEY_BOOK_ID, null);
        if (id == null) {
            String oldName = prefs.getString(KEY_BOOK, null);
            if (oldName != null) {
                for (Book book : BookStore.shared.getAllBooks(context)) {
                    if (oldName.equals(book.getName())) {
                        id = book.getCatalogId();
                        prefs.edit().putString(KEY_BOOK_ID, id)
                                .putString(KEY_EDITION_ID, book.getEditionId()).apply();
                        break;
                    }
                }
            }
        }
        if (id == null) {
            // Old releases could have a bookmark but no continue session.
            SharedPreferences bookmarks = context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE);
            int bestIndex = -1;
            for (Book book : BookStore.shared.getAllBooks(context)) {
                int index = bookmarks.getInt(editionKey(book),
                        bookmarks.getInt(book.getName(), -1));
                if (index > bestIndex) {
                    bestIndex = index;
                    id = book.getCatalogId();
                }
            }
        }
        if (id == null) return null;
        for (Book book : BookStore.shared.getBooks(context)) {
            if (id.equals(book.getCatalogId())) return book;
        }
        return null;
    }

    public static int getContinueChapterIndex(Context context) {
        Book book = getContinueReadingBook(context);
        if (book == null) return 0;
        int bookmark = getChapterIndex(context, book);
        if (bookmark >= 0) return bookmark;
        SharedPreferences prefs = context.getSharedPreferences(CONTINUE_PREFS, Context.MODE_PRIVATE);
        return book.getEditionId().equals(prefs.getString(KEY_EDITION_ID, null))
                ? prefs.getInt(KEY_CHAPTER_INDEX, 0) : 0;
    }

    @Nullable
    public static String getContinueChapterLabel(Context context) {
        Book book = getContinueReadingBook(context);
        if (book == null) return null;
        SharedPreferences prefs = context.getSharedPreferences(CONTINUE_PREFS, Context.MODE_PRIVATE);
        if (book.getEditionId().equals(prefs.getString(KEY_EDITION_ID, null))) {
            String chapterName = prefs.getString(KEY_CHAPTER_NAME, null);
            if (chapterName != null && !chapterName.isEmpty()) return chapterName;
        }
        int index = getChapterIndex(context, book);
        return index >= 0 ? "Chapter " + (index + 1) : null;
    }

    public static int getChapterIndex(Context context, Book book) {
        if (context == null || book == null) return -1;
        migrate(context, book);
        return context.getSharedPreferences(BOOKMARKS_PREFS, Context.MODE_PRIVATE)
                .getInt(editionKey(book), -1);
    }

    public static int getTotalChapters(Context context, Book book) {
        if (context == null || book == null) return 0;
        migrate(context, book);
        return context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE)
                .getInt(totalKey(editionKey(book)), 0);
    }

    public static int getProgressPercent(Context context, Book book) {
        if (context == null || book == null) return -1;
        int chapterIndex = getChapterIndex(context, book);
        if (chapterIndex < 0) return -1;
        int total = getTotalChapters(context, book);
        if (total <= 0) return -1;
        float progress = ((chapterIndex + getScrollFraction(context, book)) / total) * 100f;
        return Math.min(100, Math.max(0, Math.round(progress)));
    }

    @Nullable
    public static String getProgressLabel(Context context, Book book) {
        int percent = getProgressPercent(context, book);
        int chapterIndex = getChapterIndex(context, book);
        if (chapterIndex < 0) return null;
        int total = getTotalChapters(context, book);
        if (percent >= 0 && total > 0) {
            return context.getString(com.appsbay.minguoliteratural.R.string.reading_progress_label,
                    percent, chapterIndex + 1, total);
        }
        return context.getString(com.appsbay.minguoliteratural.R.string.reading_chapter_only,
                chapterIndex + 1);
    }

    public static void bindProgressRow(Context context, Book book,
                                       TextView progressText, ProgressBar progressBar) {
        if (progressText == null || progressBar == null) {
            return;
        }
        String label = getProgressLabel(context, book);
        int percent = getProgressPercent(context, book);
        if (label == null) {
            progressText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return;
        }
        progressText.setVisibility(View.VISIBLE);
        progressText.setText(label);
        progressText.setTextColor(MyColor.getDetailTextColor(context));
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(percent, 0));
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(MyColor.getAccentColor(context)));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(MyColor.getSeparatorColor(context)));
    }

    public static void bindOfflineBadge(Context context, Book book, TextView badge) {
        if (badge != null) {
            badge.setVisibility(View.GONE);
        }
    }

    private static String totalKey(String key) {
        return key + "_total";
    }

    private static String scrollKey(String key) {
        return key + "_scroll";
    }
}

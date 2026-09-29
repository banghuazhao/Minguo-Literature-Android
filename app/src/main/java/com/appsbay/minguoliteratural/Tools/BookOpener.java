package com.appsbay.minguoliteratural.Tools;

import android.content.Context;
import android.content.Intent;

import com.appsbay.minguoliteratural.Controller.BookChapterActivity;
import com.appsbay.minguoliteratural.Controller.BooksListActivity;
import com.appsbay.minguoliteratural.Model.Book;
import com.appsbay.minguoliteratural.Model.BookStore;

public final class BookOpener {
    public static final String EXTRA_BOOK = "book";
    public static final String EXTRA_BOOKS = "books";
    public static final String EXTRA_COLLECTION_BOOK = "collectionBook";
    public static final String EXTRA_CATEGORY_NAME = "categoryName";
    public static final String EXTRA_SEARCH_QUERY = "searchQuery";
    public static final String EXTRA_AUTO_OPEN_CHAPTER = "autoOpenChapter";

    private BookOpener() {
    }

    public static void open(Context context, Book book) {
        if (book == null || context == null) {
            return;
        }
        if (book.isCollection()) {
            Intent intent = new Intent(context, BooksListActivity.class);
            intent.putParcelableArrayListExtra(EXTRA_BOOKS, BookStore.shared.getBooksForCollection(book));
            intent.putExtra(EXTRA_COLLECTION_BOOK, book);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, BookChapterActivity.class);
            intent.putExtra(EXTRA_BOOK, book);
            context.startActivity(intent);
        }
    }

    public static void continueReading(Context context, Book book) {
        if (book == null || context == null) {
            return;
        }
        if (book.isCollection()) {
            open(context, book);
            return;
        }
        Intent intent = new Intent(context, BookChapterActivity.class);
        intent.putExtra(EXTRA_BOOK, book);
        intent.putExtra(EXTRA_AUTO_OPEN_CHAPTER, true);
        context.startActivity(intent);
    }
}

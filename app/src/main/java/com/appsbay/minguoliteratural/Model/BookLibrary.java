package com.appsbay.minguoliteratural.Model;

import android.content.Context;

import com.appsbay.minguoliteratural.Tools.TinyDB;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class BookLibrary {

    public static final String BookLibrary = "BookLibrary";
    private static final String ID_PREFIX = "book:";
    public static BookLibrary shared = new BookLibrary();

    private ArrayList<String> savedIds(Context context) {
        TinyDB tinydb = new TinyDB(context);
        ArrayList<String> stored = tinydb.getListString(BookLibrary);
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (String value : stored) {
            if (value.startsWith(ID_PREFIX)) {
                ids.add(value);
                continue;
            }
            // Older versions saved the display title, which differs by script.
            for (Book book : BookStore.shared.getAllBooks(context)) {
                if (value.equals(book.getName())) {
                    ids.add(ID_PREFIX + book.getCatalogId());
                    break;
                }
            }
        }
        ArrayList<String> migrated = new ArrayList<>(ids);
        if (!migrated.equals(stored)) tinydb.putListString(BookLibrary, migrated);
        return migrated;
    }

    public boolean have(Context context, Book book) {
        return savedIds(context).contains(ID_PREFIX + book.getCatalogId());
    }

    public void save(Context context, Book book) {
        ArrayList<String> ids = savedIds(context);
        if (ids.add(ID_PREFIX + book.getCatalogId())) {
            new TinyDB(context).putListString(BookLibrary, ids);
        }
    }

    public void remove(Context context, Book book) {
        ArrayList<String> ids = savedIds(context);
        if (ids.remove(ID_PREFIX + book.getCatalogId())) {
            new TinyDB(context).putListString(BookLibrary, ids);
        }
    }

    public ArrayList<Book> books(Context context) {
        ArrayList<Book> result = new ArrayList<>();
        for (String id : savedIds(context)) {
            for (Book book : BookStore.shared.getBooks(context)) {
                if (id.equals(ID_PREFIX + book.getCatalogId())) {
                    result.add(book);
                    break;
                }
            }
        }
        return result;
    }

    public void saveOrder(Context context, List<Book> orderedBooks) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (Book book : orderedBooks) ids.add(ID_PREFIX + book.getCatalogId());
        new TinyDB(context).putListString(BookLibrary, new ArrayList<>(ids));
    }
}

package com.appsbay.minguoliteratural.Model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class BookCategoryStore {
    public static BookCategoryStore shared = new BookCategoryStore();

    public ArrayList<BookCategory> getCategories(Context context) {

        ArrayList<BookCategory> categories = new ArrayList<>();

        ArrayList<Book> shenCongWen = new ArrayList<>();
        ArrayList<Book> luXun = new ArrayList<>();
        ArrayList<Book> zhangHenShui = new ArrayList<>();
        ArrayList<Book> baJin = new ArrayList<>();
        ArrayList<Book> other = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences("Language Preference", Context.MODE_PRIVATE);
        int language = preferences.getInt("language", 0);

        if (language == 0) {
            for (Book book : BookStore.shared.getBooks(context)) {
                if (book.getBookType() == BookType.shenCongWen) {
                    shenCongWen.add(book);
                } else if (book.getBookType() == BookType.luXun) {
                    luXun.add(book);
                } else if (book.getBookType() == BookType.zhangHenShui) {
                    zhangHenShui.add(book);
                } else if (book.getBookType() == BookType.baJin) {
                    baJin.add(book);
                } else if (book.getBookType() == BookType.other) {
                    other.add(book);
                }
            }
            categories.add(new BookCategory("沈从文", shenCongWen));
            categories.add(new BookCategory("鲁迅", luXun));
            categories.add(new BookCategory("张恨水", zhangHenShui));
            categories.add(new BookCategory("巴金", baJin));
            categories.add(new BookCategory("其他", other));
        } else {
            for (Book book : BookStore.shared.getBooks(context)) {
                if (book.getBookType() == BookType.shenCongWen_Fan) {
                    shenCongWen.add(book);
                } else if (book.getBookType() == BookType.luXun_Fan) {
                    luXun.add(book);
                } else if (book.getBookType() == BookType.zhangHenShui_Fan) {
                    zhangHenShui.add(book);
                } else if (book.getBookType() == BookType.baJin_Fan) {
                    baJin.add(book);
                } else if (book.getBookType() == BookType.other_Fan) {
                    other.add(book);
                }
            }
            categories.add(new BookCategory("沈從文", shenCongWen));
            categories.add(new BookCategory("魯迅", luXun));
            categories.add(new BookCategory("張恨水", zhangHenShui));
            categories.add(new BookCategory("巴金", baJin));
            categories.add(new BookCategory("其他", other));
        }

        return categories;
    }

    public void setCategories(ArrayList<BookCategory> categories) {
        this.categories = categories;
    }

    public ArrayList<BookCategory> categories;

}

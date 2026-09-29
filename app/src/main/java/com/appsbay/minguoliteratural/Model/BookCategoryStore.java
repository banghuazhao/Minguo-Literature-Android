package com.appsbay.minguoliteratural.Model;

import android.content.Context;
import java.util.ArrayList;

public class BookCategoryStore {
    public static final BookCategoryStore shared = new BookCategoryStore();
    public ArrayList<BookCategory> categories;

    public ArrayList<BookCategory> getCategories() {
        ArrayList<Book> shen = new ArrayList<>();
        ArrayList<Book> lu = new ArrayList<>();
        ArrayList<Book> zhang = new ArrayList<>();
        ArrayList<Book> ba = new ArrayList<>();
        ArrayList<Book> other = new ArrayList<>();
        for (Book book : BookStore.shared.books) {
            switch (book.getBookType()) {
                case shenCongWen:
                case shenCongWen_Fan: shen.add(book); break;
                case luXun:
                case luXun_Fan: lu.add(book); break;
                case zhangHenShui:
                case zhangHenShui_Fan: zhang.add(book); break;
                case baJin:
                case baJin_Fan: ba.add(book); break;
                default: other.add(book); break;
            }
        }
        ArrayList<BookCategory> result = new ArrayList<>();
        result.add(new BookCategory(BookGenres.ROMANCE, shen));
        result.add(new BookCategory(BookGenres.REALISTIC, lu));
        result.add(new BookCategory(BookGenres.FANTASY, zhang));
        result.add(new BookCategory(BookGenres.GOTHIC, ba));
        result.add(new BookCategory(BookGenres.CRIME, other));
        return result;
    }

    public ArrayList<BookCategory> getCategories(Context context) {
        BookStore.shared.updateLanguage(context);
        return getCategories();
    }

    public void setCategories(ArrayList<BookCategory> categories) {
        this.categories = categories;
    }
}

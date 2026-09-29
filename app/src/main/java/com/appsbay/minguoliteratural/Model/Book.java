package com.appsbay.minguoliteratural.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    String name;
    String author;
    String fileName;
    String imageName;
    BookType bookType;

    public Book(String imageName, String name, String author, String fileName, BookType bookType) {
        this.imageName = imageName;
        this.name = name;
        this.author = author;
        this.fileName = fileName;
        this.bookType = bookType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImageName() {
        return imageName;
    }

    /** Stable work ID shared by both scripts. Keep this value when changing cover artwork. */
    public String getCatalogId() {
        return imageName;
    }

    /** A reading position belongs to one text edition. */
    public String getEditionId() {
        return imageName + (bookType.name().endsWith("_Fan") ? ":hant" : ":hans");
    }

    // The newer reader calls this the cover; Minguo keeps its original asset names.
    public String getBookCover() {
        return imageName;
    }

    public boolean isOnline() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public String getParent() {
        return null;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public BookType getBookType() {
        return bookType;
    }

    public void setBookType(BookType bookType) {
        this.bookType = bookType;
    }


    protected Book(Parcel in) {
        name = in.readString();
        author = in.readString();
        fileName = in.readString();
        imageName = in.readString();
        bookType = (BookType) in.readValue(BookType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(fileName);
        dest.writeString(imageName);
        dest.writeValue(bookType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}

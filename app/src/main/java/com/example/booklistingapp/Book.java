package com.example.booklistingapp;

import android.graphics.Bitmap;

import java.util.Date;
import java.util.List;

public class Book {
    private String title;
    private List<String> authors;
    private String description;
    private String imageURL;
    private Date publishedDate;
    private int pageCount;

    public static final int INT_FIELD_NOT_PROVIDED = -1;

    public Book(String title, List<String> authors, String description, Date publishedDate, int pageCount, String imageURL) {
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.publishedDate = publishedDate;
        this.pageCount = pageCount;
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public int getPageCount() {
        return pageCount;
    }
}

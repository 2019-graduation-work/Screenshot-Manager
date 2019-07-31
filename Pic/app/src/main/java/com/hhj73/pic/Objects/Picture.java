package com.hhj73.pic.Objects;

import java.util.Date;

public class Picture { // 사진
    String path; // 경로
    String contents; // 추출한 contents
    Date date;
    int category = UNKNOWN;

    /* categories */
    static final int UNKNOWN = 1;
    static final int TRAVEL = 2;
    static final int DISCOUNT = 3;
    static final int SCHOOL = 4;

    public Picture() {
    }

    public Picture(String path, Date date) {
        this.path = path;
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}

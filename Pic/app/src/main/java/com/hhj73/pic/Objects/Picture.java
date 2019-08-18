package com.hhj73.pic.Objects;

public class Picture { // 사진
    String path; // 경로
    String contents; // 추출한 contents
    String date;
    int category = 0;

    /* categories */
    static final String[] names = {
            "unknown", "travel", "food",
            "discount", "finance", "school",
            "beauty", "work", "music"
    };

    public Picture() {
    }

    public Picture(String path, String date) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}

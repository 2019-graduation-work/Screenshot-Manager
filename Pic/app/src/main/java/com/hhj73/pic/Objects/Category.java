package com.hhj73.pic.Objects;

import java.util.ArrayList;

public class Category { // 분류될 카테고리
    int num; // 카테고리 번호
    String name; // 카테고리 이름
    ArrayList<Picture> pictures;

    public Category(int num, String name) {
        this.num = num;
        this.name = name;
        this.pictures = new ArrayList<>();
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }
}

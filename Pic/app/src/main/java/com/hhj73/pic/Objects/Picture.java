package com.hhj73.pic.Objects;

import java.io.Serializable;

public class Picture /*implements Comparable<Picture>*/ implements Serializable { // 사진
    String path; // 경로
    String contents; // 추출한 contents
    String date;
    int category = 0;
    String keyword; // 키워드

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    /* categories */
//    public static final String[] names = {
//            "unknown", "travel", "food",
//            "discount", "finance", "school",
//            "beauty", "work", "music"
//    };

    public static final String[][] names = {
            {"unknown"},
            {"food", "restaurant", "recipe", "cafe"},
            {"cosmetic", "discount", "skincare", "color"},
            {"travel", "place", "exchange", "transport"}
    };

    public static final String[][] names_kor = {
            {"기타"},
            {"음식", "맛집", "레시피", "카페/디저트"},
            {"화장품", "할인", "기초", "색조"},
            {"여행", "관광지", "환전", "할인", "교통"}
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

//    @Override
//    public int compareTo(@NonNull Picture p) {
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            Date thisDate = df.parse(this.date);
//            Date pDate = df.parse(p.getDate());
//
//            // 자신이 크면 양수, 작으면 음수 리턴
//            return thisDate.compareTo(pDate);
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }

}

package com.hhj73.pic.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.hhj73.pic.Objects.Picture;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sb = new StringBuffer();
        sb.append(" CREATE TABLE DATA ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, "); // 번호
        sb.append(" PATH TEXT, "); // 이미지 경로
        sb.append(" CONTENTS TEXT, "); // 추출한 텍스트
        sb.append(" DATE TEXT, "); // 저장 날짜
        sb.append(" CATEGORY TEXT ) "); // 카테고리

        // SQLite Database로 쿼리 실행
        db.execSQL(sb.toString());
        Toast.makeText(context, "Table 생성완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
        0: path
        1: contents
        2: date
        3: category
     */

    public void insertData(Picture picture) {
        // Picture 데이터 삽입
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO DATA ( ");
        sb.append(" PATH, COTENTS, DATE ) "); // Category는 나중에
        sb.append(" VALUES ( ?, ?, ? ) ");
    }

    public List getAllData() {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT _ID, NAME, AGE, PHONE FROM TEST_TABLE ");

        // 읽기 전용 DB 객체를 만든다.
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sb.toString(), null);
        List allData = new ArrayList();
        Picture picture  = null;

        // moveToNext 다음에 데이터가 있으면 true 없으면 false
        while( cursor.moveToNext() ) {
            picture = new Picture();
            picture.setPath(cursor.getString(0));
            picture.setContents(cursor.getString(1));
            picture.setDate(cursor.getString(2));
            picture.setCategory(cursor.getInt(3));
            allData.add(picture);
        }

        cursor.close();
        return allData;
    }

}

package com.hhj73.pic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.Objects.Picture;

import java.io.File;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    DBHelper dbHelper;
    ArrayList<Picture> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initToolbar();
        init();
    }

    public void initToolbar() {
        // 툴바
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.myAppName);
        setSupportActionBar(toolbar);

        // 검색
        final EditText editSearch = (EditText) findViewById(R.id.editSearch);
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch(actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                        intent.putExtra("input", editSearch.getText().toString());
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void init() {
        Intent intent = getIntent();
        String input = intent.getStringExtra("input");
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();

        // DB에서 검색
        dbHelper = new DBHelper(this, "data", null, 1);
        result = (ArrayList<Picture>) dbHelper.getSearchingData(input);

        // 레이아웃 동적으로 생성
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        int total = result.size();
        int col = 3;
        int row = total / col + 1;
        gridLayout.setColumnCount(col);
        gridLayout.setRowCount(row);

        for(int i=0, c=0, r=0; i<result.size(); i++, c++) {
            if(c == col) {
                c = 0;
                r++;
            }

            File file = new File(result.get(i).getPath());
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(500, 500));

            if(file.exists()) { // 파일 있으면
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
            else { // 파일 없으면
                imageView.setBackgroundResource(R.drawable.not_found);
            }

            // 클릭하면 상세보기
            final int index = i;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(getApplicationContext(), ImageViewActivity.class);
                    intent1.putExtra("picture", result.get(index));
                    startActivity(intent1);
                }
            });
            gridLayout.addView(imageView);
        }
    }

}

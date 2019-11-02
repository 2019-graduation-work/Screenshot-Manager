package com.hhj73.pic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.LayoutObjects.SubLayout;
import com.hhj73.pic.Objects.Category;
import com.hhj73.pic.Objects.Picture;

import java.util.ArrayList;

public class SubCategoryActivity extends AppCompatActivity {
    int value;
    String categoryName;
    Category category;

    DBHelper dbHelper;
    ArrayList<Picture> pictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(toolbar_menu);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.search:
                // Do something
                LinearLayout searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
                if(searchLayout.getVisibility() == View.INVISIBLE) {
                    searchLayout.setVisibility(View.VISIBLE);
                }
                else if(searchLayout.getVisibility() == View.VISIBLE) {
                    searchLayout.setVisibility(View.INVISIBLE);
                }
                return true;
            case R.id.option :
                // Do something
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void init() {
        Intent intent = getIntent();
        value = intent.getIntExtra("category", 0);

        final int main = value/10; // 대분류 (10의 자리)

        categoryName = Picture.names[main][0]; // 대분류
        Toast.makeText(this, categoryName, Toast.LENGTH_SHORT).show();

        // 레이아웃 동적으로 생성
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        int total = Picture.names[main].length; // 소분류 개수
        int col = 3;
        int row = total / col + 1;
        gridLayout.setColumnCount(col);
        gridLayout.setRowCount(row);

        for(int i=0, c=0, r=0; i<total-1; i++, c++) {
            if(c == col) {
                c = 0;
                r++;
            }

            /*
                     <LinearLayout
            android:id="@+id/food"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical">
            <ImageButton
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:layout_columnWeight="1"
                android:background="@drawable/folder_icon"
                android:layout_margin="10dp"
                android:clickable="false"/>
            <TextView
                android:text="음식"
                android:layout_gravity="center"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:clickable="false"/>
        </LinearLayout>


            * */
            SubLayout subLayout = new SubLayout(getApplicationContext());
            subLayout.setId(main + i + 1);

            TextView tv = subLayout.findViewById(R.id.subCategoryName);
            tv.setText(Picture.names_kor[main][i+1]);

            final int index = i + 1;
            subLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(getApplicationContext(), DirectoryActivity.class);
                    int subValue = main*10 + index;
                    intent1.putExtra("category", subValue);
                    startActivity(intent1);
                }
            });

            gridLayout.addView(subLayout);
        }
    }


}

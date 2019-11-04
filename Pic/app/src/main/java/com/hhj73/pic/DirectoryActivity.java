package com.hhj73.pic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.LayoutObjects.MyAdapter;
import com.hhj73.pic.Objects.Category;
import com.hhj73.pic.Objects.Picture;

import java.util.ArrayList;

public class DirectoryActivity extends AppCompatActivity {

    int value;
    String categoryName;
    Category category;

    DBHelper dbHelper;
    ArrayList<Picture> pictures;

    String TAG = "ㅇㅅㅇ";

    // View
    Context mContext;
    RelativeLayout mRelativeLayout;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        Log.d(TAG, "안냐세염");
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
        value = intent.getIntExtra("category", 0);

        Log.d(TAG, "value: "+String.valueOf(value));
        int main = value/10; // 대분류 (10의 자리)
        int sub = value%10; // 소분류 (1의 자리)
//        categoryName = Picture.names[value];
        Log.d(TAG, "main: "+String.valueOf(main));
        Log.d(TAG, "sub: "+String.valueOf(sub));

        categoryName = Picture.names[main][sub];
//        Toast.makeText(this, categoryName, Toast.LENGTH_SHORT).show();

        // 이미지 로드
        dbHelper = new DBHelper(this, "data", null, 1);
        pictures = (ArrayList<Picture>) dbHelper.getCategoryData(value);

        // 레이아웃
        mContext = getApplicationContext();
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(mContext, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(pictures,  mContext);
        mRecyclerView.setAdapter(mAdapter);

        // 레이아웃 동적으로 생성
//        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
//        int total = pictures.size();
//        int col = 2;
//        int row = total / col + 1;
//        gridLayout.setColumnCount(col);
//        gridLayout.setRowCount(row);
//
//        for(int i=0, c=0, r=0; i<pictures.size(); i++, c++) {
//            if(c == col) {
//                c = 0;
//                r++;
//            }
//
//            ImageListLayout layout = new ImageListLayout(getApplicationContext());
//            layout.setId(main + i + 1);
//
//            ImageView imageView = layout.findViewById(R.id.image);
//            if(imageView == null) {
//                Log.d(TAG, "엥?");
//            }
//            File file = new File(pictures.get(i).getPath());
//
//            if(file.exists()) { // 파일 있으면
//                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                imageView.setImageBitmap(bitmap);
//            }
//            else { // 파일 없으면
//                imageView.setBackgroundResource(R.drawable.not_found);
//            }
//
//            // 클릭하면 상세보기
//            final int index = i;
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent1 = new Intent(getApplicationContext(), ImageViewActivity.class);
//                    intent1.putExtra("picture", pictures.get(index));
//                    startActivity(intent1);
//                }
//            });
//            gridLayout.addView(layout);
//        }
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

}

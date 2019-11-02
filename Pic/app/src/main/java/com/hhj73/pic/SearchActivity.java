package com.hhj73.pic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.LayoutObjects.MyAdapter;
import com.hhj73.pic.Objects.Picture;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    DBHelper dbHelper;
    ArrayList<Picture> result;

    // View
    Context mContext;
    RelativeLayout mRelativeLayout;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;


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

        // 레이아웃
        mContext = getApplicationContext();
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(mContext, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(result,  mContext);
        mRecyclerView.setAdapter(mAdapter);


        // 레이아웃 동적으로 생성
//        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
//        int total = result.size();
//        int col = 3;
//        int row = total / col + 1;
//        gridLayout.setColumnCount(col);
//        gridLayout.setRowCount(row);
//
//        for(int i=0, c=0, r=0; i<result.size(); i++, c++) {
//            if(c == col) {
//                c = 0;
//                r++;
//            }
//
//            File file = new File(result.get(i).getPath());
//            ImageView imageView = new ImageView(this);
//            imageView.setLayoutParams(new ViewGroup.LayoutParams(500, 500));
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
//                    intent1.putExtra("picture", result.get(index));
//                    startActivity(intent1);
//                }
//            });
//            gridLayout.addView(imageView);
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

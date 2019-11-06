package com.hhj73.pic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hhj73.pic.LayoutObjects.ImageInformationAdapter;
import com.hhj73.pic.Objects.Picture;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageInformationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

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
        Picture picture = (Picture) intent.getSerializableExtra("picture");

//        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
//        TextView contentsTextView = (TextView) findViewById(R.id.contentsTextView);

//        titleTextView.setText(picture.getDate());
//        contentsTextView.setText(picture.getContents());
//        contentsTextView.setMovementMethod(new ScrollingMovementMethod());

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ImageInformationAdapter();
        recyclerView.setAdapter(adapter);

        adapter.addItem(picture);
        adapter.notifyDataSetChanged();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        File file = new File(picture.getPath());

        if(file.exists()) { // 파일 있으면
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
        else { // 파일 없으면
            imageView.setBackgroundResource(R.drawable.not_found);
        }
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

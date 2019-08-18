package com.hhj73.pic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DirectoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        // 툴바
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.myAppName);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        int value = intent.getIntExtra("category", 1);
        Toast.makeText(this, String.valueOf(value), Toast.LENGTH_SHORT).show();
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

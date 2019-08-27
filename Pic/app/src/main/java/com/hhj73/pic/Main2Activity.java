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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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
        // layout
        LinearLayout[] directories = {
                findViewById(R.id.unknown), findViewById(R.id.travel), findViewById(R.id.food),
                findViewById(R.id.discount), findViewById(R.id.finance), findViewById(R.id.school),
                findViewById(R.id.beauty), findViewById(R.id.work), findViewById(R.id.music)
        };

        View.OnClickListener listner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.unknown: // 0. unknown
                        Toast.makeText(Main2Activity.this, "0", Toast.LENGTH_SHORT).show();
                        switchActivity(0);
                        break;
                    case R.id.travel: // 1. travel
                        switchActivity(1);
                        break;
                    case R.id.food: // 2. food
                        switchActivity(2);
                        break;
                    case R.id.discount: // 3. discount
                        switchActivity(3);
                        break;
                    case R.id.finance: // 4. finance
                        switchActivity(4);
                        break;
                    case R.id.school: // 5. school
                        switchActivity(5);
                        break;
                    case R.id.beauty: // 6. beauty
                        switchActivity(6);
                        break;
                    case R.id.work: // 7. work
                        switchActivity(7);
                        break;
                    case R.id.music: // 8. music
                        switchActivity(8);
                        break;
                }
            }
        };


        for(int i=0; i<directories.length; i++) {
            directories[i].setOnClickListener(listner);
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

    public void setListener() {


    }

    public void switchActivity(int value) {
        Toast.makeText(this, "switch"+value, Toast.LENGTH_SHORT).show();
        /*
        static final int UNKNOWN = 0;
        static final int TRAVEL = 1;
        static final int FOOD = 2;
        static final int DISCOUNT = 3;
        static final int FINANCE = 4;
        static final int SCHOOL = 5;
        static final int BEUATY = 6;
        static final int WORK = 7;
        static final int MUSIC = 8;
         */

        Intent intent = new Intent(this, DirectoryActivity.class);
        intent.putExtra("category", value);
        startActivity(intent);
    }
}

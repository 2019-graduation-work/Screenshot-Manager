package com.hhj73.pic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SavePictureActivity extends AppCompatActivity {

    String rootSD, galleryPath;
    TextView textView;
    ListView fileListView;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MULTIPLE_PERMISSIONS = 12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_picture);

        init();
    }

    public void init() {
        // 권한 설정
        checkPermissions();

        galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(galleryPath + "/Screenshots");

        rootSD = Environment.getExternalStorageDirectory().toString();

        File directory = new File(galleryPath + "/Screenshots");
        File[] files = directory.listFiles();

        List<String> fileList = new ArrayList<>();

        for(int i=0; i<files.length; i++) {
            Toast.makeText(this, files[i].getName(), Toast.LENGTH_SHORT).show();
            fileList.add(files[i].getName());
        }

        fileListView = (ListView) findViewById(R.id.fileListView);
        fileListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList));


    }

    public void btnClicked(View view) {

    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();

        for(int i=0; i<permissions.length; i++) {
            result = ContextCompat.checkSelfPermission(this, permissions[i]);
            if(result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }

        if(!permissionList.isEmpty()) {
            // 확보해야할 권한이 있다
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if(grantResults.length > 0) {
                    for(int i=0; i<permissions.length; i++) {
                        if(permissions[i].equals(this.permissions[i])) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(this, "권한 요청에 동의하지 않으면 이용이 불가합니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                }
                else {
                    Toast.makeText(this, "권한 요청에 동의하지 않으면 이용이 불가합니다.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}

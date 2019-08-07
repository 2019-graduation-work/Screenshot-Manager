package com.hhj73.pic;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SavePictureActivity extends AppCompatActivity {

    String galleryPath;
    TextView textView;
    ListView fileListView;

    private SharedPreferences sp;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MULTIPLE_PERMISSIONS = 12345;

    File[] files;
    List<String> fileList;

    // OCR
    TessBaseAPI tessBaseAPI;
    String lang = "kor";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        if(!OpenCVLoader.initDebug()) {
        } else {
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_picture);

        init();
    }

    public void init() {
        // 초기화
        galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Screenshots";
        textView = (TextView) findViewById(R.id.textView);
        fileListView = (ListView) findViewById(R.id.fileListView);
        fileList = new ArrayList<>();
        fileListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList));
        File directory = new File(galleryPath);
        files = directory.listFiles();
//        textView.setText(galleryPath);
        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir/*+"/tessdata"*/)) {
            tessBaseAPI.init(dir, lang);
        }


        // 최근 처리 날짜 가져오기
        sp = getSharedPreferences("processedDate", Activity.MODE_PRIVATE);
        String processedDate = sp.getString("date", "null"); // date라는 키에 저장된 값이 있는지 확인, 없으면 null
        Toast.makeText(this, processedDate, Toast.LENGTH_SHORT).show();

        
        // 권한 설정
        checkPermissions();

        for(int i=0; i<files.length; i++) {
            fileList.add(files[i].getName());
        }

        try {
            checkImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkImage() throws IOException {
        // 최근 처리 날짜 가져오기
        sp = getSharedPreferences("processedInfo", Activity.MODE_PRIVATE);
        String processedDate = sp.getString("date", "null"); // date라는 키에 저장된 값이 있는지 확인, 없으면 null
        String processedPath = sp.getString("path", "null"); // path라는 키에 저장된 값이 있는지 확인, 없으면 null

        SharedPreferences.Editor editor = sp.edit();
        // 사용법

        if(processedDate.equals("null")) {
            // 처리한 것이 없음 -> 전체 처리해야함
            for(int i=0; i<1/*fileList.size()*/; i++) {
                String path = fileList.get(i);
                path = galleryPath + "/" + path;

                Bitmap img = BitmapFactory.decodeFile(path); // 이미지 가져오기
                ExifInterface exif = new ExifInterface(path); // 이미지 메타데이터
                String attrDate = exif.getAttribute(ExifInterface.TAG_DATETIME);

                // 스크린샷 이미지는 datetime 속성이 없어서 file의 속성으로 접근
                File file = new File(path);
                BasicFileAttributes attrs;
                attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                FileTime time = attrs.creationTime();

                String pattern = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String creationTime = simpleDateFormat.format(new Date(time.toMillis()));

                // 이미지 전처리
                processImage(img);

                // OCR 처리
                try {
//                    InputStream in = getContentResolver().openInputStream(data.getData());
//                    img = BitmapFactory.decodeStream(in);
                    Toast.makeText(this, "처리하니?", Toast.LENGTH_SHORT).show();
                    int width = img.getWidth();
                    int height = img.getHeight();

                    if(width > 2000 || height > 2000) { // 큰 이미지 사이즈 줄임
                        int rate = width / 1080;
                        img = Bitmap.createScaledBitmap(img, width/rate, height/rate, true);
                    }
//                    in.close();

                    tessBaseAPI.setImage(img);
                    String result = tessBaseAPI.getUTF8Text();

                    Toast.makeText(this, "result: " + result, Toast.LENGTH_SHORT).show();
                    
                    // sp editor에 입력
                    editor.putString("date", creationTime);
                    editor.putString("path", path);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        Toast.makeText(this, "dd", Toast.LENGTH_SHORT).show();
    }

    public void processImage(Bitmap img) {
        // 1. grayscale
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);
        Mat gray = new Mat();

        grayScaleJNI(src.getNativeObjAddr(), gray.getNativeObjAddr());

        // 2. binarization
        Mat bin = new Mat();

        binarizationJNI(gray.getNativeObjAddr(), bin.getNativeObjAddr());

        Utils.matToBitmap(bin, img);

        // OCR
        tessBaseAPI.setImage(img);
        String result = tessBaseAPI.getUTF8Text();
    }

    boolean checkLanguageFile(String dir) {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/tessdata/" + lang + ".traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    private void createFiles(String dir) {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("tessdata/" + lang + ".traineddata");

            String destFile = dir + "/tessdata/" + lang + ".traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void grayScaleJNI(long inputImage, long outputImage);
    public native void binarizationJNI(long inputImage, long outputImage);

}

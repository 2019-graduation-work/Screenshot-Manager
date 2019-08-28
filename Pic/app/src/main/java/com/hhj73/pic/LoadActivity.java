package com.hhj73.pic;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.Objects.Category;
import com.hhj73.pic.Objects.Picture;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class LoadActivity extends AppCompatActivity {
    ArrayList<Category> categories;

    String galleryPath;
    TextView textView;
    ListView fileListView;
    ArrayAdapter adapter;

    DBHelper dbHelper;

    private static final String TAG = "ㅎㅇ";

    private SharedPreferences sp;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MULTIPLE_PERMISSIONS = 12345;

    File[] files;
    List<String> fileList;
    List<Picture> pictures;

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
        setContentView(R.layout.activity_load);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        // 카테고리 객체
        categories = new ArrayList<>();
        String[] names = {
                "unknown", "travel", "food",
                "discount", "finance", "school",
                "beauty", "work", "music"
        };

        for(int i=0; i<names.length; i++) {
            categories.add(new Category(i, names[i]));
        }

        // 초기화
        galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/sample";
        textView = (TextView) findViewById(R.id.textView);
        fileListView = (ListView) findViewById(R.id.fileListView);
        fileList = new ArrayList<>();
        pictures = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileList);
        fileListView.setAdapter(adapter);
        File directory = new File(galleryPath);
        files = directory.listFiles();
//        textView.setText(galleryPath);
        dbHelper = new DBHelper(this, "data", null, 1);

        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir/*+"/tessdata"*/)) {
            tessBaseAPI.init(dir, lang);
        }

        // 권한 설정
        checkPermissions();

        for(int i=0; i<files.length; i++) {
            //            fileList.add(files[i].getName()); // 파일 이름 저장
            String path = galleryPath + "/" + files[i].getName();

            // 스크린샷 이미지는 datetime 속성이 없어서 file의 속성으로 접근
            BasicFileAttributes attrs;
            attrs = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
            FileTime time = attrs.creationTime();

            String pattern = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String creationTime = simpleDateFormat.format(new Date(time.toMillis()));

            Picture p = new Picture(path, creationTime);
            pictures.add(p);
            fileList.add(p.getPath() + "\n" + p.getDate());
        }
        // pictures 배열 정렬
        Collections.sort(pictures, new Comparator<Picture>() {
            @Override
            public int compare(Picture p1, Picture p2) {
                return p1.getDate().compareTo(p2.getDate());
            }
        });

        adapter.notifyDataSetChanged();
        try {
            checkImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkImage() throws IOException {
        // 최근 처리 날짜 가져오기
        Toast.makeText(this, "최근 처리 날짜 가져오기", Toast.LENGTH_SHORT).show();
        sp = getApplicationContext().getSharedPreferences("process", Activity.MODE_PRIVATE);
        String processedDate = sp.getString("date", "null"); // date라는 키에 저장된 값이 있는지 확인, 없으면 null
        String processedPath = sp.getString("path", "null"); // path라는 키에 저장된 값이 있는지 확인, 없으면 null

        SharedPreferences.Editor editor = sp.edit();

        if(processedDate.equals("null")) {
            // 처리한 것이 없음 -> 전체 처리해야함
//            processFile(0);
            for(int i=0; i<5/*pictures.size()*/; i++) {
                Log.d(TAG, "====================");

                Picture picture = pictures.get(i);
                String path = picture.getPath();
                Log.d(TAG, "path: " + path);


                Bitmap img = BitmapFactory.decodeFile(path); // 이미지 가져오기

                // 이미지 메타데이터 - Screenshot 아닌 다른 사진에 적용
//            ExifInterface exif = new ExifInterface(path);
//            String attrDate = exif.getAttribute(ExifInterface.TAG_DATETIME);

                String creationTime = picture.getDate();

                // 이미지 전처리
                processImage(img);

                // OCR 처리
                try {
//                    InputStream in = getContentResolver().openInputStream(data.getData());
//                    img = BitmapFactory.decodeStream(in);
                    int width = img.getWidth();
                    int height = img.getHeight();

                    if(width > 2000 || height > 2000) { // 큰 이미지 사이즈 줄임
                        int rate = width / 1080;
                        img = Bitmap.createScaledBitmap(img, width/rate, height/rate, true);
                    }
//                    in.close();

                    tessBaseAPI.setImage(img);
                    String result = tessBaseAPI.getUTF8Text();
                    picture.setContents(result);
                    textView.setText(result);

                    // sp editor에 입력
                    editor.putString("date", creationTime);
//                    sp.edit().putString("date", creationTime);
                    editor.putString("path", path);
//                    sp.edit().putString("path", path);
                    editor.commit();
                    editor.commit();

                    // 분류 작업 여기에
                    // ........ 일단 카테고리 랜덤으로
                    Random random = new Random();
                    int rand = random.nextInt(9); // 0~8
                    picture.setCategory(rand);

                    // DB에 저장
                    dbHelper.insertData(picture);
                    Log.d(TAG, "ㅇㅇ");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        else { // 처리한 내역이 있으면 그 이후부터 처리함
            Toast.makeText(this, "처리한 내역 존재", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "처리한 내역 존재");
//            int index = getIndex(processedDate, processedPath);
//            int index = binarySearchIndex(processedDate, 0, pictures.size());
            int index = 0;
            try {
                index = binarySearchIndex(processedDate) + 1; // 이전에 처리했던 이미지 다음 인덱스
                for(int i=index; i<pictures.size(); i++) {
                    Log.d(TAG, "====================");

                    Picture picture = pictures.get(i);
                    if(picture == null )
                        break;

                    String path = picture.getPath();
                    Log.d(TAG, "path: " + path);


                    Bitmap img = BitmapFactory.decodeFile(path); // 이미지 가져오기

                    // 이미지 메타데이터 - Screenshot 아닌 다른 사진에 적용
//            ExifInterface exif = new ExifInterface(path);
//            String attrDate = exif.getAttribute(ExifInterface.TAG_DATETIME);

                    String creationTime = picture.getDate();

                    // 이미지 전처리
                    img = processImage(img);

                    // OCR 처리
                    try {
                        Log.d(TAG, "OCR 처리");

//                    InputStream in = getContentResolver().openInputStream(data.getData());
//                    img = BitmapFactory.decodeStream(in);
                        int width = img.getWidth();
                        int height = img.getHeight();

                        if(width > 2000 || height > 2000) { // 큰 이미지 사이즈 줄임
                            int rate = width / 1080;
                            img = Bitmap.createScaledBitmap(img, width/rate, height/rate, true);
                        }
//                    in.close();

                        tessBaseAPI.setImage(img);
                        String result = tessBaseAPI.getUTF8Text();
                        Log.d(TAG, "result: "+result);

                        picture.setContents(result);
                        textView.setText(result);

                        // sp editor에 입력
                        Log.d(TAG, "sp editor에 입력"+result);
                        editor.putString("date", creationTime);
//                    sp.edit().putString("date", creationTime);
                        editor.putString("path", path);
//                    sp.edit().putString("path", path);
                        editor.commit();
                        editor.commit();

                        // 분류 작업 여기에
                        // ........ 일단 카테고리 랜덤으로
                        Random random = new Random();
                        int rand = random.nextInt(9); // 0~8
                        picture.setCategory(rand);

                        // DB에 저장
                        dbHelper.insertData(picture);
                        Log.d(TAG, "ㅇㅇ");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
//                processFile(index);
//                textView.setText(processedDate + "\n" + processedPath + "\n" + String.valueOf(index));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public int binarySearchIndex(String processDate) throws ParseException {
        int l = 0;
        int r = pictures.size();
        int m = (l + r) / 2;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while(r >= l) {
            m = (l + r) / 2;

            Date mDate = df.parse(pictures.get(m).getDate());
            Date pDate = df.parse(processDate);

            if (mDate.compareTo(pDate) > 0) {
                // mDate가 pDate 이후
                r = m - 1;
            }
            else if (mDate.compareTo(pDate) < 0) {
                // mDate가 pDate 이전
                l = m + 1;
            }
            else if (pictures.get(m).getDate().equals(processDate)) {
                return m;
            }
            else {
                return m;
            }
        }
        return m;
    }


    public Bitmap processImage(Bitmap img) {
        Log.d(TAG, "processImage");

        // 1. grayscale
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);
        Mat gray = new Mat();

        grayScaleJNI(src.getNativeObjAddr(), gray.getNativeObjAddr());

        // 2. binarization
        Mat bin = new Mat();

        binarizationJNI(gray.getNativeObjAddr(), bin.getNativeObjAddr());

        Utils.matToBitmap(bin, img);
        return img;
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

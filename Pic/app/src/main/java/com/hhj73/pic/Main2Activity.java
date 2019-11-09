package com.hhj73.pic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hhj73.pic.DB.DBHelper;
import com.hhj73.pic.Objects.Picture;
import com.twitter.penguin.korean.KoreanTokenJava;
import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import scala.collection.Seq;


public class Main2Activity extends AppCompatActivity {

    String galleryPath;
    DBHelper dbHelper;

    private static final String TAG = "ㅎㅇ";
    private static final String TAG2 = "진행률";

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

    // classification model
    private TextClassificationClient client;
    Handler handler;
    boolean loaded = false;
//    int categoryNumber = 0;

    // progressBar
    ProgressBar progress;
    double processRate = 0;

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
        setContentView(R.layout.activity_main2);

        initToolbar();
        init();
//        try {
//            loadInit();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadInit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void loadInit() throws IOException {
        // 초기화

        // for ocr
        galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/sample";
        fileList = new ArrayList<>();
        pictures = new ArrayList<>();
        File directory = new File(galleryPath);
        files = directory.listFiles();
        dbHelper = new DBHelper(this, "data", null, 1);

        tessBaseAPI = new TessBaseAPI();
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?〉〈′、`』"); // 비추천 글자

        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir/*+"/tessdata"*/)) {
            tessBaseAPI.init(dir, lang);
        }

        // 이미지 처리~
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

        try {
            checkImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkImage() throws IOException {
        // 최근 처리 날짜 가져오기
        sp = getApplicationContext().getSharedPreferences("process", Activity.MODE_PRIVATE);
        String processedDate = sp.getString("date", "null"); // date라는 키에 저장된 값이 있는지 확인, 없으면 null
        String processedPath = sp.getString("path", "null"); // path라는 키에 저장된 값이 있는지 확인, 없으면 null

        SharedPreferences.Editor editor = sp.edit();

        if(processedDate.equals("null")) {
            // 처리한 것이 없음 -> 전체 처리해야함
            for(int i=0; i<pictures.size(); i++) {
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
                img = processImage(img);

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
                    result = result.replaceAll("!\"#[$]%&\\(\\)\\{\\}@`[*]:[+];-.<>,\\^~|'\\[\\]", "");
                    Log.d(TAG, "result: "+result);

                    picture.setContents(result);

                    // 분류 작업 여기에
                    List<String> nouns = processText(result);
                    String[] inputString = nouns.toArray(new String[nouns.size()]);
                    picture.setKeyword(Arrays.toString(inputString));
                    classify(inputString, picture);
//                    picture.setCategory(categoryNumber);
//                    Log.d(TAG, "categoryNumber: " + categoryNumber);

//                    // DB에 저장
//                    dbHelper.insertData(picture);
//                    Log.d(TAG, "db에 저장했음");

                    // sp editor에 입력
                    editor.putString("date", creationTime);
//                    sp.edit().putString("date", creationTime);
                    editor.putString("path", path);
//                    sp.edit().putString("path", path);
                    editor.commit();
                    editor.commit();

                    int total = pictures.size();
                    processRate = ((double) (i+1) / total) * 100;
                    progress.setProgress((int) processRate);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else { // 처리한 내역이 있으면 그 이후부터 처리함
            Log.d(TAG, "처리한 내역 존재");
//            int index = getIndex(processedDate, processedPath);
//            int index = binarySearchIndex(processedDate, 0, pictures.size());
            int index = 0;
            try {
                index = binarySearchIndex(processedDate) + 1; // 이전에 처리했던 이미지 다음 인덱스
                int total = pictures.size() - index;
                int count = 0;
                Log.d(TAG2, "total: "+ total);
                for(int i=index; i<pictures.size(); i++) {
                    Log.d(TAG, "====================");
                    count++;

                    Log.d(TAG2, "count: "+count);
                    Log.d(TAG2, "count/total: "+count/total);
                    processRate = ((double) count / total) * 100;
                    progress.setProgress((int)processRate);
                    Log.d(TAG2, "rate: "+processRate);

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
                        result = result.replaceAll("!\"#[$]%&\\(\\)\\{\\}@`[*]:[+];-.<>,\\^~|'\\[\\]", "");

                        Log.d(TAG, "result: "+result);

                        picture.setContents(result);


                        // 분류 작업 여기에
                        // 텍스트 분석, 명사 받아옴
                        List<String> nouns = processText(result);
//                        String[] inputString = (String[]) nouns.toArray();
                        String[] inputString = nouns.toArray(new String[nouns.size()]);
                        picture.setKeyword(Arrays.toString(inputString));
                        classify(inputString, picture);
//                        Log.d(TAG, "categoryNumber: " + categoryNumber);

                        // ........ 일단 카테고리 랜덤으로
//                        Random random = new Random();
//                        int rand = random.nextInt(9); // 0~8
//                        picture.setCategory(rand);

//                        // DB에 저장
//                        dbHelper.insertData(picture);
//                        Log.d(TAG, "ㅇㅇ");

                        // sp editor에 입력
                        editor.putString("date", creationTime);
//                    sp.edit().putString("date", creationTime);
                        editor.putString("path", path);
//                    sp.edit().putString("path", path);
                        editor.commit();
                        editor.commit();

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progress.setProgress(100);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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
        progress = (ProgressBar) findViewById(R.id.progress);
        progress.setProgress((int) processRate);

        // 권한 설정
        checkPermissions();

        client = new TextClassificationClient(getApplicationContext());
        handler = new Handler();

        // layout
        LinearLayout[] directories = {
                findViewById(R.id.unknown),
                findViewById(R.id.food), /* findViewById(R.id.restaurant), findViewById(R.id.recipe), findViewById(R.id.cafe),*/
                findViewById(R.id.cosmetic), /*findViewById(R.id.cosmetic_discount), findViewById(R.id.skincare), findViewById(R.id.color),*/
                findViewById(R.id.travel)/*, findViewById(R.id.place), findViewById(R.id.exchange), findViewById(R.id.travel_discount), findViewById(R.id.transport)*/
        };

        View.OnClickListener listner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.unknown: // 00. unknown
                        switchActivity(00);
                        break;
                    case R.id.food: // 10
                        switchActivity(10);
                        break;
                    case R.id.cosmetic: // 20
                        switchActivity(20);
                        break;
                    case R.id.travel: // 30
                        switchActivity(30);
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

    public void switchActivity(int value) {
        /*
        기타 00
        음식 10 (맛집 11, 레시피 12, 카페 13)
        화장품 20 (할인 21, 기초 22, 색조 23)
        여행 30 (관광지 31, 환전 32, 할인 33, 교통 34)
         */
        Log.d(TAG, "value: " + value);
//        Intent intent = new Intent(this, DirectoryActivity.class);
        Intent intent;
        if (value != 0) {
            intent = new Intent(this, SubCategoryActivity.class);
        }
        else {
            intent = new Intent(this, DirectoryActivity.class);
        }
        intent.putExtra("category", value);
        startActivity(intent);
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

    public List<String> processText(String result) {
        // (1) normalize
        CharSequence normalized = TwitterKoreanProcessorJava.normalize(result);
        Log.d(TAG, "정규화: "+normalized);

        // (2) Tokenize
        Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
        Log.d(TAG, "토큰화: "+TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens)+" ");

        List<KoreanTokenJava> tokenList = TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens);
        List<String> nouns = new ArrayList<>();

        for(int i=0; i<tokenList.size(); i++) {
            // 명사만 저장
            String pos = tokenList.get(i).getPos().toString();
            if(pos.equals("Noun") || pos.equals("ProperNoun")) {
                String noun = tokenList.get(i).getText();
                nouns.add(noun);
            }
        }
        Log.d(TAG, nouns.toString());

        // 중복 제거
        HashSet<String> set = new HashSet<>(nouns);
        nouns = new ArrayList<String>(set);
        Log.d(TAG, "증복 제거" + nouns.toString());

        return nouns;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if(!loaded) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    client.load();
                }
            });
            loaded = true;
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "onStop");
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                client.unload();
//            }
//        });
//    }

    private void classify(final String[] inputString, final Picture picture) {
        handler.post(new Runnable() {
            @Override
            public void run() {
//                List<TextClassificationClient.Result> results = client.classify(inputString);
                String categoryName = client.classify(inputString);
                int categoryNumber = Integer.parseInt(categoryName);
                Log.d(TAG, "classify- categoryNumber: " + categoryNumber);
                picture.setCategory(categoryNumber);

                // DB에 저장
                dbHelper.insertData(picture);
                Log.d(TAG, "db에 저장했음");
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void grayScaleJNI(long inputImage, long outputImage);
    public native void binarizationJNI(long inputImage, long outputImage);
}

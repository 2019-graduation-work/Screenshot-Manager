package com.hhj73.ocrtest;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OCR TEST";
    final int GALLERY_CODE = 1;
    final int PROCESSING_CODE = 2;

    ImageView imageView;
    Bitmap img;
    TextView beforeText;
    TextView afterText;

    String datapath = "";
    String lang = "";
    static TessBaseAPI sTess;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        if(!OpenCVLoader.initDebug()) {
           Log.d(TAG, "OpenCV is not loaded");
        } else {
            Log.d(TAG, "OpenCV is successfully loaded!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        imageView = (ImageView) findViewById(R.id.imageView);
        beforeText = (TextView) findViewById(R.id.beforeText);
        afterText = (TextView) findViewById(R.id.afterText);

        beforeText.setMovementMethod(new ScrollingMovementMethod());
        afterText.setMovementMethod(new ScrollingMovementMethod());

        sTess = new TessBaseAPI();
        lang = "kor+eng";
        datapath = getFilesDir() + "/tesseract";
        if(checkFile(new File(datapath+"/tessdata"))) {
            sTess.init(datapath, lang);
        }
    }

    boolean checkFile(File dir) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }

    void copyFiles() {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void galleryBtnClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_CODE);
    }

    public void cameraBtnClicked(View view) {
        detectEdge();
        imageView.setImageBitmap(img);
    }

    public void processingBtnClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PROCESSING_CODE);
    }

    public void activityChangeBtnClicked(View view) {
        Intent intent = new Intent(this, ProcessingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    img = BitmapFactory.decodeStream(in);

                    int height = img.getHeight();
                    int width = img.getWidth();

//                    img = Bitmap.createScaledBitmap(img, width/3, height/3, true);
                    String str = width + ", " + height;
                    Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                    in.close();

//                    binarization();
                    imageView.setImageBitmap(img);

                    sTess.setImage(img);
                    String result = sTess.getUTF8Text();
                    beforeText.setText(result);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(requestCode == PROCESSING_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    img = BitmapFactory.decodeStream(in);

                    binarization();

                    sTess.setImage(img);
                    String result = sTess.getUTF8Text();
                    afterText.setText(result);

                    imageView.setImageBitmap(img);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* opencv API로 이미지 처리 */
    public void detectEdge() {
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);

        Mat edge = new Mat();
        Imgproc.Canny(src, edge, 50, 150);

        Utils.matToBitmap(edge, img);

        src.release();
        edge.release();
    }

    public void graysclae() {
        // 회색조
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(gray, img);

        src.release();
        gray.release();
    }

    public void binarization() {
        // 이진화

        // grayscale
        Mat src = new Mat();
        Mat gray = new Mat();
        Utils.bitmapToMat(img, src);
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);

        Mat bin = new Mat();
        Imgproc.threshold(gray, bin, 127, 255, THRESH_BINARY);

        Utils.matToBitmap(bin, img);

        src.release();
        bin.release();
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native String stringFromJNI();
    public native void testFunction();
}

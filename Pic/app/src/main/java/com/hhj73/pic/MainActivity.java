package com.hhj73.pic;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class MainActivity extends AppCompatActivity {

    final int GALLERY_CODE = 1;

    ImageView imageView;
    Bitmap img;
    TextView beforeText;
    TextView afterText;

    TessBaseAPI tessBaseAPI;
    String lang = "kor";

    static String TAG = "tess";


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

        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir/*+"/tessdata"*/)) {
            Log.d(TAG, "checkLanguage true return");
            tessBaseAPI.init(dir, lang);
        }
    }

    boolean checkLanguageFile(String dir)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/tessdata/" + lang + ".traineddata";
            Log.d(TAG, "filePath: "+filePath);
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    private void createFiles(String dir)
    {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("tessdata/" + lang + ".traineddata");

            String destFile = dir + "/tessdata/" + lang + ".traineddata";
            Log.d(TAG, "destFile: " + destFile);


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


    public void galleryBtnClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, GALLERY_CODE);
    }

    public void processingBtnClicked(View view) {
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);


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

                    imageView.setImageBitmap(img);

                    tessBaseAPI.setImage(img);
                    String result = tessBaseAPI.getUTF8Text();
                    beforeText.setText(result);
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

    public void grayScale() {
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

}

package com.hhj73.picument;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    final int GALLERY_CODE = 1;
    final int PROCESSING_CODE = 2;

    ImageView imageView;
    Bitmap img;
    TextView beforeText;
    TextView afterText;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        imageView = (ImageView) findViewById(R.id.imageView);
        beforeText = (TextView) findViewById(R.id.beforeText);
        afterText = (TextView) findViewById(R.id.afterText);

        beforeText.setMovementMethod(new ScrollingMovementMethod());
        afterText.setMovementMethod(new ScrollingMovementMethod());
    }

    public void galleryBtnClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, GALLERY_CODE);
    }

    public void processingBtnClicked(View view) {

        // 1. grayscale
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);
        Mat gray = new Mat();

        grayScale(src.getNativeObjAddr(), gray.getNativeObjAddr());

        // 2. binarization
        Mat bin = new Mat();

        binarization(gray.getNativeObjAddr(), bin.getNativeObjAddr());

        Utils.matToBitmap(bin, img);
        imageView.setImageBitmap(img);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    img = BitmapFactory.decodeStream(in);
                    in.close();

                    imageView.setImageBitmap(img);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

    public native void detectEdgeJNI(long inputImage, long outputImage, int th1, int th2);
    public native void grayScale(long inputImage, long outputImage);
    public native void binarization(long inputImage, long outputImage);

}

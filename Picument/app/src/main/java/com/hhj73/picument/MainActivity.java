package com.hhj73.picument;

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

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

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

        sTess = new TessBaseAPI();
        lang = "eng";
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

        // OCR
        sTess.setImage(img);
        String result = sTess.getUTF8Text();
        beforeText.setText(result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) { // 원본 이미지
            if(resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    img = BitmapFactory.decodeStream(in);
                    in.close();

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
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(getExternalFilesDir(null), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
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

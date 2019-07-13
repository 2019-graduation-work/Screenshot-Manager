package com.hhj73.pic;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    final int GALLERY_CODE = 1;

    ImageView imageView;
    Bitmap img;
    TextView beforeText;
    TextView afterText;

    TessBaseAPI tessBaseAPI;
    String lang = "kor";


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
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
        if(checkLanguageFile(dir+"/tessdata"))
            tessBaseAPI.init(dir, lang);
    }

    boolean checkLanguageFile(String dir)
    {
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

    private void createFiles(String dir)
    {
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


    public void galleryBtnClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_CODE);
    }

    public void processingBtnClicked(View view) {
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


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}

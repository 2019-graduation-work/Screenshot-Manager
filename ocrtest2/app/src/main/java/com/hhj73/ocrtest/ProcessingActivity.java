package com.hhj73.ocrtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.boundingRect;

public class ProcessingActivity extends AppCompatActivity {

    final int GALLERY_CODE = 1;

    ImageView imageView;
    Bitmap img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void binarization(View view) {
        // 이진화

        // grayscale
        Mat src = new Mat();
        Mat gray = new Mat();
        Utils.bitmapToMat(img, src);
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);

        Mat bin = new Mat();
        Imgproc.threshold(gray, bin, 127, 255, THRESH_BINARY);

        Utils.matToBitmap(bin, img);

        imageView.setImageBitmap(img);

        src.release();
        bin.release();
    }

    public Mat grayScale() {
        // 그레이스케일
        Mat src = new Mat();
        Utils.bitmapToMat(img, src);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, COLOR_RGB2GRAY);
        return gray;
    }

    public Mat cannyEdgeDetect(Mat src) {
        // 캐니 엣지 디텍트
        Mat edge = new Mat();
        Imgproc.Canny(src, edge, 50, 150);
        return edge;
    }

    public Mat findBorderComponents(Mat src) {
        // 3. 외곽 검출해서 없애버리기
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        // 3-1. findContours로 검출한 문서 외곽 (사각형 바깥쪽) 픽셀을 모두 검은색으로 만든다.
        Imgproc.findContours(src, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        ArrayList<Borders> borders = new ArrayList<>();
        int area = src.rows() * src.cols();

        for(int i=0; i<contours.size(); i++) {
            Rect r = boundingRect(contours.get(i));
            if(r.width * r.height > 0.5 * area) {
                borders.add(new Borders(i, r.x, r.y, r.x+r.width-1, r.y + r.height-1));
            }
        }

//        int i_x1_y1_x2_y2 =

        MatOfPoint borderContour = contours.get(borders.get(0).i);

        return null;
    }

    public Mat doDilation(Mat src) {
        Mat dilated = new Mat();
        Imgproc.dilate(src, dilated, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6)));

        return dilated;
    }

    public void removeBorders() {

    }
    public void textDetection(View view) {
        // 텍스트 영역

        Mat gray = grayScale();
        Mat edge = cannyEdgeDetect(gray);
        Mat noise = findBorderComponents(edge);
        Mat dilated = doDilation(noise);





        // 4. 딜레이션으로 픽셀을 팽창시킨다.
//        Mat dilated = new Mat();
//        Imgproc.dilate(noise, dilated, );


        imageView.setImageBitmap(img);

    }

    public void galleryBtnClicked(View view) {
        // 이미지 가져오기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_CODE);
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

    class Borders {
        public int i;
        public int x;
        public int y;
        public int width;
        public int height;

        public Borders(int i, int x, int y, int width, int height) {
            this.i = i;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

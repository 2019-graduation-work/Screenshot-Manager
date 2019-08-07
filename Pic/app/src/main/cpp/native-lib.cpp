#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_hhj73_pic_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_hhj73_pic_MainActivity_grayScaleJNI(JNIEnv *env, jobject instance, jlong inputImage,
                                             jlong outputImage) {

    // TODO
    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;

    cvtColor(inputMat, outputMat, COLOR_RGB2GRAY);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_hhj73_pic_MainActivity_binarizationJNI(JNIEnv *env, jobject instance, jlong inputImage,
                                                jlong outputImage) {

    // TODO
    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;

    threshold(inputMat, outputMat, 127, 255, THRESH_BINARY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_hhj73_pic_SavePictureActivity_grayScaleJNI(JNIEnv *env, jobject instance, jlong inputImage,
                                                    jlong outputImage) {

    // TODO
    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;

    cvtColor(inputMat, outputMat, COLOR_RGB2GRAY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_hhj73_pic_SavePictureActivity_binarizationJNI(JNIEnv *env, jobject instance,
                                                       jlong inputImage, jlong outputImage) {

    // TODO
    Mat &inputMat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;

    threshold(inputMat, outputMat, 127, 255, THRESH_BINARY);
}
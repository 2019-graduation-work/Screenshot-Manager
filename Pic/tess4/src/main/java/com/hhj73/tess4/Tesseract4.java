package com.hhj73.tess4;

public class Tesseract4 {
    static {
        System.loadLibrary("tess4");
    }

    public native int tessTest(int v);
}

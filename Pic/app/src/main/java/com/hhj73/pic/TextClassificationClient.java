package com.hhj73.pic;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Interface to load TfLite model and provide predictions. */
public class TextClassificationClient {
    private static final String TAG = "ㅎㅇㅎㅇ";
    private static final String MODEL_PATH = "converted_model.tflite";
    private static final String DIC_PATH = "vocab.txt";
    private static final String LABEL_PATH = "labels1.txt";

    private static final int SENTENCE_LEN = 2000;  // The maximum length of an input sentence.
    // Simple delimiter to split words.
    private static final String SIMPLE_SPACE_OR_PUNCTUATION = " |\\,|\\.|\\!|\\?|\n";
    /*
     * Reserved values in ImdbDataSet dic:
     * dic["<PAD>"] = 0      used for padding
     * dic["<START>"] = 1    mark for the start of a sentence
     * dic["<UNKNOWN>"] = 2  mark for unknown words (OOV)
     */
    private static final String START = "<START>";
    private static final String PAD = "<PAD>";
    private static final String UNKNOWN = "<UNKNOWN>";

    /** Number of results to show in the UI. */
    private static final int MAX_RESULTS = 3;

    private final Context context;
    private final Map<String, Integer> dic = new HashMap<>();
    private final List<String> labels = new ArrayList<>();
    private Interpreter tflite;

    /** An immutable result returned by a TextClassifier describing what was classified. */
    public static class Result {
        /**
         * A unique identifier for what has been classified. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /** Display name for the result. */
        private final String title;

        /** A sortable score for how good the result is relative to others. Higher should be better. */
        private final Float confidence;

        public Result(final String id, final String title, final Float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            return resultString.trim();
        }
    }

    public TextClassificationClient(Context context) {
        this.context = context;
    }

    /** Load the TF Lite model and dictionary so that the client can start classifying text. */
    @WorkerThread
    public void load() {
        loadModel();
        loadDictionary();
        loadLabels();
    }

    /** Load TF Lite model. */
    @WorkerThread
    private synchronized void loadModel() {
        try {
            ByteBuffer buffer = loadModelFile(this.context.getAssets());
            tflite = new Interpreter(buffer);
            Log.v(TAG, "TFLite model loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /** Load words dictionary. */
    @WorkerThread
    private synchronized void loadDictionary() {
        try {
            loadDictionaryFile(this.context.getAssets());
            Log.v(TAG, "Dictionary loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /** Load labels. */
    @WorkerThread
    private synchronized void loadLabels() {
        try {
            loadLabelFile(this.context.getAssets());
            Log.v(TAG, "Labels loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /** Free up resources as the client is no longer needed. */
    @WorkerThread
    public synchronized void unload() {
        tflite.close();
        dic.clear();
        labels.clear();
    }

    /** Classify an input string and returns the classification results. */
    @WorkerThread
    public synchronized /*List<Result>*/ String classify(String[] inputString) {
        Log.d(TAG, "으앙"+Arrays.toString(inputString));

        // Pre-prosessing.
        float[][] input = tokenizeInputText(inputString);
        Log.d(TAG, "float input "+Arrays.toString(input));
        // Run inference.
        Log.v(TAG, "Classifying text with TF Lite...");
        float[][] output = new float[1][labels.size()];
//        float[][] output = new float[SENTENCE_LEN][1];
        tflite.run(input, output);


        // Find the best classifications.
        PriorityQueue<Result> pq =
                new PriorityQueue<>(
                        MAX_RESULTS, new Comparator<Result>() {
                    @Override
                    public int compare(Result lhs, Result rhs) {
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    }
                });
        for (int i = 0; i < /*1*/labels.size(); i++) {
            pq.add(new Result("" + i, labels.get(i), output[0][i]));
        }
        final ArrayList<Result> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            results.add(pq.poll());
        }

        Log.d(TAG, "result: "+results.toString());

        int cnt = 0;
        for(int i=0; i< results.size(); i++) {
            if(results.get(i).getConfidence() < 0.20) {
                Log.d(TAG, results.get(i).title + "confidence: "+results.get(i).getConfidence());
                Log.d(TAG, results.get(i).title + ": cnt++");
                cnt++;
            }
        }
        // Return the probability of each class.
//        return results;
        Log.d(TAG, "cnt: "+ cnt);
        Log.d(TAG, "results.size" + results.size());
        if(cnt == results.size()) {
            return String.valueOf(0);
        }
        else {
            return results.get(0).title;
        }
    }



    /** Load TF Lite model from assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_PATH);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    /** Load dictionary from assets. */
    private void loadLabelFile(AssetManager assetManager) throws IOException {
        try (InputStream ins = assetManager.open(LABEL_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            // Each line in the label file is a label.
            while (reader.ready()) {
                labels.add(reader.readLine());
            }
        }
    }

    /** Load labels from assets. */
    private void loadDictionaryFile(AssetManager assetManager) throws IOException {
        try (InputStream ins = assetManager.open(DIC_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            // Each line in the dictionary has two columns.
            // First column is a word, and the second is the index of this word.
            while (reader.ready()) {
                List<String> line = Arrays.asList(reader.readLine().split(" "));
                if (line.size() < 2) {
                    continue;
                }
                dic.put(line.get(0), Integer.parseInt(line.get(1)));
            }
            Log.d(TAG, "Dictionary complete");
        }
    }

    /** Pre-prosessing: tokenize and map the input words into a float array. */
    float[][] tokenizeInputText(String[] inputString) {
        float[] tmp = new float[SENTENCE_LEN];
//        List<String> array = Arrays.asList(text.split(SIMPLE_SPACE_OR_PUNCTUATION));
        List<String> array = Arrays.asList(inputString);

        int index = 0;
        // Prepend <START> if it is in vocabulary file.
//        if (dic.containsKey(START)) {
//            tmp[index++] = dic.get(START);
//        }

        for (String word : array) {
            if (index >= SENTENCE_LEN) {
                break;
            }
            tmp[index++] = dic.containsKey(word) ? dic.get(word) : /* dic.get(UNKNOWN) */0;
        }
        // Padding and wrapping.
        Arrays.fill(tmp, index, SENTENCE_LEN - 1, /*(int) dic.get(PAD)*/0);
        float[][] ans = {tmp};
        return ans;
    }

    Map<String, Integer> getDic() {
        return this.dic;
    }

    Interpreter getTflite() {
        return this.tflite;
    }

    List<String> getLabels() {
        return this.labels;
    }
}
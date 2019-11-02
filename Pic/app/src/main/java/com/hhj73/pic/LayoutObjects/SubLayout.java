package com.hhj73.pic.LayoutObjects;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.hhj73.pic.R;

public class SubLayout extends LinearLayout {

    public SubLayout(Context context) {
        super(context);
        init(context);
    }

    public SubLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sublayout,this,true);
    }
}

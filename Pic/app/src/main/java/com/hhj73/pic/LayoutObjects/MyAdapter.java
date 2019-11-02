package com.hhj73.pic.LayoutObjects;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hhj73.pic.ImageViewActivity;
import com.hhj73.pic.Objects.Picture;
import com.hhj73.pic.R;

import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    public static ArrayList<Picture> mDataSet;
    public static Context mContext;

    public MyAdapter(ArrayList<Picture> mDataSet, Context mContext) {
        this.mDataSet = mDataSet;
        this.mContext = mContext;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView mImageView;
        public LinearLayout mLinearLayout;
        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.iv);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.ll);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(v.getContext(), "힝"+getLayoutPosition(), Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(v.getContext(), ImageViewActivity.class);
            intent1.putExtra("picture", mDataSet.get(getLayoutPosition()));
            v.getContext().startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        return null;
        View v = LayoutInflater.from(mContext).inflate(R.layout.cardview, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = new File(mDataSet.get(position).getPath());

        if(file.exists()) { // 파일 있으면
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            holder.mImageView.setImageBitmap(bitmap);
        }
        else { // 파일 없으면
            holder.mImageView.setBackgroundResource(R.drawable.not_found);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


}

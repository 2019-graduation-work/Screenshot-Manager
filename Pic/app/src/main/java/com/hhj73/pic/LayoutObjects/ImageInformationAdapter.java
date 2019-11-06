package com.hhj73.pic.LayoutObjects;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hhj73.pic.Objects.Picture;
import com.hhj73.pic.R;

import java.util.ArrayList;

public class ImageInformationAdapter extends RecyclerView.Adapter<ImageInformationAdapter.ItemViewHolder> {
    private ArrayList<Picture> listData = new ArrayList<>();

    @NonNull
    @Override
    public ImageInformationAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_information, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageInformationAdapter.ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void addItem(Picture picture) {
        listData.add(picture);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private TextView keywordTextView;
        private TextView contentsTextView;

        ItemViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            keywordTextView = (TextView) itemView.findViewById(R.id.keywordTextView);
            contentsTextView = (TextView) itemView.findViewById(R.id.contentsTextView);
        }

        void onBind(Picture picture) {
            dateTextView.setText(picture.getDate());
//            keywordTextView.setText(picture.getKeyword());
            contentsTextView.setText(picture.getContents());
        }
    }
}

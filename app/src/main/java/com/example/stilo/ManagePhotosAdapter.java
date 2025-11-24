package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ManagePhotosAdapter extends RecyclerView.Adapter<ManagePhotosAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<Photo> photoList;
    private final OnPhotoDeleteListener deleteListener;

    public interface OnPhotoDeleteListener {
        void onPhotoDelete(Photo photo);
    }

    public ManagePhotosAdapter(Context context, List<Photo> photoList, OnPhotoDeleteListener deleteListener) {
        this.context = context;
        this.photoList = photoList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);
        Glide.with(context)
                .load(photo.getUrl())
                .centerCrop()
                .into(holder.imageView);

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPhotoDelete(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
            deleteButton = itemView.findViewById(R.id.delete_photo_button);
        }
    }
}

package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class NearbyProviderAdapter extends RecyclerView.Adapter<NearbyProviderAdapter.ProviderViewHolder> {

    private final Context context;
    private final List<Provider> providerList;
    private final OnProviderClickListener listener;

    // Interface para o click listener
    public interface OnProviderClickListener {
        void onProviderClick(Provider provider);
    }

    public NearbyProviderAdapter(Context context, List<Provider> providerList, OnProviderClickListener listener) {
        this.context = context;
        this.providerList = providerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_provider, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Provider provider = providerList.get(position);

        holder.nameTextView.setText(provider.getName());

        // Carrega a imagem do perfil
        if (provider.getProfileImageUrl() != null && !provider.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(provider.getProfileImageUrl())
                    .placeholder(R.drawable.sharp_account_circle_24)
                    .error(R.drawable.sharp_account_circle_24)
                    .circleCrop()
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.sharp_account_circle_24);
        }

        // Define o listener para o item inteiro
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProviderClick(provider);
            }
        });
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.image_view_provider_icon);
            nameTextView = itemView.findViewById(R.id.text_view_provider_name);
        }
    }
}

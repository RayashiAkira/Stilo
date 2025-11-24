package com.example.stilo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stilo.User;

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ViewHolder> {

    private Context context;
    private List<User> clientList;

    public ClienteAdapter(Context context, List<User> clientList) {
        this.context = context;
        this.clientList = clientList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cliente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = clientList.get(position);
        holder.clientName.setText(user.getApelido());
        holder.clientEmail.setText(user.getEmail());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.clientImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PerfilClienteActivity.class);
            intent.putExtra("clientName", user.getApelido());
            intent.putExtra("clientEmail", user.getEmail());
            intent.putExtra("clientImageUrl", user.getProfileImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView clientImage;
        TextView clientName, clientEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientImage = itemView.findViewById(R.id.client_image);
            clientName = itemView.findViewById(R.id.client_name);
            clientEmail = itemView.findViewById(R.id.client_email);
        }
    }
}

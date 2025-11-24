package com.example.stilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;
    private final OnHorarioListener listener;

    public interface OnHorarioListener {
        void onDeleteClick(Horario horario);
    }

    public HorarioAdapter(List<Horario> horarios, OnHorarioListener listener) {
        this.horarios = horarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        Horario horario = horarios.get(position);

        holder.titleText.setText(horario.getTitle());
        holder.horarioText.setText(horario.getFormattedTime());
        holder.serviceTypeText.setText(horario.getServiceType());
        holder.priceText.setText(String.format(Locale.getDefault(), "R$ %.2f", horario.getPrice()));

        if (horario.getDescription() != null && !horario.getDescription().isEmpty()) {
            holder.descriptionText.setVisibility(View.VISIBLE);
            holder.descriptionText.setText(horario.getDescription());
        } else {
            holder.descriptionText.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(horario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView horarioText;
        TextView serviceTypeText;
        TextView priceText;
        TextView descriptionText;
        ImageButton deleteButton;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            horarioText = itemView.findViewById(R.id.horario_text);
            serviceTypeText = itemView.findViewById(R.id.service_type_text);
            priceText = itemView.findViewById(R.id.price_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

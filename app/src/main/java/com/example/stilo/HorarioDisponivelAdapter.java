package com.example.stilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HorarioDisponivelAdapter extends RecyclerView.Adapter<HorarioDisponivelAdapter.HorarioDisponivelViewHolder> {

    private final List<Horario> horarios;
    private final OnHorarioClickListener listener;

    public interface OnHorarioClickListener {
        void onHorarioClick(Horario horario);
    }

    public HorarioDisponivelAdapter(List<Horario> horarios, OnHorarioClickListener listener) {
        this.horarios = horarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HorarioDisponivelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario_disponivel, parent, false);
        return new HorarioDisponivelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioDisponivelViewHolder holder, int position) {
        Horario horario = horarios.get(position);
        holder.horarioText.setText(horario.getFormattedTime());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHorarioClick(horario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    static class HorarioDisponivelViewHolder extends RecyclerView.ViewHolder {
        TextView horarioText;

        public HorarioDisponivelViewHolder(@NonNull View itemView) {
            super(itemView);
            horarioText = itemView.findViewById(R.id.horario_disponivel_text);
        }
    }
}

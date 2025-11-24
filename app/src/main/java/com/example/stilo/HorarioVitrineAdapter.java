package com.example.stilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HorarioVitrineAdapter extends RecyclerView.Adapter<HorarioVitrineAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;
    private final OnHorarioClickListener listener;

    public interface OnHorarioClickListener {
        void onHorarioClick(Horario horario);
    }

    public HorarioVitrineAdapter(List<Horario> horarios, OnHorarioClickListener listener) {
        this.horarios = horarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario_vitrine, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        Horario horario = horarios.get(position);
        String time = horario.getStartTime() + " - " + horario.getEndTime();
        holder.timeTextView.setText(time);

        holder.agendarButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHorarioClick(horario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        Button agendarButton;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.horario_time_text);
            agendarButton = itemView.findViewById(R.id.agendar_button_item);
        }
    }
}

package com.example.stilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.AgendamentoViewHolder> {

    private final List<Agendamento> agendamentos;
    private final OnAgendamentoListener listener;

    public interface OnAgendamentoListener {
        void onCancelClick(Agendamento agendamento);
    }

    public AgendamentoAdapter(List<Agendamento> agendamentos, OnAgendamentoListener listener) {
        this.agendamentos = agendamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AgendamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agendamento, parent, false);
        return new AgendamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendamentoViewHolder holder, int position) {
        Agendamento agendamento = agendamentos.get(position);

        holder.providerNameText.setText(agendamento.getProviderName());
        holder.dateText.setText(agendamento.getDate());
        holder.timeText.setText(agendamento.getFormattedTime());

        holder.cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick(agendamento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return agendamentos.size();
    }

    static class AgendamentoViewHolder extends RecyclerView.ViewHolder {
        TextView providerNameText, dateText, timeText;
        Button cancelButton;

        public AgendamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            providerNameText = itemView.findViewById(R.id.provider_name_text);
            dateText = itemView.findViewById(R.id.date_text);
            timeText = itemView.findViewById(R.id.time_text);
            cancelButton = itemView.findViewById(R.id.cancel_button);
        }
    }
}

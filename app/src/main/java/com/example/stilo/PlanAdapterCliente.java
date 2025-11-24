package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlanAdapterCliente extends RecyclerView.Adapter<PlanAdapterCliente.PlanViewHolder> {

    private final Context context;
    private final List<Plan> planList;
    private final OnPlanAdhesionListener onPlanAdhesionListener;

    public interface OnPlanAdhesionListener {
        void onAdhesionClick(Plan plan);
    }

    public PlanAdapterCliente(Context context, List<Plan> planList, OnPlanAdhesionListener onPlanAdhesionListener) {
        this.context = context;
        this.planList = planList;
        this.onPlanAdhesionListener = onPlanAdhesionListener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plan_cliente, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.planNameTextView.setText(plan.getName());
        holder.planDescriptionTextView.setText(plan.getDescription());
        holder.planPriceTextView.setText(String.format("R$ %.2f", plan.getPrice()));
        holder.planDurationTextView.setText("Duração: " + plan.getDuration());

        holder.adhesionButton.setOnClickListener(v -> {
            if (onPlanAdhesionListener != null) {
                onPlanAdhesionListener.onAdhesionClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView planNameTextView, planDescriptionTextView, planPriceTextView, planDurationTextView;
        Button adhesionButton;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            planNameTextView = itemView.findViewById(R.id.plan_name_text_view);
            planDescriptionTextView = itemView.findViewById(R.id.plan_description_text_view);
            planPriceTextView = itemView.findViewById(R.id.plan_price_text_view);
            planDurationTextView = itemView.findViewById(R.id.plan_duration_text_view);
            adhesionButton = itemView.findViewById(R.id.adhesion_button);
        }
    }
}

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

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private final Context context;
    private final List<Plan> planList;
    private final OnPlanListener onPlanListener;

    public interface OnPlanListener {
        void onEditClick(Plan plan);
        void onDeleteClick(Plan plan);
    }

    public PlanAdapter(Context context, List<Plan> planList, OnPlanListener onPlanListener) {
        this.context = context;
        this.planList = planList;
        this.onPlanListener = onPlanListener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.planNameTextView.setText(plan.getName());
        holder.planDescriptionTextView.setText(plan.getDescription());
        holder.planPriceTextView.setText(String.format("R$ %.2f", plan.getPrice()));
        holder.planDurationTextView.setText("Duração: " + plan.getDuration());

        holder.editButton.setOnClickListener(v -> {
            if (onPlanListener != null) {
                onPlanListener.onEditClick(plan);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onPlanListener != null) {
                onPlanListener.onDeleteClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView planNameTextView, planDescriptionTextView, planPriceTextView, planDurationTextView;
        Button editButton, deleteButton;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            planNameTextView = itemView.findViewById(R.id.plan_name_text_view);
            planDescriptionTextView = itemView.findViewById(R.id.plan_description_text_view);
            planPriceTextView = itemView.findViewById(R.id.plan_price_text_view);
            planDurationTextView = itemView.findViewById(R.id.plan_duration_text_view);
            editButton = itemView.findViewById(R.id.edit_plan_button);
            deleteButton = itemView.findViewById(R.id.delete_plan_button);
        }
    }
}

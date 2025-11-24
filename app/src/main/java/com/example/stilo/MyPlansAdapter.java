package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stilo.R;
import java.util.List;

public class MyPlansAdapter extends RecyclerView.Adapter<MyPlansAdapter.ViewHolder> {

    private Context context;
    private List<Plan> planList;

    public MyPlansAdapter(Context context, List<Plan> planList) {
        this.context = context;
        this.planList = planList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_plan_partner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.planNameTextView.setText(plan.getName());
        holder.providerNameTextView.setText(plan.getDescription());
        holder.pointsTextView.setText(String.valueOf(plan.getPrice()));
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView planNameTextView;
        TextView providerNameTextView;
        TextView pointsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            planNameTextView = itemView.findViewById(R.id.plan_name_text_view);
            providerNameTextView = itemView.findViewById(R.id.provider_name_text_view);
            pointsTextView = itemView.findViewById(R.id.points_text_view);
        }
    }
}

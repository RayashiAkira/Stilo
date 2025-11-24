package com.example.stilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private final List<Subscription> subscriptionList;
    private final OnSubscriptionCancelListener listener;

    public interface OnSubscriptionCancelListener {
        void onCancelClick(Subscription subscription);
    }

    public SubscriptionAdapter(List<Subscription> subscriptionList, OnSubscriptionCancelListener listener) {
        this.subscriptionList = subscriptionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_plan_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription subscription = subscriptionList.get(position);
        holder.planName.setText(subscription.getPlanName());
        holder.providerName.setText(subscription.getProviderName());

        holder.cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick(subscription);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView planName, providerName;
        Button cancelButton;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            planName = itemView.findViewById(R.id.subscription_plan_name);
            providerName = itemView.findViewById(R.id.subscription_provider_name);
            cancelButton = itemView.findViewById(R.id.cancel_subscription_button);
        }
    }
}

package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceTransactionAdapter extends RecyclerView.Adapter<FinanceTransactionAdapter.ViewHolder> {

    private final List<FinanceTransaction> transactions;
    private final OnTransactionListener listener;

    public interface OnTransactionListener {
        void onTransactionLongClick(FinanceTransaction transaction);
    }

    public FinanceTransactionAdapter(List<FinanceTransaction> transactions, OnTransactionListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finance_transaction_provider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FinanceTransaction transaction = transactions.get(position);
        holder.bind(transaction, listener);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView description;
        TextView tag;
        TextView amount;
        TextView date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.transaction_icon);
            name = itemView.findViewById(R.id.transaction_name);
            description = itemView.findViewById(R.id.transaction_description);
            tag = itemView.findViewById(R.id.transaction_tag);
            amount = itemView.findViewById(R.id.transaction_amount);
            date = itemView.findViewById(R.id.transaction_date);
        }

        public void bind(final FinanceTransaction transaction, final OnTransactionListener listener) {
            Context context = itemView.getContext();

            name.setText(transaction.getName());
            description.setText(transaction.getDescription());

            // Formata a data
            Date dateValue = transaction.getDate();
            if (dateValue != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                date.setText(dateFormat.format(dateValue));
            } else {
                date.setText("");
            }

            // Configura o valor e a cor
            double transactionAmount = transaction.getAmount();
            if (transactionAmount >= 0) {
                amount.setText(String.format(Locale.getDefault(), "+ R$ %.2f", transactionAmount));
                amount.setTextColor(ContextCompat.getColor(context, R.color.income_color));
                icon.setImageResource(R.drawable.ic_arrow_upward);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.income_color));
                tag.setVisibility(View.GONE); // Esconde a tag para ganhos
            } else {
                amount.setText(String.format(Locale.getDefault(), "- R$ %.2f", Math.abs(transactionAmount)));
                amount.setTextColor(ContextCompat.getColor(context, R.color.expense_color));
                icon.setImageResource(R.drawable.ic_arrow_downward);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.expense_color));

                // Mostra a tag de despesa, se existir
                if (transaction.getExpenseTag() != null && !transaction.getExpenseTag().isEmpty()) {
                    tag.setText(transaction.getExpenseTag());
                    tag.setVisibility(View.VISIBLE);
                } else {
                    tag.setVisibility(View.GONE);
                }
            }

            // Configura o clique longo
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionLongClick(transaction);
                    return true;
                }
                return false;
            });
        }
    }
}

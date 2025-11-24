package com.example.stilo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinancesFragment extends Fragment implements FinanceTransactionAdapter.OnTransactionListener {

    private static final String TAG = "FinancesFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView totalSpentAmount, plansSpentAmount, servicesSpentAmount;
    private BarChart spendingChart;
    private FloatingActionButton fabAddExpense;

    private List<FinanceTransaction> transactionList;
    private FinanceTransactionAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // ... (findViewByIds) ...
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_finances);
        recyclerView = view.findViewById(R.id.recycler_view_finances);
        progressBar = view.findViewById(R.id.progress_finances);
        emptyView = view.findViewById(R.id.empty_view_finances);
        totalSpentAmount = view.findViewById(R.id.total_spent_amount);
        plansSpentAmount = view.findViewById(R.id.plans_spent_amount);
        servicesSpentAmount = view.findViewById(R.id.services_spent_amount);
        spendingChart = view.findViewById(R.id.spending_chart);
        fabAddExpense = view.findViewById(R.id.fab_add_expense_client);

        setupRecyclerView();
        setupChart();

        swipeRefreshLayout.setOnRefreshListener(this::loadTransactions);

        fabAddExpense.setOnClickListener(v -> {
            showAddExpenseDialog(null);
        });

        loadTransactions();
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        adapter = new FinanceTransactionAdapter(transactionList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid()).collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) { Log.w(TAG, "Listen failed.", error); return; }

                    transactionList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            FinanceTransaction transaction = doc.toObject(FinanceTransaction.class);
                            transaction.setId(doc.getId());
                            transactionList.add(transaction);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateUI();
                });
    }

    @Override
    public void onTransactionLongClick(FinanceTransaction transaction) {
        showEditDeleteDialog(transaction);
    }

    private void showEditDeleteDialog(final FinanceTransaction transaction) {
        new AlertDialog.Builder(getContext())
                .setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddExpenseDialog(transaction);
                    } else {
                        showDeleteConfirmationDialog(transaction);
                    }
                }).show();
    }

    private void showDeleteConfirmationDialog(final FinanceTransaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Excluir Despesa")
                .setMessage("Tem certeza que deseja excluir '" + transaction.getName() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> deleteTransaction(transaction))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteTransaction(final FinanceTransaction transaction) {
        if (currentUser == null || transaction.getId() == null) return;
        db.collection("users").document(currentUser.getUid()).collection("transactions").document(transaction.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Despesa excluída!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao excluir.", Toast.LENGTH_SHORT).show());
    }

    private void showAddExpenseDialog(@Nullable final FinanceTransaction transactionToEdit) {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        final EditText nameEditText = view.findViewById(R.id.edit_text_expense_name);
        final EditText amountEditText = view.findViewById(R.id.edit_text_expense_amount);
        final RadioGroup typeRadioGroup = view.findViewById(R.id.radio_group_expense_type);
        final RadioButton planRadioButton = view.findViewById(R.id.radio_button_plan);
        final RadioButton serviceRadioButton = view.findViewById(R.id.radio_button_service);

        final boolean isEditing = transactionToEdit != null;

        builder.setTitle(isEditing ? "Editar Despesa" : "Adicionar Nova Despesa");
        if (isEditing) {
            nameEditText.setText(transactionToEdit.getName());
            amountEditText.setText(String.format(Locale.getDefault(), "%.2f", transactionToEdit.getAmount()));
            if ("Plano".equals(transactionToEdit.getType())) {
                planRadioButton.setChecked(true);
            } else {
                serviceRadioButton.setChecked(true);
            }
        }

        builder.setView(view);
        builder.setPositiveButton(isEditing ? "Salvar" : "Adicionar", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString().replace(',', '.');
            int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();

            if (name.isEmpty() || amountStr.isEmpty() || selectedTypeId == -1) {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String type = selectedTypeId == R.id.radio_button_plan ? "Plano" : "Serviço";

            FinanceTransaction transaction = new FinanceTransaction(name, amount, type, null, null, new Date());

            if (isEditing) {
                db.collection("users").document(currentUser.getUid()).collection("transactions").document(transactionToEdit.getId())
                        .set(transaction) // Usa 'set' para sobrescrever o objeto
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Despesa atualizada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao atualizar.", Toast.LENGTH_SHORT).show());
            } else {
                db.collection("users").document(currentUser.getUid()).collection("transactions")
                        .add(transaction)
                        .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Despesa adicionada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao adicionar.", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    // ... (setupChart, updateUI, updateTotals, updateChart) ...
    private void setupChart() {
        spendingChart.getDescription().setEnabled(false);
        spendingChart.getLegend().setEnabled(false);
        spendingChart.setDrawValueAboveBar(true);

        XAxis xAxis = spendingChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);

        spendingChart.getAxisLeft().setTextColor(Color.WHITE);
        spendingChart.getAxisRight().setEnabled(false);
    }

    private void updateUI() {
        updateTotals();
        updateChart();
        emptyView.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTotals() {
        double total = 0;
        double plansTotal = 0;
        double servicesTotal = 0;

        for (FinanceTransaction transaction : transactionList) {
            total += transaction.getAmount();
            if ("Plano".equals(transaction.getType())) {
                plansTotal += transaction.getAmount();
            } else {
                servicesTotal += transaction.getAmount();
            }
        }

        totalSpentAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
        plansSpentAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", plansTotal));
        servicesSpentAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", servicesTotal));
    }

    private void updateChart() {
        if (transactionList.isEmpty() || getContext() == null) {
            spendingChart.clear();
            spendingChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        double plansTotal = 0;
        double servicesTotal = 0;

        for (FinanceTransaction transaction : transactionList) {
            if ("Plano".equals(transaction.getType())) {
                plansTotal += transaction.getAmount();
            } else {
                servicesTotal += transaction.getAmount();
            }
        }
        entries.add(new BarEntry(0, (float) plansTotal));
        entries.add(new BarEntry(1, (float) servicesTotal));

        BarDataSet dataSet = new BarDataSet(entries, "Gastos");
        int[] colors = {
                ContextCompat.getColor(getContext(), R.color.plan_color),
                ContextCompat.getColor(getContext(), R.color.service_color)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        spendingChart.setData(barData);
        spendingChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Planos", "Serviços"}));
        spendingChart.invalidate();
    }
}

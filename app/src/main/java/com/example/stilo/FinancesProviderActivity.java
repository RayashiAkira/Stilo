package com.example.stilo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinancesProviderActivity extends AppCompatActivity implements FinanceTransactionAdapter.OnTransactionListener {

    private static final String TAG = "FinancesProviderActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView balanceAmount, incomeAmount, expenseAmount;
    private BarChart earningsChart;
    private FloatingActionButton fabAddTransaction;

    private List<FinanceTransaction> transactionList;
    private FinanceTransactionAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_finances_provider);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_finances_provider);
        recyclerView = findViewById(R.id.recycler_view_finances_provider);
        progressBar = findViewById(R.id.progress_finances_provider);
        emptyView = findViewById(R.id.empty_view_finances_provider);
        balanceAmount = findViewById(R.id.balance_amount_provider);
        incomeAmount = findViewById(R.id.income_amount_provider);
        expenseAmount = findViewById(R.id.expense_amount_provider);
        earningsChart = findViewById(R.id.earnings_chart);
        fabAddTransaction = findViewById(R.id.fab_add_transaction_provider);

        setupRecyclerView();
        setupChart();

        swipeRefreshLayout.setOnRefreshListener(this::loadTransactions);

        fabAddTransaction.setOnClickListener(v -> showAddTransactionDialog(null));

        loadTransactions();
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        adapter = new FinanceTransactionAdapter(transactionList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        db.collection("users").document(currentUser.getUid()).collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

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
        new AlertDialog.Builder(this)
                .setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddTransactionDialog(transaction);
                    } else {
                        showDeleteConfirmationDialog(transaction);
                    }
                }).show();
    }

    private void showDeleteConfirmationDialog(final FinanceTransaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Transação")
                .setMessage("Tem certeza que deseja excluir '" + transaction.getName() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> deleteTransaction(transaction))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteTransaction(final FinanceTransaction transaction) {
        if (currentUser == null || transaction.getId() == null) return;
        db.collection("users").document(currentUser.getUid()).collection("transactions").document(transaction.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transação excluída!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
    }

    private void showAddTransactionDialog(@Nullable final FinanceTransaction transactionToEdit) {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction_provider, null);

        final EditText nameEditText = view.findViewById(R.id.edit_text_transaction_name);
        final EditText amountEditText = view.findViewById(R.id.edit_text_transaction_amount);
        final EditText descriptionEditText = view.findViewById(R.id.edit_text_transaction_description);
        final RadioGroup typeRadioGroup = view.findViewById(R.id.radio_group_transaction_type);
        final RadioButton incomeRadioButton = view.findViewById(R.id.radio_button_income);
        final RadioButton expenseRadioButton = view.findViewById(R.id.radio_button_expense);
        final LinearLayout expenseTagsContainer = view.findViewById(R.id.expense_tags_container);
        final RadioGroup expenseTagRadioGroup = view.findViewById(R.id.radio_group_expense_tag);
        final RadioButton essentialRadioButton = view.findViewById(R.id.radio_button_essential);
        final RadioButton optionalRadioButton = view.findViewById(R.id.radio_button_optional);
        final Button dateButton = view.findViewById(R.id.button_transaction_date);

        final boolean isEditing = transactionToEdit != null;
        selectedDate = Calendar.getInstance(); // Reseta para a data atual

        updateDateButtonText(dateButton);

        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            expenseTagsContainer.setVisibility(checkedId == R.id.radio_button_expense ? View.VISIBLE : View.GONE);
        });

        dateButton.setOnClickListener(v -> showDatePickerDialog(dateButton));

        builder.setTitle(isEditing ? "Editar Transação" : "Adicionar Nova Transação");
        if (isEditing) {
            nameEditText.setText(transactionToEdit.getName());
            amountEditText.setText(String.format(Locale.getDefault(), "%.2f", Math.abs(transactionToEdit.getAmount())));
            descriptionEditText.setText(transactionToEdit.getDescription());

            if (transactionToEdit.getDate() != null) {
                selectedDate.setTime(transactionToEdit.getDate());
                updateDateButtonText(dateButton);
            }

            if (transactionToEdit.getAmount() >= 0) {
                incomeRadioButton.setChecked(true);
            } else {
                expenseRadioButton.setChecked(true);
                expenseTagsContainer.setVisibility(View.VISIBLE);
                if ("Essencial".equals(transactionToEdit.getExpenseTag())) {
                    essentialRadioButton.setChecked(true);
                } else if ("Opcional".equals(transactionToEdit.getExpenseTag())) {
                    optionalRadioButton.setChecked(true);
                }
            }
        }

        builder.setView(view);
        builder.setPositiveButton(isEditing ? "Salvar" : "Adicionar", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString().replace(',', '.');
            String description = descriptionEditText.getText().toString();
            int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();

            if (name.isEmpty() || amountStr.isEmpty() || selectedTypeId == -1) {
                Toast.makeText(this, "Nome, valor e tipo são obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String type = selectedTypeId == R.id.radio_button_income ? "Ganho" : "Despesa";
            String expenseTag = null;

            if (type.equals("Despesa")) {
                amount *= -1; // Valor negativo para despesas
                int selectedTagId = expenseTagRadioGroup.getCheckedRadioButtonId();
                if (selectedTagId != -1) {
                    expenseTag = selectedTagId == R.id.radio_button_essential ? "Essencial" : "Opcional";
                }
            }

            FinanceTransaction transaction = new FinanceTransaction(name, amount, type, description, expenseTag, selectedDate.getTime());

            if (isEditing) {
                db.collection("users").document(currentUser.getUid()).collection("transactions").document(transactionToEdit.getId())
                        .set(transaction)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transação atualizada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show());
            } else {
                db.collection("users").document(currentUser.getUid()).collection("transactions")
                        .add(transaction)
                        .addOnSuccessListener(documentReference -> Toast.makeText(this, "Transação adicionada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao adicionar.", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void showDatePickerDialog(final Button dateButton) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtonText(dateButton);
        };

        new DatePickerDialog(this,
                dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtonText(Button dateButton) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(selectedDate.getTime()));
    }

    // ... (Restante do código: setupChart, updateUI, updateTotals, updateChart) ...
    private void setupChart() {
        earningsChart.getDescription().setEnabled(false);
        earningsChart.getLegend().setEnabled(false);
        earningsChart.setDrawValueAboveBar(true);

        XAxis xAxis = earningsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);

        earningsChart.getAxisLeft().setTextColor(Color.WHITE);
        earningsChart.getAxisRight().setEnabled(false);
    }

    private void updateUI() {
        updateTotals();
        updateChart();
        emptyView.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTotals() {
        double income = 0;
        double expense = 0;

        for (FinanceTransaction transaction : transactionList) {
            if (transaction.getAmount() >= 0) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }

        double balance = income + expense;

        balanceAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", balance));
        incomeAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", income));
        expenseAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", Math.abs(expense)));
    }

    private void updateChart() {
        if (transactionList.isEmpty()) {
            earningsChart.clear();
            earningsChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        double income = 0;
        double expense = 0;

        for (FinanceTransaction transaction : transactionList) {
            if (transaction.getAmount() >= 0) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }
        entries.add(new BarEntry(0, (float) income));
        entries.add(new BarEntry(1, (float) Math.abs(expense)));

        BarDataSet dataSet = new BarDataSet(entries, "Finanças");
        int[] colors = {
                ContextCompat.getColor(this, R.color.income_color),
                ContextCompat.getColor(this, R.color.expense_color)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        earningsChart.setData(barData);
        earningsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Ganhos", "Despesas"}));
        earningsChart.invalidate();
    }
}

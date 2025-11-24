package com.example.stilo;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagePlansActivity extends AppCompatActivity implements PlanAdapter.OnPlanListener {

    private TextInputEditText planNameEditText, planPriceEditText, planDescriptionEditText;
    private RadioGroup planDurationRadioGroup;
    private Spinner planCategorySpinner;
    private MaterialButton savePlanButton;
    private RecyclerView existingPlansRecyclerView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PlanAdapter planAdapter;
    private List<Plan> planList;
    private String editingPlanId = null; // Para rastrear o plano que está sendo editado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_plans);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        planNameEditText = findViewById(R.id.plan_name_edit_text);
        planPriceEditText = findViewById(R.id.plan_price_edit_text);
        planDescriptionEditText = findViewById(R.id.plan_description_edit_text);
        planDurationRadioGroup = findViewById(R.id.plan_duration_radio_group);
        planCategorySpinner = findViewById(R.id.plan_category_spinner);
        savePlanButton = findViewById(R.id.save_plan_button);
        existingPlansRecyclerView = findViewById(R.id.existing_plans_recycler_view);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.plan_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        planCategorySpinner.setAdapter(adapter);

        savePlanButton.setOnClickListener(v -> savePlan());

        setupRecyclerView();
        loadExistingPlans();
    }

    private void setupRecyclerView() {
        planList = new ArrayList<>();
        planAdapter = new PlanAdapter(this, planList, this);
        existingPlansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        existingPlansRecyclerView.setAdapter(planAdapter);
    }

    private void savePlan() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String planName = planNameEditText.getText().toString().trim();
        String planPrice = planPriceEditText.getText().toString().trim();
        String planDescription = planDescriptionEditText.getText().toString().trim();
        String planCategory = planCategorySpinner.getSelectedItem().toString();
        int selectedId = planDurationRadioGroup.getCheckedRadioButtonId();

        if (planName.isEmpty() || planPrice.isEmpty() || planDescription.isEmpty() || selectedId == -1) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String duration = selectedId == R.id.monthly_radio_button ? "Mensal" : "Anual";

        Map<String, Object> plan = new HashMap<>();
        plan.put("providerId", currentUser.getUid());
        plan.put("name", planName);
        plan.put("price", Double.parseDouble(planPrice));
        plan.put("description", planDescription);
        plan.put("category", planCategory);
        plan.put("duration", duration);

        if (editingPlanId == null) {
            // Criar um novo plano
            db.collection("plans").add(plan)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Plano salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadExistingPlans();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar o plano", Toast.LENGTH_SHORT).show());
        } else {
            // Atualizar um plano existente
            db.collection("plans").document(editingPlanId).set(plan)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Plano atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadExistingPlans();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar o plano", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadExistingPlans() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("plans").whereEqualTo("providerId", currentUser.getUid()).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                planList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Plan plan = document.toObject(Plan.class);
                    plan.setId(document.getId());
                    planList.add(plan);
                }
                planAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar os planos", Toast.LENGTH_SHORT).show());
    }

    private void clearForm() {
        planNameEditText.setText("");
        planPriceEditText.setText("");
        planDescriptionEditText.setText("");
        planCategorySpinner.setSelection(0);
        planDurationRadioGroup.clearCheck();
        editingPlanId = null;
        savePlanButton.setText("Salvar Plano");
    }

    @Override
    public void onEditClick(Plan plan) {
        planNameEditText.setText(plan.getName());
        planPriceEditText.setText(String.valueOf(plan.getPrice()));
        planDescriptionEditText.setText(plan.getDescription());
        if ("Mensal".equals(plan.getDuration())) {
            planDurationRadioGroup.check(R.id.monthly_radio_button);
        } else {
            planDurationRadioGroup.check(R.id.yearly_radio_button);
        }

        String[] categories = getResources().getStringArray(R.array.plan_categories);
        int categoryPosition = new ArrayList<>(Arrays.asList(categories)).indexOf(plan.getCategory());
        planCategorySpinner.setSelection(categoryPosition);

        editingPlanId = plan.getId();
        savePlanButton.setText("Atualizar Plano");
    }

    @Override
    public void onDeleteClick(Plan plan) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir Plano")
            .setMessage("Tem certeza que deseja excluir este plano?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                db.collection("plans").document(plan.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Plano excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        loadExistingPlans();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir o plano", Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}

package com.example.stilo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyPlansActivity extends AppCompatActivity implements SubscriptionAdapter.OnSubscriptionCancelListener {

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Subscription> subscriptionList;
    private List<Subscription> filteredSubscriptionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ChipGroup categoryChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_plans);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.my_plans_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        subscriptionList = new ArrayList<>();
        filteredSubscriptionList = new ArrayList<>();
        adapter = new SubscriptionAdapter(filteredSubscriptionList, this);
        recyclerView.setAdapter(adapter);

        categoryChipGroup = findViewById(R.id.category_chip_group);
        setupCategoryFilter();

        loadSubscriptions();
    }

    private void loadSubscriptions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("user_plans")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        subscriptionList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Subscription subscription = document.toObject(Subscription.class);
                            subscription.setId(document.getId());
                            fetchPlanDetails(subscription);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao carregar seus planos.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fetchPlanDetails(Subscription subscription) {
        db.collection("plans").document(subscription.getPlanId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String category = documentSnapshot.getString("category");
                subscription.setCategory(category);
            }
            subscriptionList.add(subscription);
            filterByCategory(); // Update the list after each item is loaded
        }).addOnFailureListener(e -> {
            // Handle failure, maybe add subscription with default/no category
            subscriptionList.add(subscription);
            filterByCategory();
        });
    }

    @SuppressWarnings("deprecation")
    private void setupCategoryFilter() {
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            filterByCategory();
        });
    }

    private void filterByCategory() {
        int checkedChipId = categoryChipGroup.getCheckedChipId();
        filteredSubscriptionList.clear();

        if (checkedChipId == -1) { // Use -1 instead of View.NO_ID
            filteredSubscriptionList.addAll(subscriptionList);
        } else {
            Chip selectedChip = findViewById(checkedChipId);
            if (selectedChip != null) {
                String category = selectedChip.getText().toString();
                for (Subscription sub : subscriptionList) {
                    if (category.equalsIgnoreCase(sub.getCategory())) {
                        filteredSubscriptionList.add(sub);
                    }
                }
            } else {
                filteredSubscriptionList.addAll(subscriptionList);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelClick(Subscription subscription) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Assinatura")
                .setMessage("Tem certeza que deseja cancelar o plano '" + subscription.getPlanName() + "'?")
                .setPositiveButton("Sim, cancelar", (dialog, which) -> {
                    db.collection("user_plans").document(subscription.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Assinatura cancelada.", Toast.LENGTH_SHORT).show();
                                // Remove from both lists
                                int position = filteredSubscriptionList.indexOf(subscription);
                                if (position != -1) {
                                    filteredSubscriptionList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                subscriptionList.remove(subscription);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Falha ao cancelar assinatura.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Não", null)
                .show();
    }
}

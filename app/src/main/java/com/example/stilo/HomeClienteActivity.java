package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeClienteActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView greetingTextView;
    private ImageButton profileIconButton;
    private AutoCompleteTextView searchBar;
    private LinearLayout agendamentosButton;
    private LinearLayout planosButton;
    private LinearLayout financeiroButton;
    private LinearLayout beneficiosButton;

    private Map<String, String> providerNameMap = new HashMap<>();
    private List<String> allProviderNames = new ArrayList<>();
    private ArrayAdapter<String> searchAdapter;

    private Map<String, Object> currentUserData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_cliente);

        initFirebase();
        initUI();
        setupClickListeners();
        loadUserData();
        fetchAllProviders();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initUI() {
        greetingTextView = findViewById(R.id.greeting_textview);
        profileIconButton = findViewById(R.id.profile_icon_button);
        searchBar = findViewById(R.id.search_bar);
        agendamentosButton = findViewById(R.id.agendamentos_button);
        planosButton = findViewById(R.id.planos_button);
        financeiroButton = findViewById(R.id.financeiro_button);
        beneficiosButton = findViewById(R.id.beneficios_button);

        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchBar.setAdapter(searchAdapter);
    }

    private void setupClickListeners() {
        profileIconButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
        });
        agendamentosButton.setOnClickListener(v -> startActivity(new Intent(HomeClienteActivity.this, MeusAgendamentosActivity.class)));
        planosButton.setOnClickListener(v -> startActivity(new Intent(HomeClienteActivity.this, MyPlansActivity.class)));
        financeiroButton.setOnClickListener(v -> startActivity(new Intent(HomeClienteActivity.this, FinancesActivity.class)));
        beneficiosButton.setOnClickListener(v -> startActivity(new Intent(HomeClienteActivity.this, PromotionsActivity.class)));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProviders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = searchAdapter.getItem(position);
            String providerId = providerNameMap.get(selectedName);

            if (providerId != null) {
                Intent intent = new Intent(HomeClienteActivity.this, ProviderProfileActivity.class);
                intent.putExtra("PROVIDER_ID", providerId);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUserData = documentSnapshot.getData();
                    String apelido = (String) currentUserData.get("apelido");
                    greetingTextView.setText("Olá, " + (apelido != null && !apelido.isEmpty() ? apelido : "Usuário"));
                    String imageUrl = (String) currentUserData.get("profileImageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(HomeClienteActivity.this).load(imageUrl).circleCrop().into(profileIconButton);
                    }
                }
            });
        }
    }

    private void fetchAllProviders() {
        db.collection("users").whereEqualTo("userType", "prestador").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                providerNameMap.clear();
                allProviderNames.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String uid = document.getId();
                    String name = document.getString("razaoSocial");
                    if (name == null || name.isEmpty()) {
                        name = document.getString("nomeFantasia");
                    }

                    if (name != null && !name.isEmpty()) {
                        allProviderNames.add(name);
                        providerNameMap.put(name, uid);
                    }
                }
            }
        });
    }

    private void filterProviders(String query) {
        List<String> filteredNames = new ArrayList<>();
        if (query != null && !query.isEmpty()) {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (String name : allProviderNames) {
                if (name.toLowerCase().contains(lowerCaseQuery)) {
                    filteredNames.add(name);
                }
            }
        }

        searchAdapter.clear();
        searchAdapter.addAll(filteredNames);
        searchAdapter.notifyDataSetChanged();
    }
}

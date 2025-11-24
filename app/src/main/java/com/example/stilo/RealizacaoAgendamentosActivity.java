package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RealizacaoAgendamentosActivity extends AppCompatActivity implements EstabelecimentoAdapter.OnItemClickListener {

    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private EstabelecimentoAdapter adapter;
    private List<Estabelecimento> estabelecimentoList = new ArrayList<>();

    private AutoCompleteTextView searchBar;
    private Button proximoButton;
    private MaterialToolbar toolbar;

    private Estabelecimento estabelecimentoSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realizacao_agendamentos);

        db = FirebaseFirestore.getInstance();

        initUI();
        setupListeners();
        fetchAllProviders();
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view_estabelecimentos);
        searchBar = findViewById(R.id.search_edit_text);
        proximoButton = findViewById(R.id.button_proximo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EstabelecimentoAdapter(this, estabelecimentoList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        proximoButton.setOnClickListener(v -> {
            if (estabelecimentoSelecionado != null) {
                Intent intent = new Intent(RealizacaoAgendamentosActivity.this, ProviderProfileActivity.class);
                intent.putExtra("PROVIDER_ID", estabelecimentoSelecionado.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor, selecione um estabelecimento.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllProviders() {
        db.collection("users").whereEqualTo("userType", "prestador").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                estabelecimentoList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Estabelecimento estabelecimento = new Estabelecimento();
                    estabelecimento.setId(document.getId());

                    String nome = document.getString("razaoSocial");
                    if (nome == null || nome.isEmpty()) {
                        nome = document.getString("nomeFantasia"); // Fallback para nome fantasia
                    }
                    estabelecimento.setNome(nome);

                    // Placeholder para o tipo/descrição, já que não temos esse campo ainda
                    estabelecimento.setTipo("Serviços de Barbearia e Cabelo");

                    // Placeholder para a avaliação
                    if (document.contains("rating")) {
                        estabelecimento.setRating(document.getDouble("rating").floatValue());
                    } else {
                        estabelecimento.setRating(4.5f); // Valor padrão
                    }

                    if (document.contains("profileImageUrl")) {
                        estabelecimento.setImageUrl(document.getString("profileImageUrl"));
                    }

                    estabelecimentoList.add(estabelecimento);
                }
                adapter.updateList(estabelecimentoList);
            } else {
                Toast.makeText(this, "Erro ao carregar estabelecimentos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Estabelecimento estabelecimento) {
        this.estabelecimentoSelecionado = estabelecimento;
        // O feedback visual (borda e checkmark) já é tratado dentro do adapter
    }
}

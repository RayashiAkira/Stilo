package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeusAgendamentosActivity extends AppCompatActivity implements AgendamentoAdapter.OnAgendamentoListener {

    private static final String TAG = "MeusAgendamentos";

    private RecyclerView recyclerView;
    private AgendamentoAdapter adapter;
    private List<Agendamento> agendamentoList;
    private TextView emptyView;
    private FloatingActionButton fabRealizarAgendamento;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_agendamentos);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view_meus_agendamentos);
        emptyView = findViewById(R.id.empty_view_agendamentos);
        fabRealizarAgendamento = findViewById(R.id.fab_realizar_agendamento);

        agendamentoList = new ArrayList<>();
        adapter = new AgendamentoAdapter(agendamentoList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabRealizarAgendamento.setOnClickListener(v -> {
            Intent intent = new Intent(MeusAgendamentosActivity.this, RealizacaoAgendamentosActivity.class);
            startActivity(intent);
        });

        loadAgendamentos();
    }

    private void loadAgendamentos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para ver seus agendamentos.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("appointments")
                .whereEqualTo("clientId", currentUser.getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    agendamentoList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Agendamento agendamento = document.toObject(Agendamento.class);
                        agendamento.setId(document.getId());
                        agendamentoList.add(agendamento);
                    }
                    adapter.notifyDataSetChanged();
                    checkIfListIsEmpty();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar agendamentos", e);
                    Toast.makeText(MeusAgendamentosActivity.this, "Erro ao carregar agendamentos.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onCancelClick(Agendamento agendamento) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Agendamento")
                .setMessage("Tem certeza que deseja cancelar este agendamento com " + agendamento.getProviderName() + "?")
                .setPositiveButton("Sim, cancelar", (dialog, which) -> cancelAppointment(agendamento))
                .setNegativeButton("Não", null)
                .show();
    }

    private void cancelAppointment(Agendamento agendamento) {
        DocumentReference appointmentRef = db.collection("appointments").document(agendamento.getId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("clientId", FieldValue.delete());
        updates.put("clientName", FieldValue.delete());
        updates.put("booked", false);

        appointmentRef.update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(MeusAgendamentosActivity.this, "Agendamento cancelado com sucesso!", Toast.LENGTH_SHORT).show();
                // Recarrega a lista para remover o item cancelado da UI do cliente
                int position = agendamentoList.indexOf(agendamento);
                if (position != -1) {
                    agendamentoList.remove(position);
                    adapter.notifyItemRemoved(position);
                    checkIfListIsEmpty();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MeusAgendamentosActivity.this, "Falha ao cancelar o agendamento.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao cancelar agendamento", e);
            });
    }

    private void checkIfListIsEmpty() {
        if (agendamentoList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setText("Você ainda não possui agendamentos.");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

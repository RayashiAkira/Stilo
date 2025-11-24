package com.example.stilo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgendarHorarioActivity extends AppCompatActivity implements HorarioDisponivelAdapter.OnHorarioClickListener {

    private static final String TAG = "AgendarHorarioActivity";

    private RecyclerView recyclerView;
    private HorarioDisponivelAdapter adapter;
    private List<Horario> horariosDisponiveis;
    private TextView emptyView;
    private TextView providerNameTitle;

    private String providerId;
    private String providerName;
    private String selectedDate;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_horario);

        // Receber dados do Intent
        providerId = getIntent().getStringExtra("PROVIDER_ID");
        providerName = getIntent().getStringExtra("PROVIDER_NAME");

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar Views
        CalendarView calendarView = findViewById(R.id.calendar_view_agendar);
        recyclerView = findViewById(R.id.recycler_view_horarios_disponiveis);
        emptyView = findViewById(R.id.empty_view_horarios_disponiveis);
        providerNameTitle = findViewById(R.id.provider_name_title);

        // Configurar o título
        if (providerName != null) {
            providerNameTitle.setText("com " + providerName);
        }

        // Configurar RecyclerView
        horariosDisponiveis = new ArrayList<>();
        adapter = new HorarioDisponivelAdapter(horariosDisponiveis, this);
        // Usar um GridLayoutManager para melhor visualização dos horários
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        // Inicializa a data e carrega os horários do dia atual
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(Calendar.getInstance().getTime());
        loadHorariosDisponiveis(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
            loadHorariosDisponiveis(selectedDate);
        });
    }

    private void loadHorariosDisponiveis(String date) {
        if (providerId == null) return;

        db.collection("available_hours")
                .document(providerId)
                .collection(date)
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    horariosDisponiveis.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Horario horario = document.toObject(Horario.class);
                        horario.setId(document.getId()); // Guardar o ID para futura remoção
                        horariosDisponiveis.add(horario);
                    }
                    adapter.notifyDataSetChanged();
                    checkIfListIsEmpty();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar horários disponíveis", e);
                    Toast.makeText(this, "Erro ao buscar horários.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onHorarioClick(Horario horario) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para agendar.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Agendamento")
                .setMessage("Deseja agendar com " + providerName + " no dia " + selectedDate + " às " + horario.getStartTime() + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    confirmarAgendamento(horario, currentUser);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarAgendamento(Horario horario, FirebaseUser cliente) {
        // Criar o objeto do novo agendamento
        Agendamento novoAgendamento = new Agendamento(providerId, providerName, cliente.getUid(), selectedDate, horario.getStartTime(), horario.getEndTime());

        // Usar um WriteBatch para garantir que as duas operações (criar e deletar) ocorram juntas
        WriteBatch batch = db.batch();

        // 1. Criar o novo agendamento na coleção 'appointments'
        DocumentReference appointmentRef = db.collection("appointments").document();
        batch.set(appointmentRef, novoAgendamento);

        // 2. Deletar o horário da lista de horários disponíveis do prestador
        DocumentReference availableHourRef = db.collection("available_hours")
                .document(providerId)
                .collection(selectedDate)
                .document(horario.getId());
        batch.delete(availableHourRef);

        // Executar o batch
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Agendamento confirmado com sucesso!", Toast.LENGTH_LONG).show();
            finish(); // Volta para a tela anterior
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro ao confirmar agendamento", e);
            Toast.makeText(this, "Ocorreu uma falha. Tente novamente.", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkIfListIsEmpty() {
        if (horariosDisponiveis.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

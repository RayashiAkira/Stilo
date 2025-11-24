package com.example.stilo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GerenciarAgendaActivity extends AppCompatActivity implements HorarioAdapter.OnHorarioListener {

    private static final String TAG = "GerenciarAgendaActivity";

    private RecyclerView recyclerViewHorarios;
    private HorarioAdapter horarioAdapter;
    private List<Horario> horariosList;
    private TextView emptyView;
    private String selectedDate; // Formato: dd-MM-yyyy

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_agenda);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(Calendar.getInstance().getTime());

        CalendarView calendarView = findViewById(R.id.calendar_view);
        recyclerViewHorarios = findViewById(R.id.recycler_view_horarios);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fabAddHorario = findViewById(R.id.fab_add_horario);

        horariosList = new ArrayList<>();
        horarioAdapter = new HorarioAdapter(horariosList, this);
        recyclerViewHorarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHorarios.setAdapter(horarioAdapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
            loadHorariosForDate(selectedDate);
        });

        fabAddHorario.setOnClickListener(v -> showAddHorarioDialog());

        loadHorariosForDate(selectedDate);
    }

    private void loadHorariosForDate(String date) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("available_hours")
                .document(currentUser.getUid())
                .collection(date)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    horariosList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Horario horario = document.toObject(Horario.class);
                        horario.setId(document.getId());
                        horariosList.add(horario);
                    }
                    horarioAdapter.notifyDataSetChanged();
                    checkIfListIsEmpty();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar horários", e);
                    Toast.makeText(GerenciarAgendaActivity.this, "Erro ao carregar horários.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddHorarioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_horario, null);
        builder.setView(dialogView);

        final TextInputEditText titleEditText = dialogView.findViewById(R.id.edit_text_title);
        final Spinner serviceTypeSpinner = dialogView.findViewById(R.id.spinner_service_type);
        final TextInputEditText startTimeEditText = dialogView.findViewById(R.id.edit_text_start_time);
        final TextInputEditText endTimeEditText = dialogView.findViewById(R.id.edit_text_end_time);
        final TextInputEditText priceEditText = dialogView.findViewById(R.id.edit_text_price);
        final TextInputEditText descriptionEditText = dialogView.findViewById(R.id.edit_text_description);
        final Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        final Button saveButton = dialogView.findViewById(R.id.button_save);

        // Configurar o Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(adapter);

        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String serviceType = serviceTypeSpinner.getSelectedItem().toString();
            String startTime = startTimeEditText.getText().toString().trim();
            String endTime = endTimeEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = 0.0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
                return;
            }

            Horario newHorario = new Horario(title, serviceType, startTime, endTime, price, description);
            saveHorarioToFirebase(newHorario);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveHorarioToFirebase(Horario horario) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("available_hours")
                .document(currentUser.getUid())
                .collection(selectedDate)
                .add(horario)
                .addOnSuccessListener(documentReference -> {
                    horario.setId(documentReference.getId());
                    // Adicionar na lista e reordenar
                    horariosList.add(horario);
                    horariosList.sort((h1, h2) -> h1.getStartTime().compareTo(h2.getStartTime()));
                    horarioAdapter.notifyDataSetChanged();
                    checkIfListIsEmpty();
                    Toast.makeText(this, "Horário adicionado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao salvar horário", e);
                    Toast.makeText(this, "Falha ao adicionar horário.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteClick(Horario horario) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || horario.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Você tem certeza que deseja excluir este horário?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    db.collection("available_hours")
                            .document(currentUser.getUid())
                            .collection(selectedDate)
                            .document(horario.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                horariosList.remove(horario);
                                horarioAdapter.notifyDataSetChanged();
                                checkIfListIsEmpty();
                                Toast.makeText(this, "Horário removido!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erro ao remover horário", e);
                                Toast.makeText(GerenciarAgendaActivity.this, "Falha ao remover horário.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkIfListIsEmpty() {
        if (horariosList.isEmpty()) {
            recyclerViewHorarios.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHorarios.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderProfileActivity extends AppCompatActivity implements HorarioVitrineAdapter.OnHorarioClickListener {

    private static final String TAG = "ProviderProfileActivity";

    // Views
    private ImageView providerProfileImage;
    private TextView providerNameText, providerDescriptionText, todayHoursTitle;
    private MaterialButton scheduleButton, submitCommentButton;
    private RecyclerView photosRecyclerView, commentsRecyclerView, plansRecyclerView, availableHoursRecyclerView;
    private RatingBar ratingBar;
    private TextInputEditText commentEditText;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Adapters e listas
    private PhotoGalleryAdapter photoAdapter;
    private ReviewAdapter reviewAdapter;
    private RecyclerView.Adapter planAdapter;
    private HorarioVitrineAdapter availableHoursAdapter;
    private List<String> photoUrls;
    private List<Review> reviewList;
    private List<Plan> planList;
    private List<Horario> availableHoursList;

    private String providerId;
    private String providerName;
    private String foundAvailableDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        providerId = getIntent().getStringExtra("PROVIDER_ID");

        initViews();
        setupRecyclerViews();
        loadProviderData();
        loadProviderPhotos();
        loadReviews();
        loadPlans();
        findNextAvailableHours(7);
        setupListeners();
    }

    private void initViews() {
        providerProfileImage = findViewById(R.id.provider_profile_image);
        providerNameText = findViewById(R.id.provider_name_text);
        providerDescriptionText = findViewById(R.id.provider_description_text);
        scheduleButton = findViewById(R.id.agendar_horario_button);
        photosRecyclerView = findViewById(R.id.photos_recycler_view);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        plansRecyclerView = findViewById(R.id.plans_recycler_view);
        availableHoursRecyclerView = findViewById(R.id.available_hours_recycler_view);
        todayHoursTitle = findViewById(R.id.today_hours_title);
        ratingBar = findViewById(R.id.rating_bar);
        commentEditText = findViewById(R.id.comment_input_edit_text);
        submitCommentButton = findViewById(R.id.submit_comment_button);
    }

    private void setupRecyclerViews() {
        // Photos RecyclerView
        photoUrls = new ArrayList<>();
        photoAdapter = new PhotoGalleryAdapter(this, photoUrls);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photosRecyclerView.setAdapter(photoAdapter);

        // Comments/Reviews RecyclerView
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(reviewAdapter);

        // Available Hours RecyclerView
        availableHoursList = new ArrayList<>();
        availableHoursAdapter = new HorarioVitrineAdapter(availableHoursList, this);
        availableHoursRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        availableHoursRecyclerView.setAdapter(availableHoursAdapter);

        // Plans RecyclerView
        planList = new ArrayList<>();
        plansRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && providerId.equals(currentUser.getUid())) {
            planAdapter = new PlanAdapter(this, planList, new PlanAdapter.OnPlanListener() {
                @Override
                public void onEditClick(Plan plan) {
                    // Lógica para editar
                }

                @Override
                public void onDeleteClick(Plan plan) {
                    // Lógica para deletar
                }
            });
        } else {
            planAdapter = new PlanAdapterCliente(this, planList, this::subscribeToPlan);
        }
        plansRecyclerView.setAdapter(planAdapter);
    }

    private void loadProviderData() {
        if (providerId != null) {
            db.collection("users").document(providerId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            providerName = documentSnapshot.getString("nomeFantasia"); // ou "razaoSocial"
                            providerNameText.setText(providerName);

                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            Glide.with(this).load(imageUrl).placeholder(R.drawable.sharp_account_circle_24).error(R.drawable.sharp_account_circle_24).into(providerProfileImage);

                            String description = documentSnapshot.getString("description");
                            if (description != null && !description.trim().isEmpty()) {
                                providerDescriptionText.setVisibility(View.VISIBLE);
                                providerDescriptionText.setText(description);
                            } else {
                                providerDescriptionText.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading provider data", e));
        }
    }

    private void findNextAvailableHours(int maxSearchDays) {
        findAndLoadHours(Calendar.getInstance(), maxSearchDays);
    }

    private void findAndLoadHours(Calendar calendar, int attemptsLeft) {
        if (attemptsLeft <= 0 || providerId == null) {
            todayHoursTitle.setVisibility(View.GONE);
            availableHoursRecyclerView.setVisibility(View.GONE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dateToSearch = sdf.format(calendar.getTime());

        db.collection("available_hours").document(providerId).collection(dateToSearch)
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        availableHoursList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Horario horario = document.toObject(Horario.class);
                            horario.setId(document.getId());
                            availableHoursList.add(horario);
                        }
                        availableHoursAdapter.notifyDataSetChanged();

                        foundAvailableDate = dateToSearch;
                        String title = "Horários disponíveis para " + (dateToSearch.equals(sdf.format(Calendar.getInstance().getTime())) ? "hoje" : dateToSearch);
                        todayHoursTitle.setText(title);
                        todayHoursTitle.setVisibility(View.VISIBLE);
                        availableHoursRecyclerView.setVisibility(View.VISIBLE);

                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        findAndLoadHours(calendar, attemptsLeft - 1);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar horários para " + dateToSearch, e);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    findAndLoadHours(calendar, attemptsLeft - 1);
                });
    }

    @Override
    public void onHorarioClick(Horario horario) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para agendar.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Agendamento")
                .setMessage("Deseja agendar com " + providerName + " no dia " + foundAvailableDate + " às " + horario.getStartTime() + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    confirmarAgendamento(horario, currentUser);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarAgendamento(Horario horario, FirebaseUser cliente) {
        Agendamento novoAgendamento = new Agendamento(providerId, providerName, cliente.getUid(), foundAvailableDate, horario.getStartTime(), horario.getEndTime());

        WriteBatch batch = db.batch();

        DocumentReference appointmentRef = db.collection("appointments").document();
        batch.set(appointmentRef, novoAgendamento);

        DocumentReference availableHourRef = db.collection("available_hours").document(providerId).collection(foundAvailableDate).document(horario.getId());
        batch.delete(availableHourRef);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Agendamento confirmado com sucesso!", Toast.LENGTH_LONG).show();
            findNextAvailableHours(7);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro ao confirmar agendamento", e);
            Toast.makeText(this, "Ocorreu uma falha. Tente novamente.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProviderPhotos() {
        if (providerId != null) {
            db.collection("users").document(providerId).collection("gallery_photos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    photoUrls.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String url = document.getString("url");
                        if (url != null) {
                            photoUrls.add(url);
                        }
                    }
                    photoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading photos", e));
        }
    }

    private void loadReviews() {
        if (providerId != null) {
            db.collection("users").document(providerId).collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviewList.add(review);
                    }
                    reviewAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading reviews", e));
        }
    }

    private void loadPlans() {
        if (providerId != null) {
            db.collection("plans").whereEqualTo("providerId", providerId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    planList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Plan plan = document.toObject(Plan.class);
                        plan.setId(document.getId());
                        planList.add(plan);
                    }
                    planAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading plans", e));
        }
    }

    private void submitReview() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para avaliar.", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Por favor, adicione uma nota.", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorId = currentUser.getUid();
        DocumentReference authorDoc = db.collection("users").document(authorId);

        authorDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String authorName = documentSnapshot.getString("apelido"); // ou "nomeCompleto"
                if (authorName == null) authorName = "Anônimo";

                Review review = new Review(authorName, comment, rating, Timestamp.now());

                db.collection("users").document(providerId).collection("reviews").add(review)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(ProviderProfileActivity.this, "Avaliação Concluída", Toast.LENGTH_SHORT).show();
                        reviewList.add(0, review); // Adiciona no início da lista
                        reviewAdapter.notifyItemInserted(0);
                        commentsRecyclerView.scrollToPosition(0);
                        // Limpa os campos
                        ratingBar.setRating(0);
                        commentEditText.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProviderProfileActivity.this, "Erro ao enviar avaliação.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupListeners() {
        scheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderProfileActivity.this, AgendarHorarioActivity.class);
            intent.putExtra("PROVIDER_ID", providerId);
            intent.putExtra("PROVIDER_NAME", providerName);
            startActivity(intent);
        });

        submitCommentButton.setOnClickListener(v -> submitReview());
    }

    private void subscribeToPlan(Plan plan) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para aderir a um plano.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userPlanRef = db.collection("user_plans").document(userId + "_" + plan.getId());

        Map<String, Object> subscription = new HashMap<>();
        subscription.put("userId", userId);
        subscription.put("planId", plan.getId());
        subscription.put("providerId", plan.getProviderId());
        subscription.put("planName", plan.getName());
        subscription.put("providerName", providerName);
        subscription.put("subscribedAt", Timestamp.now());

        userPlanRef.set(subscription)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Você aderiu ao plano " + plan.getName(), Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Falha ao aderir ao plano.", Toast.LENGTH_SHORT).show());
    }
}

package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomePrestadorActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    // UI Components
    private TextView greetingTextView;
    private ImageButton profileIconButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_prestador);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        greetingTextView = findViewById(R.id.greeting_textview);
        profileIconButton = findViewById(R.id.profile_icon_button);

        // Configura o botão para gerenciar a agenda
        LinearLayout manageScheduleButton = findViewById(R.id.schedule_button);
        manageScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePrestadorActivity.this, GerenciarAgendaActivity.class);
            startActivity(intent);
        });

        // Configura o botão para gerenciar fotos
        LinearLayout managePhotosButton = findViewById(R.id.photos_button);
        managePhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePrestadorActivity.this, ManagePhotosActivity.class);
            startActivity(intent);
        });

        // Configura o botão para gerenciar planos
        LinearLayout managePlansButton = findViewById(R.id.plans_button);
        managePlansButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePrestadorActivity.this, ManagePlansActivity.class);
            startActivity(intent);
        });

        // Configura o botão para finanças
        LinearLayout financesButton = findViewById(R.id.finances_button);
        financesButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePrestadorActivity.this, FinancesProviderActivity.class);
            startActivity(intent);
        });

        profileIconButton.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nomeFantasia = documentSnapshot.getString("nomeFantasia");
                    greetingTextView.setText("Olá, " + (nomeFantasia != null && !nomeFantasia.isEmpty() ? nomeFantasia : "Prestador"));
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(HomePrestadorActivity.this).load(imageUrl).circleCrop().into(profileIconButton);
                    }
                }
            });
        }
    }
}

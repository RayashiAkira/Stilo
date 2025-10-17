package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private TextView profileName, profileEmail, profilePhone, profileDob;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileDob = findViewById(R.id.profile_dob);
        logoutButton = findViewById(R.id.logout_button);

        loadUserProfile();

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            // Volta para a tela de autenticação
            Intent intent = new Intent(HomeActivity.this, AuthenticationActivity.class);
            // Limpa todas as atividades anteriores para que o usuário não possa voltar
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(uid);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Pega os dados do documento
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String dob = documentSnapshot.getString("dateOfBirth");

                    // Coloca os dados nos TextViews
                    profileName.setText(name);
                    profileEmail.setText(email);
                    profilePhone.setText(phone);
                    profileDob.setText(dob);

                } else {
                    Log.d(TAG, "Nenhum documento encontrado para este usuário");
                    Toast.makeText(this, "Não foi possível encontrar os dados do perfil.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar documento", e);
                Toast.makeText(this, "Erro ao carregar o perfil.", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
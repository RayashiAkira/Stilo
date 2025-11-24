package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private static final String TAG = "PerfilActivity";

    // Views
    private TextView profileTitle, profileUserType, profileMainName, profileSecondaryName;
    private TextView profileDocument, profileDob, profileEmail, profilePhone, profileAddress;
    private Button backButton, logoutButton;
    private FloatingActionButton fabEditProfile;
    private ImageView profileImageView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Outros
    private ActivityResultLauncher<Intent> editProfileActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar Views e Launchers
        initViews();
        setupEditProfileLauncher();
        loadUserProfile();

        // Configurar Listeners
        backButton.setOnClickListener(v -> navigateToHome());
        fabEditProfile.setOnClickListener(v -> openEditProfile());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void initViews() {
        profileTitle = findViewById(R.id.profile_title);
        profileUserType = findViewById(R.id.profile_user_type);
        profileMainName = findViewById(R.id.profile_main_name);
        profileSecondaryName = findViewById(R.id.profile_secondary_name);
        profileDocument = findViewById(R.id.profile_document);
        profileDob = findViewById(R.id.profile_dob);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileAddress = findViewById(R.id.profile_address);
        backButton = findViewById(R.id.back_button);
        logoutButton = findViewById(R.id.logout_button);
        fabEditProfile = findViewById(R.id.fab_edit_profile);
        profileImageView = findViewById(R.id.profile_image);
    }

    private void setupEditProfileLauncher() {
        editProfileActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Recria a atividade para aplicar o tema se o tipo de usuário mudar
                recreate();
            }
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    populateProfileData(documentSnapshot);
                } else {
                    Toast.makeText(this, "Dados do perfil não encontrados.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao carregar o perfil.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao buscar documento do usuário", e);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void populateProfileData(DocumentSnapshot doc) {
        String userType = doc.getString("userType");
        if ("cliente".equalsIgnoreCase(userType)) {
            profileTitle.setText("Perfil do Cliente");
            profileMainName.setText(doc.getString("nomeCompleto"));
            profileSecondaryName.setText(doc.getString("apelido"));
            profileDocument.setText(doc.getString("cpf"));
        } else if ("prestador".equalsIgnoreCase(userType)) {
            profileTitle.setText("Perfil do Prestador");
            profileMainName.setText(doc.getString("razaoSocial"));
            profileSecondaryName.setText(doc.getString("nomeFantasia"));
            profileDocument.setText(doc.getString("cnpj"));
        }

        profileUserType.setText(userType != null ? userType.substring(0, 1).toUpperCase() + userType.substring(1) : "Não definido");
        profileEmail.setText(doc.getString("email"));
        profilePhone.setText(doc.getString("phone"));
        profileDob.setText(doc.getString("dateOfBirth"));

        // Tratamento do Endereço (que é um mapa)
        Object addressObject = doc.get("address");
        if (addressObject instanceof Map) {
            Map<String, Object> addressMap = (Map<String, Object>) addressObject;
            String endereco = (String) addressMap.get("endereco");
            String numero = (String) addressMap.get("numero");
            String bairro = (String) addressMap.get("bairro");
            String cidade = (String) addressMap.get("cidade");
            String estado = (String) addressMap.get("estado");
            String cep = (String) addressMap.get("cep");

            String fullAddress = String.format("%s, %s - %s, %s/%s, %s", endereco, numero, bairro, cidade, estado, cep);
            profileAddress.setText(fullAddress);
        }

        String imageUrl = doc.getString("profileImageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.sharp_account_circle_24).error(R.drawable.sharp_account_circle_24).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.sharp_account_circle_24);
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(PerfilActivity.this, EditProfileActivity.class);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()) {
                    intent.putExtra("userType", documentSnapshot.getString("userType"));
                    editProfileActivityResultLauncher.launch(intent);
                }
            });
        }
    }

    private void navigateToHome() {
        String userType = ThemeManager.getUserType(this);
        Intent intent;
        if ("cliente".equalsIgnoreCase(userType)) {
            intent = new Intent(PerfilActivity.this, HomeClienteActivity.class);
        } else if ("prestador".equalsIgnoreCase(userType)) {
            intent = new Intent(PerfilActivity.this, HomePrestadorActivity.class);
        } else {
            intent = new Intent(PerfilActivity.this, AuthenticationActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        mAuth.signOut();
        ThemeManager.clearTheme(this);
        Intent intent = new Intent(PerfilActivity.this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

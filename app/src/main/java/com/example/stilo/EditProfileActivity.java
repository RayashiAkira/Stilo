package com.example.stilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    // Views
    private ShapeableImageView profileImageView;
    private Button changePhotoButton, saveChangesButton;
    private TextInputLayout secondaryNameLayout, phoneLayout, cepLayout, enderecoLayout, bairroLayout, numeroLayout, cidadeLayout, estadoLayout;
    private TextInputEditText secondaryNameEditText, emailEditText, phoneEditText, cepEditText, enderecoEditText, bairroEditText, numeroEditText, cidadeEditText, estadoEditText;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Image selection
    private Uri imageUri;
    private String downloadedImageUrl;
    private ActivityResultLauncher<String> mGetContent;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isInitialDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        initImagePicker();

        loadUserProfileData();

        changePhotoButton.setOnClickListener(v -> mGetContent.launch("image/*"));
        saveChangesButton.setOnClickListener(v -> saveProfileChanges());
        cepEditText.addTextChangedListener(new MaskTextWatcher(cepEditText, signuptabfragment.FORMAT_CEP));
        cepEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isInitialDataLoaded) {
                    return;
                }
                String cep = MaskTextWatcher.unmask(s.toString());
                if (cep.length() == 8) {
                    buscarCep(cep);
                }
            }
        });
    }

    private void initViews() {
        profileImageView = findViewById(R.id.edit_profile_image);
        changePhotoButton = findViewById(R.id.change_photo_button);
        secondaryNameLayout = findViewById(R.id.edit_secondary_name_layout);
        secondaryNameEditText = findViewById(R.id.edit_secondary_name);
        emailEditText = findViewById(R.id.edit_email);
        phoneLayout = findViewById(R.id.edit_phone_layout);
        phoneEditText = findViewById(R.id.edit_phone);
        cepLayout = findViewById(R.id.edit_cep_layout);
        cepEditText = findViewById(R.id.edit_cep);
        enderecoLayout = findViewById(R.id.edit_endereco_layout);
        enderecoEditText = findViewById(R.id.edit_endereco);
        bairroLayout = findViewById(R.id.edit_bairro_layout);
        bairroEditText = findViewById(R.id.edit_bairro);
        numeroLayout = findViewById(R.id.edit_numero_layout);
        numeroEditText = findViewById(R.id.edit_numero);
        cidadeLayout = findViewById(R.id.edit_cidade_layout);
        cidadeEditText = findViewById(R.id.edit_cidade);
        estadoLayout = findViewById(R.id.edit_estado_layout);
        estadoEditText = findViewById(R.id.edit_estado);
        saveChangesButton = findViewById(R.id.save_changes_button);
        progressBar = new ProgressBar(this); // Assuming you'll add a progress bar to your layout
    }

    private void initImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageUri = uri;
                profileImageView.setImageURI(imageUri);
                Toast.makeText(this, "Foto selecionada. Clique em salvar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");
                        if ("cliente".equalsIgnoreCase(userType)) {
                            secondaryNameLayout.setHint("Apelido");
                            secondaryNameEditText.setText(documentSnapshot.getString("apelido"));
                        } else {
                            secondaryNameLayout.setHint("Nome Fantasia");
                            secondaryNameEditText.setText(documentSnapshot.getString("nomeFantasia"));
                        }

                        emailEditText.setText(documentSnapshot.getString("email"));
                        phoneEditText.setText(documentSnapshot.getString("phone"));
                        cepEditText.setText(documentSnapshot.getString("cep"));
                        enderecoEditText.setText(documentSnapshot.getString("endereco"));
                        bairroEditText.setText(documentSnapshot.getString("bairro"));
                        numeroEditText.setText(documentSnapshot.getString("numero"));
                        cidadeEditText.setText(documentSnapshot.getString("cidade"));
                        estadoEditText.setText(documentSnapshot.getString("estado"));

                        downloadedImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (downloadedImageUrl != null && !downloadedImageUrl.isEmpty()) {
                            Glide.with(this).load(downloadedImageUrl).into(profileImageView);
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load user profile", task.getException());
                }
                isInitialDataLoaded = true;
            });
        }
    }

    private void saveProfileChanges() {
        saveChangesButton.setEnabled(false);
        if (imageUri != null) {
            uploadImageAndSaveChanges();
        } else {
            saveTextChangesOnly(downloadedImageUrl); // Save with existing image URL
        }
    }

    private void uploadImageAndSaveChanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        StorageReference storageRef = storage.getReference().child("profile_images/" + currentUser.getUid());
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            downloadedImageUrl = uri.toString();
            saveTextChangesOnly(downloadedImageUrl);
        })).addOnFailureListener(e -> {
            Toast.makeText(EditProfileActivity.this, "Falha no upload da imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
            saveChangesButton.setEnabled(true);
        });
    }

    private void saveTextChangesOnly(String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Erro: Nenhum usuário logado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String secondaryName = secondaryNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String cep = cepEditText.getText().toString().trim();
        String endereco = enderecoEditText.getText().toString().trim();
        String bairro = bairroEditText.getText().toString().trim();
        String numero = numeroEditText.getText().toString().trim();
        String cidade = cidadeEditText.getText().toString().trim();
        String estado = estadoEditText.getText().toString().trim();

        if (phone.isEmpty() || cep.isEmpty() || endereco.isEmpty() || bairro.isEmpty() || numero.isEmpty() || cidade.isEmpty() || estado.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            saveChangesButton.setEnabled(true);
            return;
        }

        String userType = getIntent().getStringExtra("userType");
        Map<String, Object> updatedData = new HashMap<>();

        if ("cliente".equalsIgnoreCase(userType)) {
            updatedData.put("apelido", secondaryName);
        } else {
            updatedData.put("nomeFantasia", secondaryName);
        }
        updatedData.put("phone", phone);
        updatedData.put("cep", cep);
        updatedData.put("endereco", endereco);
        updatedData.put("bairro", bairro);
        updatedData.put("numero", numero);
        updatedData.put("cidade", cidade);
        updatedData.put("estado", estado);
        if (imageUrl != null) {
            updatedData.put("profileImageUrl", imageUrl);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent());
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Falha ao atualizar o perfil.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating document", e);
                    saveChangesButton.setEnabled(true);
                });
    }

    private void buscarCep(String cep) {
        executorService.execute(() -> {
            try {
                URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (jsonResponse.has("erro")) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "CEP não encontrado.", Toast.LENGTH_SHORT).show());
                    return;
                }

                String logradouro = jsonResponse.getString("logradouro");
                String bairro = jsonResponse.getString("bairro");
                String localidade = jsonResponse.getString("localidade");
                String uf = jsonResponse.getString("uf");

                runOnUiThread(() -> {
                    enderecoEditText.setText(logradouro);
                    bairroEditText.setText(bairro);
                    cidadeEditText.setText(localidade);
                    estadoEditText.setText(uf);
                });

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Erro ao buscar CEP: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Erro ao buscar CEP.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}

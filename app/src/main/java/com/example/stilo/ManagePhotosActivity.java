package com.example.stilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManagePhotosActivity extends AppCompatActivity implements ManagePhotosAdapter.OnPhotoDeleteListener {

    private static final String TAG = "ManagePhotosActivity";

    private RecyclerView photosRecyclerView;
    private FloatingActionButton addPhotoFab;
    private TextInputEditText descriptionEditText;
    private Button saveDescriptionButton;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private DocumentReference userDocRef;

    private ManagePhotosAdapter adapter;
    private List<Photo> photoList;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadImageToStorage(imageUri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_photos);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userDocRef = db.collection("users").document(currentUser.getUid());
        }

        initViews();
        setupRecyclerView();
        loadPhotos();
        if (currentUser != null) {
            loadCurrentDescription();
        }
        setupListeners();
    }

    private void initViews() {
        photosRecyclerView = findViewById(R.id.photos_recycler_view);
        addPhotoFab = findViewById(R.id.fab_add_photo);
        descriptionEditText = findViewById(R.id.description_edit_text);
        saveDescriptionButton = findViewById(R.id.save_description_button);
    }

    private void setupRecyclerView() {
        photoList = new ArrayList<>();
        adapter = new ManagePhotosAdapter(this, photoList, this);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photosRecyclerView.setAdapter(adapter);
    }

    private void loadPhotos() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("gallery_photos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    photoList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Photo photo = document.toObject(Photo.class);
                        photo.setId(document.getId());
                        photoList.add(photo);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading photos", e));
    }

    private void setupListeners() {
        addPhotoFab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        saveDescriptionButton.setOnClickListener(v -> saveDescription());
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child("gallery_images/" + userId + "/" + UUID.randomUUID().toString());

        photoRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePhotoUrlToFirestore(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(ManagePhotosActivity.this, "Falha no upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePhotoUrlToFirestore(String url) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> photoData = new HashMap<>();
        photoData.put("url", url);

        db.collection("users").document(userId).collection("gallery_photos")
                .add(photoData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ManagePhotosActivity.this, "Foto adicionada!", Toast.LENGTH_SHORT).show();
                    loadPhotos(); // Recarrega as fotos
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManagePhotosActivity.this, "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCurrentDescription() {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentDescription = documentSnapshot.getString("description");
                if (currentDescription != null) {
                    descriptionEditText.setText(currentDescription);
                }
            }
        });
    }

    private void saveDescription() {
        String newDescription = descriptionEditText.getText().toString().trim();
        userDocRef.update("description", newDescription)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManagePhotosActivity.this, "Descrição salva com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ManagePhotosActivity.this, "Erro ao salvar a descrição.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPhotoDelete(Photo photo) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        // Deleta a referência no Firestore
        db.collection("users").document(userId).collection("gallery_photos").document(photo.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Deleta o arquivo no Storage
                    StorageReference photoRef = storage.getReferenceFromUrl(photo.getUrl());
                    photoRef.delete().addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(ManagePhotosActivity.this, "Foto removida.", Toast.LENGTH_SHORT).show();
                        loadPhotos(); // Recarrega
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ManagePhotosActivity.this, "Erro ao remover a imagem.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManagePhotosActivity.this, "Erro ao remover a foto.", Toast.LENGTH_SHORT).show();
                });
    }
}

class Photo {
    private String id;
    private String url;

    public Photo() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

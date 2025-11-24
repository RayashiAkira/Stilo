package com.example.stilo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.stilo.User;

import java.util.ArrayList;
import java.util.List;

public class EncontrarClienteActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewClients;
    private ClienteAdapter clienteAdapter;
    private List<User> clientList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView noClientsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encontrar_cliente);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewClients = findViewById(R.id.recycler_view_clients);
        progressBar = findViewById(R.id.progress_bar);
        noClientsTextView = findViewById(R.id.no_clients_textview);

        setupRecyclerView();
        loadClients();
    }

    private void setupRecyclerView() {
        clienteAdapter = new ClienteAdapter(this, clientList);
        recyclerViewClients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClients.setAdapter(clienteAdapter);
    }

    private void loadClients() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").whereEqualTo("userType", "Cliente").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    clientList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        clientList.add(user);
                    }
                    clienteAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    if (clientList.isEmpty()) {
                        noClientsTextView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    noClientsTextView.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
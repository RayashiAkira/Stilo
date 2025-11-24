package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity implements ProviderAdapter.OnProviderClickListener {

    private static final String TAG = "SearchResultsActivity";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProviderAdapter adapter;
    private List<Provider> providerList;
    private TextView noResultsTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_view_search_results);
        noResultsTextView = findViewById(R.id.no_results_textview);
        progressBar = findViewById(R.id.progress_bar_search);

        providerList = new ArrayList<>();
        adapter = new ProviderAdapter(this, providerList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String query = getIntent().getStringExtra("SEARCH_QUERY");
        if (query != null && !query.isEmpty()) {
            searchProviders(query);
        }
    }

    private void searchProviders(String query) {
        progressBar.setVisibility(View.VISIBLE);
        noResultsTextView.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("userType", "Prestador de servico")
                .orderBy("nomeFantasia")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    providerList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Provider provider = document.toObject(Provider.class);
                        provider.setUid(document.getId());
                        providerList.add(provider);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching providers: ", e);
                    updateUI();
                });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        if (providerList.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onProviderClick(Provider provider) {
        Intent intent = new Intent(this, ProviderProfileActivity.class);
        intent.putExtra("PROVIDER_ID", provider.getUid());
        startActivity(intent);
    }
}

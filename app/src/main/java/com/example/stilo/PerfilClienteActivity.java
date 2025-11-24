package com.example.stilo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class PerfilClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_cliente);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView profileImage = findViewById(R.id.profile_image);
        TextView nameTextView = findViewById(R.id.name_textview);
        TextView emailTextView = findViewById(R.id.email_textview);

        String name = getIntent().getStringExtra("clientName");
        String email = getIntent().getStringExtra("clientEmail");
        String imageUrl = getIntent().getStringExtra("clientImageUrl");

        nameTextView.setText(name);
        emailTextView.setText(email);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).circleCrop().into(profileImage);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(v -> finish());
    }
}

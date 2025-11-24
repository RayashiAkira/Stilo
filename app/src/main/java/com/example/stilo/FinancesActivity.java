package com.example.stilo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FinancesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finances);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_finances, new FinancesFragment())
                    .commit();
        }
    }
}

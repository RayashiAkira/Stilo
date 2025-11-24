package com.example.stilo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AdvantageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_favorites);
    }
}
package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // O tema deve ser aplicado ANTES de setContentView
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Se o usuário já está logado E a preferência de tema existe, vá para a home
        if (currentUser != null && ThemeManager.getUserType(this) != null) {
            navigateToCorrectHome(currentUser.getUid());
        } else {
            // Senão, mostre a tela de login
            if (savedInstanceState == null) {
                loadFragment(new logintabfragment(), false);
            }
        }
    }

    private void navigateToCorrectHome(String uid) {
        String userType = ThemeManager.getUserType(this);
        Intent intent;
        if ("cliente".equalsIgnoreCase(userType)) {
            intent = new Intent(this, HomeClienteActivity.class);
        } else if ("prestador".equalsIgnoreCase(userType)) {
            intent = new Intent(this, HomePrestadorActivity.class);
        } else {
            // Fallback: se o tipo de usuário for desconhecido, limpe e volte ao login
            ThemeManager.clearTheme(this);
            loadFragment(new logintabfragment(), false);
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void navigateToSignup() {
        loadFragment(new signuptabfragment(), true);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public void navigateToLogin() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            loadFragment(new logintabfragment(), false);
        }
    }
}

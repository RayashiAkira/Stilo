package com.example.stilo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.splashscreen.SplashScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends Activity { // Alterado de AppCompatActivity para Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instala e gerencia a transição da Splash Screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Não é mais necessário chamar setContentView() aqui.
        // O tema da splash screen cuida do visual inicial.
        // A lógica de redirecionamento é chamada imediatamente.

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            redirectTo(Apresentacao1Activity.class);
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                checkUserTypeAndRedirect(currentUser.getUid());
            } else {
                redirectTo(AuthenticationActivity.class);
            }
        }
    }

    private void checkUserTypeAndRedirect(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userType = documentSnapshot.getString("userType");
                    if ("Prestador".equalsIgnoreCase(userType)) { // Alterado para equalsIgnoreCase
                        redirectTo(HomePrestadorActivity.class);
                    } else {
                        redirectTo(HomeClienteActivity.class);
                    }
                } else {
                    redirectTo(AuthenticationActivity.class);
                }
            })
            .addOnFailureListener(e -> {
                redirectTo(AuthenticationActivity.class);
            });
    }

    private void redirectTo(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); 
    }
}

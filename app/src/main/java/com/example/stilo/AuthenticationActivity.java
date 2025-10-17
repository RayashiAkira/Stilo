package com.example.stilo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.Log; // Certifique-se que está usando android.util.Log se essa for a intenção

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity);

        if (savedInstanceState == null) {
            loadFragment(new logintabfragment(), false);
        }
    }

    public void navigateToSignup() {
        Log.d("AuthActivity", "navigateToSignup called");
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
        Log.d("AuthActivity", "Fragment transaction committed for: " + fragment.getClass().getSimpleName());
    }

    public void navigateToLogin() {
        Log.d("AuthActivity", "navigateToLogin called");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            Log.d("AuthActivity", "Popped backstack.");
        } else {
            Log.d("AuthActivity", "Backstack vazia, carregando LoginFragment.");
            loadFragment(new logintabfragment(), false);
        }
    } // Fim do método navigateToLogin

} // Fim da classe AuthenticationActivity

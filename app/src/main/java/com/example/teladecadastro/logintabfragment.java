package com.example.teladecadastro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class logintabfragment extends Fragment {

    private static final String TAG = "LoginFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logintabfragment, container, false);

        Button loginButton = view.findViewById(R.id.login_button);
        TextView goToSignupText = view.findViewById(R.id.text_go_to_signup);

        Log.d(TAG, "onCreateView: goToSignupText is " + (goToSignupText == null ? "NULL" : "NOT NULL"));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                // Lógica de login
            }
        });

        // ...
        if (goToSignupText != null) {
            goToSignupText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "goToSignupText CLICADO!");
                    if (getActivity() != null) {
                        Log.d(TAG, "Activity: " + getActivity().getClass().getSimpleName());
                        // ***** CORREÇÃO AQUI *****
                        if (getActivity() instanceof AuthenticationActivity) {
                            Log.d(TAG, "Chamando navigateToSignup() na AuthenticationActivity...");
                            ((AuthenticationActivity) getActivity()).navigateToSignup();
                        } else {
                            // Este else não deve mais ser alcançado se AuthenticationActivity for a host
                            Log.e(TAG, "ERRO: A Activity não é uma instância de AuthenticationActivity! É: " + getActivity().getClass().getName());
                        }
                    } else {
                        Log.e(TAG, "ERRO: getActivity() retornou NULL!");
                    }
                }
            });
        }
        return view;
    }
}

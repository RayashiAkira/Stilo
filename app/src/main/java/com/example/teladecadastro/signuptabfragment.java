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

public class signuptabfragment extends Fragment {

    private static final String TAG = "SignupFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signuptabfragment, container, false);

        Button signupButton = view.findViewById(R.id.signup_button);
        TextView goToLoginText = view.findViewById(R.id.text_go_to_login);

        Log.d(TAG, "onCreateView: goToLoginText is " + (goToLoginText == null ? "NULL" : "NOT NULL"));

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Signup button clicked");
                // Lógica de cadastro
            }
        });

        // ...
        if (goToLoginText != null) {
            goToLoginText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "goToLoginText CLICADO!");
                    if (getActivity() != null) {
                        Log.d(TAG, "Activity: " + getActivity().getClass().getSimpleName());
                        // ***** CORREÇÃO AQUI *****
                        if (getActivity() instanceof AuthenticationActivity) {
                            Log.d(TAG, "Chamando navigateToLogin() na AuthenticationActivity...");
                            ((AuthenticationActivity) getActivity()).navigateToLogin();
                        } else {
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

package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class logintabfragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView goToSignupText;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logintabfragment, container, false);

        // Inicializar a instância do Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.login_email);
        passwordEditText = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.login_button);
        goToSignupText = view.findViewById(R.id.text_go_to_signup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), task -> {
                            if (task.isSuccessful()) {
                                // Login bem-sucedido
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                                // Navegar para a HomeActivity
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish(); // Finaliza a Activity de autenticação
                            } else {
                                // Se o login falhar, exibir uma mensagem ao usuário
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Falha no login. Verifique seu e-mail e senha.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        if (goToSignupText != null) {
            goToSignupText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        if (getActivity() instanceof AuthenticationActivity) {
                            ((AuthenticationActivity) getActivity()).navigateToSignup();
                        }
                    }
                }
            });
        }
        return view;
    }
}
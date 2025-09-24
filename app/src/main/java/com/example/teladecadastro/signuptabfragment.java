package com.example.teladecadastro;

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

public class signuptabfragment extends Fragment {

    private static final String TAG = "SignupFragment";
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private TextView goToLoginText;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signuptabfragment, container, false);

        // Inicializar a instância do Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        nameEditText = view.findViewById(R.id.nome);
        emailEditText = view.findViewById(R.id.signup_email);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginText = view.findViewById(R.id.text_go_to_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chamar o método de criação de usuário do Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), task -> {
                            if (task.isSuccessful()) {
                                // Cadastro bem-sucedido
                                Log.d(TAG, "createUserWithEmail:success");
                                Toast.makeText(getContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                                // Navegar de volta para a tela de login
                                if (getActivity() instanceof AuthenticationActivity) {
                                    ((AuthenticationActivity) getActivity()).navigateToLogin();
                                }
                            } else {
                                // Se o cadastro falhar, exibir uma mensagem ao usuário
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Falha no cadastro. Tente novamente.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        if (goToLoginText != null) {
            goToLoginText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        if (getActivity() instanceof AuthenticationActivity) {
                            ((AuthenticationActivity) getActivity()).navigateToLogin();
                        }
                    }
                }
            });
        }
        return view;
    }
}
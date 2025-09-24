package com.example.teladecadastro;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signuptabfragment extends Fragment {

    private static final String TAG = "SignupFragment";
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private TextView goToLoginText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signuptabfragment, container, false);

        // Inicializar a instância do Firebase Auth e Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = view.findViewById(R.id.nome);
        emailEditText = view.findViewById(R.id.signup_email);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginText = view.findViewById(R.id.text_go_to_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // Salva o nome de usuário no Cloud Firestore
                                if (user != null) {
                                    saveUserDataToFirestore(user.getUid(), name, email);
                                }

                                // Navegar de volta para a tela de login
                                if (getActivity() instanceof AuthenticationActivity) {
                                    ((AuthenticationActivity) getActivity()).navigateToLogin();
                                }
                            } else {
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

    private void saveUserDataToFirestore(String userId, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);

        // Adiciona um novo documento à coleção "users" com o UID do usuário como ID do documento
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documento salvo com sucesso!");
                    Toast.makeText(getContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao salvar documento", e);
                    Toast.makeText(getContext(), "Falha ao salvar dados de usuário.", Toast.LENGTH_SHORT).show();
                });
    }
}
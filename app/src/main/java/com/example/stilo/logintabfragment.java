package com.example.stilo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class logintabfragment extends Fragment {

    private EditText email, password;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private ProgressBar loginProgressBar;
    private TextView signUpText, forgotPass, loginTitle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private int secretClickCount = 0;
    private long lastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logintabfragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.button);
        rememberMeCheckBox = view.findViewById(R.id.remember_me_checkbox);
        loginProgressBar = view.findViewById(R.id.login_progress_bar);
        signUpText = view.findViewById(R.id.sign_up_text);
        forgotPass = view.findViewById(R.id.forgot_pass);
        loginTitle = view.findViewById(R.id.login_title);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();
            
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(emailText, passwordText);
        });

        signUpText.setOnClickListener(v -> {
            if (getActivity() instanceof AuthenticationActivity) {
                ((AuthenticationActivity) getActivity()).navigateToSignup();
            }
        });

        forgotPass.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        // Lógica secreta para abrir a tela de apresentação
        loginTitle.setOnClickListener(v -> {
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - lastClickTime > 1000) { // Reseta se o clique for demorado
                secretClickCount = 1;
            } else {
                secretClickCount++;
            }
            lastClickTime = currentTime;

            if (secretClickCount == 5) {
                secretClickCount = 0;
                Toast.makeText(getContext(), "Acesso secreto concedido!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), Apresentacao1Activity.class);
                startActivity(intent);
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_forgot_password, null, false);
        final TextInputEditText emailInput = view.findViewById(R.id.email_input);

        builder.setView(view)
                .setTitle("Redefinir Senha")
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor, insira seu e-mail.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        loginProgressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loginProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "E-mail de redefinição de senha enviado para " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Falha ao enviar e-mail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        loginProgressBar.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.INVISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    navigateToCorrectHome(user.getUid());
                }
            } else {
                Toast.makeText(getContext(), "Falha na autenticação: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                loginProgressBar.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateToCorrectHome(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userType = documentSnapshot.getString("userType");

                // Salva o tipo de usuário para carregar o tema correto da próxima vez
                ThemeManager.setUserType(getActivity(), userType);

                Intent intent;
                if ("cliente".equalsIgnoreCase(userType)) {
                    intent = new Intent(getActivity(), HomeClienteActivity.class);
                } else if ("prestador".equalsIgnoreCase(userType)) {
                    intent = new Intent(getActivity(), HomePrestadorActivity.class);
                } else {
                    Toast.makeText(getContext(), "Tipo de usuário desconhecido.", Toast.LENGTH_SHORT).show();
                    loginProgressBar.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                    return;
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            } else {
                Toast.makeText(getContext(), "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                loginProgressBar.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Erro ao buscar dados do usuário.", Toast.LENGTH_SHORT).show();
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        });
    }
}

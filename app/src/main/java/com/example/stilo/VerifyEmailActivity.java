package com.example.stilo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // 1. ADICIONADO: Import do Firestore

public class VerifyEmailActivity extends AppCompatActivity {

    private static final String TAG = "VerifyEmailActivity";
    private TextView verifyMessageTextView, backToLoginButton;
    private Button resendEmailButton, checkVerificationButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // 2. ADICIONADO: Instância do Firestore
    private Handler handler;
    private Runnable verificationChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // 3. ADICIONADO: Inicialização do Firestore

        verifyMessageTextView = findViewById(R.id.verify_email_message);
        resendEmailButton = findViewById(R.id.resend_email_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);
        checkVerificationButton = findViewById(R.id.check_verification_button);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            verifyMessageTextView.setText("Enviamos um link de confirmação para o seu e-mail:\n" + user.getEmail());
        }

        resendEmailButton.setOnClickListener(v -> {
            if (user != null) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(VerifyEmailActivity.this, "Novo e-mail de verificação enviado.", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "sendEmailVerification", task.getException());
                                Toast.makeText(VerifyEmailActivity.this, "Falha ao reenviar e-mail.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // O botão manual agora também chama o mesmo método da verificação automática
        checkVerificationButton.setOnClickListener(v -> checkEmailVerification());

        backToLoginButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(VerifyEmailActivity.this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        handler = new Handler(Looper.getMainLooper());
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Verificando status do e-mail...");
                checkEmailVerification();
                // Agenda a próxima verificação para 5 segundos
                handler.postDelayed(this, 5000);
            }
        };
    }

    // 4. ALTERADO: A lógica de verificação agora chama um método para checar o tipo de usuário
    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Atualiza a referência do usuário após o 'reload'
                    FirebaseUser freshUser = mAuth.getCurrentUser();
                    if (freshUser != null && freshUser.isEmailVerified()) {
                        // Para o loop de verificação para não executar novamente
                        handler.removeCallbacks(verificationChecker);

                        Toast.makeText(VerifyEmailActivity.this, "E-mail verificado com sucesso!", Toast.LENGTH_SHORT).show();

                        // AGORA, VAMOS CHECAR O TIPO DE USUÁRIO ANTES DE REDIRECIONAR
                        checkUserTypeAndRedirect(freshUser.getUid());
                    }
                } else {
                    Log.e(TAG, "reload failed", task.getException());
                    // Possivelmente o usuário foi deletado, então deslogar
                    mAuth.signOut();
                    Intent intent = new Intent(VerifyEmailActivity.this, AuthenticationActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    // 5. NOVO MÉTODO: Lógica para buscar o tipo de usuário e redirecionar
    private void checkUserTypeAndRedirect(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");
                        Log.d(TAG, "Tipo de usuário encontrado: " + userType);

                        Intent intent;
                        if ("cliente".equalsIgnoreCase(userType)) {
                            intent = new Intent(VerifyEmailActivity.this, HomeClienteActivity.class);
                        } else if ("prestador".equalsIgnoreCase(userType)) {
                            intent = new Intent(VerifyEmailActivity.this, HomePrestadorActivity.class);
                        } else {
                            // Caso de segurança: se o tipo não for encontrado, vai para uma tela padrão
                            Log.w(TAG, "Tipo de usuário desconhecido ou nulo. Redirecionando para PerfilActivity.");
                            intent = new Intent(VerifyEmailActivity.this, PerfilActivity.class);
                        }

                        // Limpa todas as activities anteriores (login, cadastro, etc.) da pilha
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // Erro crítico: usuário autenticado mas sem dados no Firestore
                        Log.e(TAG, "Usuário autenticado, mas sem documento no Firestore. UID: " + userId);
                        Toast.makeText(this, "Erro ao carregar dados do usuário. Tente fazer login novamente.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        // Redireciona para o login
                        Intent intent = new Intent(VerifyEmailActivity.this, AuthenticationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha ao buscar documento do usuário", e);
                    Toast.makeText(this, "Erro de conexão. Verifique sua internet.", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Inicia o verificador quando a atividade está visível
        handler.post(verificationChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Para o verificador quando a atividade não está mais visível
        handler.removeCallbacks(verificationChecker);
    }
}
package com.example.stilo;

import android.app.DatePickerDialog;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class signuptabfragment extends Fragment {

    private static final String TAG = "SignupFragment";
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, dataNascimentoEditText, telefoneEditText;
    private Button signupButton;
    private TextView goToLoginText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Calendar myCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signuptabfragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = view.findViewById(R.id.nome);
        emailEditText = view.findViewById(R.id.signup_email);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm);
        dataNascimentoEditText = view.findViewById(R.id.data_nascimento);
        telefoneEditText = view.findViewById(R.id.telefone);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginText = view.findViewById(R.id.text_go_to_login);

        // Configuração do DatePickerDialog
        DatePickerDialog.OnDateSetListener date = (view1, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };
        dataNascimentoEditText.setOnClickListener(v -> new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());


        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String dob = dataNascimentoEditText.getText().toString().trim();
            String phone = telefoneEditText.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || dob.isEmpty() || phone.isEmpty()) {
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
                            if (user != null) {
                                saveUserDataToFirestore(user.getUid(), name, email, dob, phone);
                            }
                            if (getActivity() instanceof AuthenticationActivity) {
                                ((AuthenticationActivity) getActivity()).navigateToLogin();
                            }
                        } else {
                            // AQUI ESTÁ A NOVA LÓGICA DE TRATAMENTO DE ERROS
                            String errorMessage = "Falha no cadastro. Tente novamente."; // Mensagem padrão
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                errorMessage = "A senha é muito fraca. Use pelo menos 6 caracteres.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "O formato do email é inválido.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "Este email já está cadastrado.";
                            } catch (Exception e) {
                                Log.e(TAG, "createUserWithEmail:failure", e);
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        if (goToLoginText != null) {
            goToLoginText.setOnClickListener(v -> {
                if (getActivity() instanceof AuthenticationActivity) {
                    ((AuthenticationActivity) getActivity()).navigateToLogin();
                }
            });
        }
        return view;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));
        dataNascimentoEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void saveUserDataToFirestore(String userId, String name, String email, String dob, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("dateOfBirth", dob);
        userData.put("phone", phone);

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
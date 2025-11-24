package com.example.stilo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PromotionsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userRef;

    private TextView userCoinsBalance;
    private LinearLayout dailyCheckInContainer;
    private TextView viewHistoryButton;
    private Button redeemButton; // Renomeado para checkInButton

    private final int[] dailyRewards = {20, 110, 45, 45, 80, 70, 70};

    // Variáveis de estado para a lógica de check-in
    private int currentCheckInDayIndex = -1;
    private boolean canUserCheckIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_promotions);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userRef = db.collection("users").document(currentUser.getUid());

        initViews();
        loadUserData();
    }

    private void initViews() {
        userCoinsBalance = findViewById(R.id.user_coins_balance);
        dailyCheckInContainer = findViewById(R.id.daily_check_in_container);
        ImageButton backButton = findViewById(R.id.back_button_promotions);
        backButton.setOnClickListener(v -> finish());

        viewHistoryButton = findViewById(R.id.view_history_button);
        viewHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(PromotionsActivity.this, RedemptionHistoryActivity.class);
            startActivity(intent);
        });

        redeemButton = findViewById(R.id.redeem_button);
        redeemButton.setOnClickListener(v -> {
            if (canUserCheckIn && currentCheckInDayIndex != -1) {
                redeemButton.setEnabled(false);
                performCheckIn(currentCheckInDayIndex);
            } else {
                Toast.makeText(PromotionsActivity.this, "Você já realizou o check-in hoje.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    long coins = document.getLong("coins") != null ? document.getLong("coins") : 0;
                    userCoinsBalance.setText(String.valueOf(coins));
                    updateCheckInUI(document);
                } else {
                    // Lida com o caso de novo usuário sem dados de check-in
                    updateCheckInUI(null);
                }
            } else {
                Toast.makeText(this, "Falha ao carregar dados do usuário.", Toast.LENGTH_SHORT).show();
                if(redeemButton != null) redeemButton.setEnabled(false);
            }
        });
    }

    private void updateCheckInUI(DocumentSnapshot document) {
        dailyCheckInContainer.removeAllViews();

        canUserCheckIn = false;
        currentCheckInDayIndex = -1;

        long checkInStreak = 0;
        boolean canCheckInToday = true;

        if (document != null) {
            checkInStreak = document.getLong("checkInStreak") != null ? document.getLong("checkInStreak") : 0;
            Timestamp lastCheckInTimestamp = document.getTimestamp("lastCheckIn");

            if (lastCheckInTimestamp != null) {
                Calendar today = Calendar.getInstance();
                Calendar lastCheckInCal = Calendar.getInstance();
                lastCheckInCal.setTime(lastCheckInTimestamp.toDate());

                if (isSameDay(today, lastCheckInCal)) {
                    canCheckInToday = false; // Check-in de hoje já foi feito
                } else {
                    Calendar expectedToday = (Calendar) lastCheckInCal.clone();
                    expectedToday.add(Calendar.DAY_OF_YEAR, 1);
                    if (!isSameDay(today, expectedToday)) {
                        checkInStreak = 0; // Quebrou a sequência
                    }
                }
            }
        }

        if (checkInStreak >= 7) {
            checkInStreak = 0; // Reinicia o ciclo
        }

        for (int i = 0; i < 7; i++) {
            View dayView = LayoutInflater.from(this).inflate(R.layout.item_check_in_day, dailyCheckInContainer, false);
            TextView dayText = dayView.findViewById(R.id.check_in_day_text);
            ImageView rewardIcon = dayView.findViewById(R.id.check_in_reward_icon);
            TextView rewardAmount = dayView.findViewById(R.id.check_in_reward_amount);

            dayText.setText("Dia " + (i + 1));
            rewardAmount.setText("+" + dailyRewards[i]);

            rewardIcon.setImageResource((i == 1 || i == 4) ? R.drawable.ic_gift : R.drawable.ic_coin);

            if (i < checkInStreak) { // Dias já coletados
                dayView.setActivated(true);
                rewardAmount.setTextColor(Color.GRAY);
                dayText.setTextColor(Color.GRAY);
            } else if (i == checkInStreak && canCheckInToday) { // Dia atual para coleta
                canUserCheckIn = true;
                currentCheckInDayIndex = i;
                rewardAmount.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                dayText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } else { // Dias futuros ou já coletado hoje
                if (i == checkInStreak && !canCheckInToday) {
                    dayView.setActivated(true); // Mostra como coletado para hoje
                }
                rewardAmount.setTextColor(Color.GRAY);
                dayText.setTextColor(Color.GRAY);
            }
            dayView.setEnabled(false); // Nenhum dia é clicável diretamente
            dailyCheckInContainer.addView(dayView);
        }

        redeemButton.setEnabled(canUserCheckIn);
        if (canUserCheckIn) {
            redeemButton.setText("Fazer Check-in (Dia " + (currentCheckInDayIndex + 1) + ")");
        } else {
            redeemButton.setText("Check-in de Hoje Concluído");
        }
    }

    private void performCheckIn(int dayIndex) {
        long reward = dailyRewards[dayIndex];

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            long currentCoins = snapshot.exists() && snapshot.getLong("coins") != null ? snapshot.getLong("coins") : 0;
            long newCoins = currentCoins + reward;
            long newStreak = dayIndex + 1;

            Map<String, Object> updates = new HashMap<>();
            updates.put("coins", newCoins);
            updates.put("checkInStreak", newStreak);
            updates.put("lastCheckIn", new Timestamp(new Date()));

            if (snapshot.exists()) {
                transaction.update(userRef, updates);
            } else {
                transaction.set(userRef, updates);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Check-in realizado! Você ganhou " + reward + " moedas.", Toast.LENGTH_SHORT).show();
            loadUserData();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Falha ao realizar check-in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            redeemButton.setEnabled(true);
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

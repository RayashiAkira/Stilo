package com.example.stilo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Random;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "stilo_notifications";

    public NotificationWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        String[] messages = {
            "Já fez seu check-in hoje? Não perca suas moedas Stilo!",
            "Um novo dia, uma nova chance de brilhar. Que tal começar com o app Stilo?",
            "Você é incrível! Continue cuidando de você com a ajuda do Stilo.",
            "A beleza está em cada detalhe. Abra o Stilo e descubra novas inspirações.",
            "Seu próximo look dos sonhos pode estar a um clique de distância. Explore o Stilo!"
        };

        Random random = new Random();
        String message = messages[random.nextInt(messages.length)];

        sendNotification(getApplicationContext(), message);

        return Result.success();
    }

    private void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Stilo Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, PromotionsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stilo_coin_large) 
                .setContentTitle("Stilo te lembra!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}

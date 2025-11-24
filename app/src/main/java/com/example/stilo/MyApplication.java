package com.example.stilo;

import android.app.Application;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        
        scheduleNotificationWorker();
    }

    private void scheduleNotificationWorker() {
        PeriodicWorkRequest notificationWorkRequest = 
            new PeriodicWorkRequest.Builder(NotificationWorker.class, 2, TimeUnit.HOURS, 15, TimeUnit.MINUTES)
            .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "stilo_notification_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWorkRequest
        );
    }
}

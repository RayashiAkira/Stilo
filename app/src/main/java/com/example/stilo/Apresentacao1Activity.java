package com.example.stilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class Apresentacao1Activity extends AppCompatActivity {

    private VideoView videoViewBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apresentacao1);

        videoViewBackground = findViewById(R.id.video_view_background);
        Button comecarButton = findViewById(R.id.button_comecar);
        TextView entrarContaText = findViewById(R.id.text_entrar_conta);

        setupVideoBackground();

        comecarButton.setOnClickListener(v -> {
            startActivity(new Intent(Apresentacao1Activity.this, Apresentacao2Activity.class));
        });

        entrarContaText.setOnClickListener(v -> {
            startActivity(new Intent(Apresentacao1Activity.this, AuthenticationActivity.class));
        });
    }

    private void setupVideoBackground() {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stiloapresentacao);
        videoViewBackground.setVideoURI(videoUri);

        videoViewBackground.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f); // Vídeo sem som
            videoViewBackground.start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reinicia o vídeo se a atividade for retomada
        videoViewBackground.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa o vídeo para economizar recursos
        if (videoViewBackground.isPlaying()) {
            videoViewBackground.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libera os recursos do vídeo
        videoViewBackground.stopPlayback();
    }
}

package com.example.stilo;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LegalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        // Opcional: Adicionar um botão "voltar" na barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Termos e Privacidade");
        }

        // Carrega o texto do strings.xml e aplica a formatação HTML
        TextView legalText = findViewById(R.id.legal_text);
        legalText.setText(Html.fromHtml(getString(R.string.texto_termos_completos), Html.FROM_HTML_MODE_COMPACT));
    }

    // Opcional: Faz o botão "voltar" na barra de título funcionar
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
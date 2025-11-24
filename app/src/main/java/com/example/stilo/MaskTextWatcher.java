package com.example.stilo;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskTextWatcher implements TextWatcher {

    private final EditText editText;
    private final String mask;
    private boolean isUpdating;
    private String old = "";

    public MaskTextWatcher(EditText editText, String mask) {
        this.editText = editText;
        this.mask = mask;
    }

    // Método estático para remover caracteres da máscara
    public static String unmask(String s) {
        return s.replaceAll("[^0-9]*", ""); // Remove tudo que não for dígito
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = MaskTextWatcher.unmask(s.toString());
        String mascara = "";
        if (isUpdating) {
            old = str;
            isUpdating = false;
            return;
        }

        int i = 0;
        for (char m : mask.toCharArray()) {
            if (m != '#' && str.length() > old.length()) {
                mascara += m;
                continue;
            }
            try {
                mascara += str.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }

        isUpdating = true;
        editText.setText(mascara);
        editText.setSelection(mascara.length()); // Posiciona o cursor no final
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
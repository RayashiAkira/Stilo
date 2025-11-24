package com.example.stilo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

public class AddTransactionDialogFragment extends DialogFragment {

    private TextInputEditText descriptionEditText;
    private TextInputEditText amountEditText;
    private Button saveButton;
    private Button cancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        descriptionEditText = view.findViewById(R.id.transaction_description_edit_text);
        amountEditText = view.findViewById(R.id.transaction_amount_edit_text);
        saveButton = view.findViewById(R.id.save_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            // Lógica para salvar a transação
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
}

package com.example.stilo;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class FinanceTransaction {
    @Exclude
    private String id;

    private String name;
    private double amount;
    private String type; // "Ganho" ou "Despesa"
    private String description;
    private String expenseTag; // "Essencial" ou "Opcional"
    private Date date;

    // Construtor vazio necessário para o Firestore
    public FinanceTransaction() {}

    public FinanceTransaction(String name, double amount, String type, String description, String expenseTag, Date date) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.expenseTag = expenseTag;
        this.date = date;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpenseTag() {
        return expenseTag;
    }

    public void setExpenseTag(String expenseTag) {
        this.expenseTag = expenseTag;
    }

    @ServerTimestamp
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

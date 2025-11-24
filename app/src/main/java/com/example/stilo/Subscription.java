package com.example.stilo;

import com.google.firebase.firestore.Exclude;

public class Subscription {

    @Exclude
    private String id; // Document ID from Firestore

    private String planName;
    private String providerName;
    private String userId;
    private String planId;
    private String providerId;
    private String category; // Novo campo

    // Construtor vazio é necessário para o Firestore
    public Subscription() {}

    // Getters
    public String getId() { return id; }
    public String getPlanName() { return planName; }
    public String getProviderName() { return providerName; }
    public String getUserId() { return userId; }
    public String getPlanId() { return planId; }
    public String getProviderId() { return providerId; }
    public String getCategory() { return category; } // Novo getter

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPlanName(String planName) { this.planName = planName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setCategory(String category) { this.category = category; } // Novo setter
}

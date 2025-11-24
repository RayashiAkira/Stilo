package com.example.stilo;

public class Agendamento {
    private String id;
    private String providerId;
    private String providerName; // Para exibição fácil
    private String clientId;
    private String date; // Formato: dd-MM-yyyy
    private String startTime;
    private String endTime;

    // Construtor vazio para o Firebase
    public Agendamento() {}

    public Agendamento(String providerId, String providerName, String clientId, String date, String startTime, String endTime) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.clientId = clientId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getFormattedTime() {
        return startTime + " - " + endTime;
    }
}

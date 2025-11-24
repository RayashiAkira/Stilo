package com.example.stilo;

public class Horario {
    private String id;
    private String title;
    private String serviceType;
    private String startTime;
    private String endTime;
    private double price;
    private String description;

    // Construtor vazio necessário para o Firebase
    public Horario() {}

    public Horario(String title, String serviceType, String startTime, String endTime, double price, String description) {
        this.title = title;
        this.serviceType = serviceType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedTime() {
        return startTime + " - " + endTime;
    }
}

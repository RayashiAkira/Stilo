package com.example.stilo;

public class Plan {
    private String id;
    private String name;
    private double price;
    private String description;
    private String duration;
    private String providerId;
    private String category; // Novo campo

    // Construtor vazio necessário para o Firestore
    public Plan() {}

    public Plan(String id, String name, double price, String description, String duration, String providerId, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.duration = duration;
        this.providerId = providerId;
        this.category = category;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getDuration() { return duration; }
    public String getProviderId() { return providerId; }
    public String getCategory() { return category; } // Novo getter

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setCategory(String category) { this.category = category; } // Novo setter
}

package com.example.stilo;

import com.google.firebase.Timestamp;

public class Review {
    private String authorName;
    private String comment;
    private float rating;
    private Timestamp timestamp;

    public Review() {
        // Construtor vazio necessário para o Firestore
    }

    public Review(String authorName, String comment, float rating, Timestamp timestamp) {
        this.authorName = authorName;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getComment() {
        return comment;
    }

    public float getRating() {
        return rating;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}

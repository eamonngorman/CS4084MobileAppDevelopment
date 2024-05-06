package com.example.cs4084mobileappdevelopment;

public class Message {
    private String message;
    private double latitude;
    private double longitude;
    private String category;

    public Message() {
        // Default constructor required for Firestore
    }

    public Message(String message, double latitude, double longitude, String category) {
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

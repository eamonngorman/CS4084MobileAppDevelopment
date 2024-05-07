package com.example.cs4084mobileappdevelopment;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Message {
    private String message;
    private double latitude;
    private double longitude;
    private String category;
    private String postId;

    public Message() {
        // Default constructor required for Firestore
    }

    public Message(String postId, String message, double latitude, double longitude, String category) {
        this.postId = postId;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public DocumentReference getPostRef() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference postsRef = db.collection("messages");

        // Log postId for debugging
        Log.d("Message", "getPostRef: PostId: " + this.postId);

        // Get a reference to the specific post using its postId
        DocumentReference postReference = postsRef.document(this.postId);

        // Log the post reference for debugging
        if (postReference != null) {
            Log.d("Message", "getPostRef: Post Reference: " + postReference.getPath());
        } else {
            Log.d("Message", "getPostRef: Post Reference is null");
        }

        return postReference;
    }
}

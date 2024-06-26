package com.example.cs4084mobileappdevelopment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommentHandler {

    public static void postComment(String commentText, String postId, String author){


        Map<String, Object> comment = new HashMap<>();
        comment.put("postId", postId);
        comment.put("comment", commentText);
        comment.put("timestamp", System.currentTimeMillis());
        comment.put("author", author);

        CollectionReference commentsRef = FirebaseFirestore.getInstance().collection("comments");

        commentsRef.add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Comment added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error adding comment
                    }
                });
    }



}



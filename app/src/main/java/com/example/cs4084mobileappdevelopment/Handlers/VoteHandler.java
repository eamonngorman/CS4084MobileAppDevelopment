package com.example.cs4084mobileappdevelopment.Handlers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class VoteHandler {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void upvote(DocumentReference postRef, String userId) {
        String postId = postRef.getId();
        DocumentReference userVoteRef = postRef.collection("userVotes").document(userId);
        DocumentReference upvoteRef = db.collection("upvotes").document(postId);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userVoteSnapshot = transaction.get(userVoteRef);
                if (!userVoteSnapshot.exists()) {
                    DocumentSnapshot upvoteSnapshot = transaction.get(upvoteRef);
                    long newUpvotes = upvoteSnapshot.getLong("count") + 1;
                    transaction.update(upvoteRef, "count", newUpvotes);

                    Map<String, Object> userVote = new HashMap<>();
                    userVote.put("voted", true);
                    transaction.set(userVoteRef, userVote);
                }
                return null;
            }
        });
    }

    public void downvote(DocumentReference postRef, String userId) {
        String postId = postRef.getId();
        DocumentReference userVoteRef = postRef.collection("userVotes").document(userId);
        DocumentReference downvoteRef = db.collection("downvotes").document(postId);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userVoteSnapshot = transaction.get(userVoteRef);
                if (!userVoteSnapshot.exists()) {
                    DocumentSnapshot downvoteSnapshot = transaction.get(downvoteRef);
                    long newDownvotes = downvoteSnapshot.getLong("count") + 1;
                    transaction.update(downvoteRef, "count", newDownvotes);

                    Map<String, Object> userVote = new HashMap<>();
                    userVote.put("voted", true);
                    transaction.set(userVoteRef, userVote);
                }
                return null;
            }
        });
    }
}
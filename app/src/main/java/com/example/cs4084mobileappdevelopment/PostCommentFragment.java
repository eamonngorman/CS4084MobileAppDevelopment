package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostCommentFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;


    private EditText commentEditText;
    private Button postCommentButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String postId;

    public PostCommentFragment() {

    }

    public PostCommentFragment(DocumentReference messageRef) {
        this.postId = messageRef.getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Get the post ID from the arguments
//        postId = getArguments().getString("postId");

        // Initialize views
        commentEditText = view.findViewById(R.id.comment_edit_text);
        postCommentButton = view.findViewById(R.id.comment_button);

        // Set up the post comment button
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String commentText = commentEditText.getText().toString();
//                String postId = getArguments().getString("postId");

                CommentHandler.postComment(commentText, postId);
            }
        });

        return view;
    }
}
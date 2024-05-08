package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ViewCommentsFragment extends Fragment {

    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference commentsRef;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;


    public ViewCommentsFragment(){

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setAdapter(commentsAdapter);

        commentsRef = db.collection("comments");

        Query query = commentsRef.whereEqualTo("postId", "fe7YoAz5va9nwqiXGl4v");

        query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("ViewCommentsFragment", "Error: " + error.getMessage());
                return;
            }

            if (querySnapshot != null) {

                List<Comment> commentsList = querySnapshot.toObjects(Comment.class);
                commentsAdapter.setComments(commentsList);
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Log.d("ViewCommentsFragment", "got one: " + document.getData());
                }
            } else {
                Log.d("ViewCommentsFragment", "Nope");
            }
        });


        view.findViewById(R.id.post_comment_button).setOnClickListener(v -> {
            // add button stuff
        });

        view.findViewById(R.id.close_comments).setOnClickListener(v -> {
            // add button stuff
        });

        return view;
    }
}


//commentButton.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//
//        PostCommentFragment commentFragment = new PostCommentFragment(postRef);
//        FragmentManager fragmentManager = getParentFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(R.id.message_container, commentFragment);
//        fragmentTransaction.commit();
//
////                PostCommentFragment commentFragment = new PostCommentFragment(postRef);
////
////                getSupportFragmentManager().beginTransaction()
////                        .replace(R.id.message_container, commentFragment)
////                        .commit();
//
//    }
//});
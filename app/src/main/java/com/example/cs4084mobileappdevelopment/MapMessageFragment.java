package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cs4084mobileappdevelopment.Handlers.VoteHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentContainerView;

import com.example.cs4084mobileappdevelopment.MapMessageFragment;
import com.example.cs4084mobileappdevelopment.R;


public class MapMessageFragment extends Fragment {

    private static final String ARG_MARKER_TITLE = "marker_title";
    private static final String ARG_MARKER_SNIPPET = "marker_snippet";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_LOCATION = "location";

    public MapMessageFragment() {
    }

    public static MapMessageFragment newInstance(String markerTitle, String markerSnippet, long timestamp, String location, String postId) {
        MapMessageFragment fragment = new MapMessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MARKER_TITLE, markerTitle);
        args.putString(ARG_MARKER_SNIPPET, markerSnippet);
        args.putLong(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_LOCATION, location);
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_message, container, false);

        // Firebase and votehandler
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        VoteHandler voteHandler = new VoteHandler();

        // Getting post ID and references
        Bundle args = getArguments();
        String postId = args.getString("postId");
        DocumentReference postRef = db.collection("messages").document(postId);
        DocumentReference upvoteRef = db.collection("upvotes").document(postId);
        DocumentReference downvoteRef = db.collection("downvotes").document(postId);


        TextView UpvoteCount = view.findViewById(R.id.UpvoteCount);
        TextView DownvoteCount = view.findViewById(R.id.DownVoteCount);
        ImageButton upvoteButton = view.findViewById(R.id.upvoteBtn);
        ImageButton downvoteButton = view.findViewById(R.id.downvoteBtn);
        Button closeButton = view.findViewById(R.id.CloseBtn);
        Button commentButton = view.findViewById(R.id.view_comment_button);

        updateVotes(upvoteRef, UpvoteCount);
        updateVotes(downvoteRef, DownvoteCount);

        displayPostDetails(args, view);

        setButtonClickListeners(closeButton, upvoteButton, downvoteButton, postRef, userId, voteHandler, commentButton);

        return view;




    }

    private void updateVotes(DocumentReference voteRef, TextView voteCountView) {
        voteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long votes = document.getLong("count");
                        voteCountView.setText(String.valueOf(votes));
                    } else {
                        Log.d("MapMessageFragment", "No such document");
                    }
                } else {
                    Log.d("MapMessageFragment", "get failed with ", task.getException());
                }
            }
        });
    }

    private void displayPostDetails(Bundle args, View view) {
        String markerTitle = args.getString(ARG_MARKER_TITLE);
        String location = args.getString(ARG_LOCATION);
        String markerMessage = args.getString(ARG_MARKER_SNIPPET);

        // Getting how long ago the post was
        long timestamp = args.getLong(ARG_TIMESTAMP);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

        String timeAgo = getTimeAgo(timeDifference);

        // Updating text views
        TextView titleTextView = view.findViewById(R.id.title);
        titleTextView.setText("Posted " + timeAgo);

        TextView locationTextView = view.findViewById(R.id.LocationText);
        locationTextView.setText(location);

        TextView messageTextView = view.findViewById(R.id.message);
        messageTextView.setText(markerMessage);
    }

    private void setButtonClickListeners(Button closeButton, ImageButton upvoteButton, ImageButton downvoteButton, DocumentReference postRef, String userId, VoteHandler voteHandler, Button commentButton) {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(MapMessageFragment.this).commit();
                }
            }
        });

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "PostID", Toast.LENGTH_SHORT).show();
                voteHandler.upvote(postRef, userId);
            }
        });

        downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Clicked downvote" + postRef + " as user " + userId, Toast.LENGTH_SHORT).show();
                voteHandler.downvote(postRef, userId);
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewCommentsFragment viewCommentFragment = new ViewCommentsFragment();
//                ViewCommentsFragment commentFragment = new ViewCommentsFragment(postRef);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, viewCommentFragment);
                fragmentTransaction.commit();


//                PostCommentFragment commentFragment = new PostCommentFragment(postRef);
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



            }
        });

    }



    private String getTimeAgo(long timeDifference) {
        if (timeDifference < 60000) {
            return "just now";
        } else if (timeDifference < 3600000) {
            return (timeDifference / 60000) + " minutes ago";
        } else if (timeDifference < 86400000) {
            return (timeDifference / 3600000) + " hours ago";
        } else {
            return (timeDifference / 86400000) + " days ago";
        }
    }
}
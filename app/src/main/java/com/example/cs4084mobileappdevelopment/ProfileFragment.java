package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    RecyclerView postsRecyclerView;
    RecyclerView.Adapter adapter;
    List<String> messagesList;
    List<String> messageIds;
    long upvotes = 0;
    long downvotes = 0;
    long totalVotes = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button closeButton = view.findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
                }
            }
        });
        TextView emailTextView = view.findViewById(R.id.user_details);
        TextView votesTextView = view.findViewById(R.id.upvotes);
        postsRecyclerView = view.findViewById(R.id.messageRecycler);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        emailTextView.setText(user.getEmail());

        messagesList = new ArrayList<>();
        messageIds = new ArrayList<>();
        class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
            private List<String> messagesList;

            MessageAdapter(List<String> messagesList) {
                this.messagesList = messagesList;
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_view_item, parent, false);
                return new MessageViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
                String message = messagesList.get(position);
                holder.messageTextView.setText(message);
            }

            @Override
            public int getItemCount() {
                return messagesList.size();
            }

            class MessageViewHolder extends RecyclerView.ViewHolder {
                TextView messageTextView;

                MessageViewHolder(View view) {
                    super(view);
                    messageTextView = view.findViewById(R.id.messageText);
                }
            }
        }
        adapter = new MessageAdapter(messagesList);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postsRecyclerView.setAdapter(adapter);

        db.collection("messages")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String message = document.getString("message");
                            messagesList.add(message);
                            messageIds.add(document.getId());
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("ProfileFragment", "userID: " + user.getUid());
                        Log.d("ProfileFragment", "messagesList: " + messagesList);
                        Log.d("ProfileFragment", "messageIds: " + messageIds);

                        for (String messageId : messageIds) {
                            Log.d("ProfileFragment", "messageId: " + messageId);
                            db.collection("upvotes")
                                    .whereEqualTo("postId", messageId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                                totalVotes += document.getLong("count");
                                                Log.d("ProfileFragment", "upvotes: " + upvotes);
                                            }

                                        } else {
                                            Log.d("ProfileFragment", "Error getting upvotes");
                                        }
                                    });

                            db.collection("downvotes")
                                    .whereEqualTo("postId", messageId)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task2.getResult()) {
                                                totalVotes -= document.getLong("count");
                                                Log.d("ProfileFragment", "downvotes: " + downvotes);
                                            }
                                        } else {
                                            Log.d("ProfileFragment", "Error getting downvotes");
                                        }
                                        votesTextView.setText("Total Votes: " + totalVotes);
                                    });

                        }
                    } else {
                        // Handle any errors
                    }
                });


        return view;
    }
}
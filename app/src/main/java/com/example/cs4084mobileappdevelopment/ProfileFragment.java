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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

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
    List<String> categoriesList;
    List<String> timeList;
    long currentTime = System.currentTimeMillis();
    long upvotes = 0;
    long downvotes = 0;
    long totalVotes = 0;

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
        categoriesList = new ArrayList<>();
        timeList = new ArrayList<>();
        class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
            private List<String> messagesList;
            private List<String> categoriesList;
            private List<String> timeList;

            MessageAdapter(List<String> messagesList, List<String> categoriesList, List<String> timeList) {
                this.messagesList = messagesList;
                this.categoriesList = categoriesList;
                this.timeList = timeList;
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
                String category = categoriesList.get(position);
                String time = timeList.get(position);
                holder.messageTextView.setText(message);
                holder.categoryTextView.setText(category);
                holder.timeTextView.setText(time);
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = holder.getAdapterPosition();
                        String messageId = messageIds.get(adapterPosition);
                        db.collection("messages").document(messageId).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ProfileFragment", "deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("ProfileFragment", "Error deleting document", e);
                                    }
                                });
                    }
                });
            }

            @Override
            public int getItemCount() {
                return messagesList.size();
            }

            class MessageViewHolder extends RecyclerView.ViewHolder {
                TextView messageTextView;
                TextView categoryTextView;
                TextView timeTextView;
                Button deleteButton;

                MessageViewHolder(View view) {
                    super(view);
                    messageTextView = view.findViewById(R.id.messageText);
                    categoryTextView = view.findViewById(R.id.categoryText);
                    timeTextView = view.findViewById(R.id.timeText);
                    deleteButton = view.findViewById(R.id.deleteButton);
                }
            }
        }

        adapter = new MessageAdapter(messagesList, categoriesList, timeList);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postsRecyclerView.setAdapter(adapter);


        db.collection("messages")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String message = document.getString("message");
                            String category = document.getString("category");
                            long timestamp = document.getLong("timestamp");
                            String time = getTimeAgo(currentTime - timestamp);

                            messagesList.add(message);
                            messageIds.add(document.getId());
                            categoriesList.add(category);
                            timeList.add(time);
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
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
        TextView textView = view.findViewById(R.id.user_details);
        postsRecyclerView = view.findViewById(R.id.messageRecycler);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        textView.setText(user.getEmail());

        messagesList = new ArrayList<>();
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
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("ProfileFragment", "userID: " + user.getUid());
                        Log.d("ProfileFragment", "messagesList: " + messagesList);
                    } else {
                        // Handle any errors
                    }
                });

        return view;
    }
}
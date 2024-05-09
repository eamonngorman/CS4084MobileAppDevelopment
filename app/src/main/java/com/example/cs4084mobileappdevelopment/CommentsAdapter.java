package com.example.cs4084mobileappdevelopment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments = new ArrayList<>();

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        setAuthToUsername(comment);
//        holder.bind(comment);
        holder.commentTextView.setText(comment.getComment());
        holder.commentTimeView.setText("Posted:" + getTimeAgo(comment.getTimestamp()));
        holder.commentAuthorView.setText("By:" + comment.getAuthor());

    }

    @Override
    public int getItemCount() {

        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView commentTextView;
        private TextView commentTimeView;
        private TextView commentAuthorView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment_text);
            commentTimeView = itemView.findViewById(R.id.comment_timestamp);
            commentAuthorView = itemView.findViewById(R.id.comment_author);
        }
    }

    private String getTimeAgo(long TimeOfComment) {

        long timeDifference = System.currentTimeMillis() - TimeOfComment;
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

    private void setAuthToUsername(Comment c) {

        UserNameHandler un = new UserNameHandler();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        ;
        FirebaseUser user = auth.getCurrentUser();
        ;

        assert user != null;
        un.getUserName(user.getUid(), new UserNameHandler.QueryCallbackString() {
            @Override
            public void onQueryCompletedString(String username) {
                c.setAuthor(username);
            }
        });

    }


}
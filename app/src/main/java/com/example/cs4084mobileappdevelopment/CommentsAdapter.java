package com.example.cs4084mobileappdevelopment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.bind(comment);
//        holder.commentTextView.setText("TEXT" + comment.getText());
        holder.commentTimeView.setText("Time:" + comment.getTime());
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

        public void bind(Comment comment) {
            commentTextView.setText("HERE " + comment.getComment());
//            String location = String.format("Location: %s, %s", message.getLatitude(), message.getLongitude());
//            locationTextView.setText(location);
//            eventTypeTextView.setText("Event Type: " + message.getCategory());
        }
    }


}
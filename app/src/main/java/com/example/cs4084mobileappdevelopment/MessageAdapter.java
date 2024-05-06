package com.example.cs4084mobileappdevelopment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList = new ArrayList<>();

    public void setData(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView locationTextView;
        private TextView eventTypeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            locationTextView = itemView.findViewById(R.id.location_text_view);
            eventTypeTextView = itemView.findViewById(R.id.event_type_text_view);
        }

        public void bind(Message message) {
            messageTextView.setText(message.getMessage());
            String location = String.format("Location: %s, %s", message.getLatitude(), message.getLongitude());
            locationTextView.setText(location);
            eventTypeTextView.setText("Event Type: " + message.getCategory());
        }
    }
}

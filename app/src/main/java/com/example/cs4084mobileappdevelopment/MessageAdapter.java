package com.example.cs4084mobileappdevelopment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;

import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messageList = new ArrayList<>();
    }

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

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView locationTextView;
        private TextView eventTypeTextView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            locationTextView = itemView.findViewById(R.id.location_text_view);
            eventTypeTextView = itemView.findViewById(R.id.event_type_text_view);
        }

        void bind(Message message) {
            messageTextView.setText(message.getMessage());

            // Use geocoding to get human-readable location
            String location = getGeocodedLocation(message.getLatitude(), message.getLongitude());
            locationTextView.setText(location);

            eventTypeTextView.setText("Event Type: " + message.getCategory());
        }

        private String getGeocodedLocation(double latitude, double longitude) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String location = "Unknown";

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    if (address.getThoroughfare() != null) {
                        location = address.getThoroughfare() + ", " + address.getLocality() + ", " + address.getCountryName();
                    } else {
                        location = address.getLocality() + ", " + address.getCountryName();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return location;
        }
    }
}


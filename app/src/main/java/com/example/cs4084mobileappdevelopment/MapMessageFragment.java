package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapMessageFragment extends Fragment {

    private static final String ARG_MARKER_TITLE = "marker_title";
    private static final String ARG_MARKER_SNIPPET = "marker_snippet";
    private static final String ARG_TIMESTAMP = "timestamp";

    private static final String ARG_LOCATION = "location";

    public MapMessageFragment() {
        // Required empty public constructor
    }

    public static MapMessageFragment newInstance(String markerTitle, String markerSnippet, long timestamp, String location) {
        MapMessageFragment fragment = new MapMessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MARKER_TITLE, markerTitle);
        args.putString(ARG_MARKER_SNIPPET, markerSnippet);
        args.putLong(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_message, container, false);

        // Getting arguments from the map activity
        Bundle args = getArguments();
        String markerTitle = args.getString(ARG_MARKER_TITLE);
        String location = args.getString(ARG_LOCATION);
        String markerMessage = args.getString(ARG_MARKER_SNIPPET);
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

        Button closeButton = view.findViewById(R.id.CloseBtn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(MapMessageFragment.this).commit();
                }
            }
        });

        return view;
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
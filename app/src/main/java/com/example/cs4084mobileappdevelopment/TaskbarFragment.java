package com.example.cs4084mobileappdevelopment;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TaskbarFragment extends Fragment {

    FirebaseAuth auth;
    Button button;
    FirebaseUser user;

    Button viewMap;

    Button createPost;

    ImageButton viewMapButton;

    ImageButton createPostButton;

    ImageButton homeBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taskbar, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        ImageButton viewMapButton = view.findViewById(R.id.mapImgButton);
        ImageButton createPostButton = view.findViewById(R.id.createPostBtn);
        ImageButton homeBtn = view.findViewById(R.id.homeBtn);

        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });

        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                startActivity(intent);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });




        return view;
    }
}
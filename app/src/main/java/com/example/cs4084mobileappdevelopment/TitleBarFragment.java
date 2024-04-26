package com.example.cs4084mobileappdevelopment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class TitleBarFragment extends Fragment {

    private ImageView titleBarImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_title_bar, container, false);

        titleBarImage = view.findViewById(R.id.title_bar_image);

        return view;
    }
}
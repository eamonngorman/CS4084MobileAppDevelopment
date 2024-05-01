package com.example.cs4084mobileappdevelopment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class CreatePost extends Fragment {

    EditText editTextMessage;
    Button buttonLogin;
    Spinner spinnerCategory;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Request location permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonLogin = view.findViewById(R.id.buttonSubmit);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(editTextMessage.getText());
                String category = spinnerCategory.getSelectedItem().toString();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(getActivity(), "Enter Message", Toast.LENGTH_SHORT).show();
                    return;
                }

                postMessageToFirestore(message, category);
            }
        });

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private Location getDeviceCoordinates() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    return location;
                }
            }
        }
        return null;
    }

    private void postMessageToFirestore(String message, String category) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("message", message);
        postData.put("category", category);
        postData.put("timestamp", System.currentTimeMillis());

        Location currentLocation = getDeviceCoordinates();

        if (currentLocation == null) {
            Toast.makeText(getActivity(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();

        postData.put("latitude", latitude);
        postData.put("longitude", longitude);

        String geohash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 12);

        postData.put("geoHash", geohash);

        db.collection("messages")
                .add(postData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Message posted to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error posting message to Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error posting message to Firestore", e);
                });
    }
}
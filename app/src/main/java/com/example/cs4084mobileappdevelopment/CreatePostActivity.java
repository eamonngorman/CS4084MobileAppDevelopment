package com.example.cs4084mobileappdevelopment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class CreatePostActivity extends AppCompatActivity {


    EditText editTextMessage;
    Button buttonLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonLogin = findViewById(R.id.buttonSubmit);

        buttonLogin.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                String message;
                message = String.valueOf(editTextMessage.getText());

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(CreatePostActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                    return;
                }

                postMessageToFirestore(message);

            }

        });
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Request location permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Get device coordinates
    private Location getDeviceCoordinates() {
        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    return location;
                }
            }
        }
        return null; // Return null if location is not available
    }


    // Write data to Firestore
    private void postMessageToFirestore(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new message document
        Map<String, Object> postData = new HashMap<>();
        postData.put("message", message);
        postData.put("timestamp", System.currentTimeMillis());
        // Get device coordinates


        Location currentLocation = getDeviceCoordinates();

        // Check if currentLocation is null
        if (currentLocation == null) {
            // Show toast message
            Toast.makeText(this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            // Exit function
            return;
        }
        // Add coordinates to data
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();


        postData.put("latitude", latitude);
        postData.put("longitude", longitude);

        String geohash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 12);

        postData.put("geoHash", geohash);

        // Add document to Firestore collection
        db.collection("messages")
                .add(postData)
                .addOnSuccessListener(documentReference -> {
                    // Show toast message for success
                    Toast.makeText(this, "Message posted to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show toast message for failure
                    Toast.makeText(this, "Error posting message to Firestore", Toast.LENGTH_SHORT).show();
                    // Log error
                    Log.e("Firestore", "Error posting message to Firestore", e);
                });
    }



}
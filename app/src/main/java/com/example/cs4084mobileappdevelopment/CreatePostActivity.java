package com.example.cs4084mobileappdevelopment;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class CreatePostActivity extends AppCompatActivity {

    EditText editTextMessage;
    Button buttonLogin;

    FusedLocationProviderClient fusedLocationProviderClient;

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

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        buttonLogin.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();

            if (TextUtils.isEmpty(message)) {
                Toast.makeText(CreatePostActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                return;
            }

            postMessageToFirestore(message);
        });
    }

    private void postMessageToFirestore(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> postData = new HashMap<>();
        postData.put("message", message);
        postData.put("timestamp", System.currentTimeMillis());

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get device coordinates
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
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
                    } else {
                        Toast.makeText(this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to get last location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Location", "Failed to get last location: " + e.getMessage());
                });
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, post message to Firestore
                String message = editTextMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    postMessageToFirestore(message);
                } else {
                    Toast.makeText(CreatePostActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

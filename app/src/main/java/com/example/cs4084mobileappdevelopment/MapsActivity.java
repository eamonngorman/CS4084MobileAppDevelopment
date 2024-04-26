package com.example.cs4084mobileappdevelopment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.GeoHash;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void handleWindowInsetsForAndroidRAndAbove() {
        final WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private final int FINE_PERMISSION_CODE = 1;
    GoogleMap gMap;
    FrameLayout map;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleWindowInsetsForAndroidRAndAbove();
        } else {
            // For older versions, we need to make the status bar and navigation bar transparent
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }


        //FragmentManager fragmentManager = getSupportFragmentManager();
        // fragmentTransaction = fragmentManager.beginTransaction();

        // taskbarFragment = new TaskbarFragment();
        //fragmentTransaction.replace(R.id.fragment_container, taskbarFragment);
        //fragmentTransaction.commit();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;

        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //this.gMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        this.gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
        queryMessagesNearby(currentLocation.getLatitude(), currentLocation.getLongitude());


        TaskbarFragment taskbarFragment = new TaskbarFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, taskbarFragment)
                .commit();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void queryMessagesNearby(double currentLatitude, double currentLongitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Log current location
        Log.d("Firestore", "Current Location - Latitude: " + currentLatitude + ", Longitude: " + currentLongitude);

        // Calculate geohash prefix for the given coordinate
        GeoLocation center = new GeoLocation(currentLatitude, currentLongitude);
        final double radiusInM = 10 * 1000;

        // Log radius
        Log.d("Firestore", "Radius: " + radiusInM + " meters");

        // Get GeoHash query bounds
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);

        // Log number of bounds
        Log.d("Firestore", "Number of Bounds: " + bounds.size());

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("messages")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            // Log the geohash range for this bound
            Log.d("Firestore", "Geohash Range - Start: " + b.startHash + ", End: " + b.endHash);

            tasks.add(q.get());
        }

        // Log number of tasks
        Log.d("Firestore", "Number of Tasks: " + tasks.size());

        // Execute tasks and wait for completion
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();

                            // Log number of documents in the snapshot
                            Log.d("Firestore", "Number of Documents: " + snap.size());

                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");

                                // Log latitude and longitude of each document
                                Log.d("Firestore", "Document - Latitude: " + lat + ", Longitude: " + lng);

                                // Calculate distance between document location and center
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                Log.d("Firestore", "Distance from Current Location: " + distanceInM + " meters");

                                // Check if document is within radius
                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc);
                                }
                            }
                        }

                        // Log number of matching documents
                        Log.d("Firestore", "Number of Matching Documents: " + matchingDocs.size());

                        // Process matching documents and add markers to map
                        for (DocumentSnapshot documentSnapshot : matchingDocs) {
                            // Retrieve message, latitude, longitude, and timestamp from document
                            String message = documentSnapshot.getString("message");
                            double messageLat = documentSnapshot.getDouble("latitude");
                            double messageLng = documentSnapshot.getDouble("longitude");
                            long timestamp = documentSnapshot.getLong("timestamp"); // Optional: Retrieve timestamp if needed

                            // Log retrieved values
                            Log.d("Firestore", "Message: " + message);
                            Log.d("Firestore", "Latitude: " + messageLat);
                            Log.d("Firestore", "Longitude: " + messageLng);
                            Log.d("Firestore", "Timestamp: " + timestamp);

                            // Create LatLng object for message location
                            LatLng messageLocation = new LatLng(messageLat, messageLng);

                            // Add marker to map with message as label
                            gMap.addMarker(new MarkerOptions().position(messageLocation).title(message));
                        }
                    }
                });
    }



}
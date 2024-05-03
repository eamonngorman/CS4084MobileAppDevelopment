package com.example.cs4084mobileappdevelopment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Style parsing failed, cannot find style.");

        }

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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BitmapDescriptor getMarkerIcon(int drawableRes) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void queryMessagesNearby(double currentLatitude, double currentLongitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        GeoLocation center = new GeoLocation(currentLatitude, currentLongitude);
        final double radiusInM = 10 * 1000;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("messages")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();

                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");

                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);

                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc);
                                }
                            }
                        }

                        for (DocumentSnapshot documentSnapshot : matchingDocs) {
                            String message = documentSnapshot.getString("message");
                            String category = documentSnapshot.getString("category");
                            double messageLat = documentSnapshot.getDouble("latitude");
                            double messageLng = documentSnapshot.getDouble("longitude");

                            LatLng messageLocation = new LatLng(messageLat, messageLng);

                            MarkerOptions markerOptions = new MarkerOptions().position(messageLocation).title(message);

                            if (category != null) {
                                int iconResId = 0;
                                switch (category) {
                                    case "General":
                                        iconResId = R.drawable.small_info;
                                        break;
                                    case "Traffic alert":
                                        iconResId = R.drawable.small_traffic;
                                        break;
                                    case "Event":
                                        iconResId = R.drawable.small_meetup;
                                        break;
                                    case "Question":
                                        iconResId = R.drawable.small_question;
                                        break;
                                    case "Safety notice":
                                        iconResId = R.drawable.small_danger;
                                        break;
                                    case "null":
                                        iconResId = R.drawable.small_info;
                                        break;
                                }

                                if (iconResId != 0) {
                                    markerOptions.icon(getMarkerIcon(iconResId));
                                }
                            }

                            gMap.addMarker(markerOptions);
                        }
                    }
                });
    }



}
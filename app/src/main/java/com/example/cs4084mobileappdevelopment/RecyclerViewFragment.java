package com.example.cs4084mobileappdevelopment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {

    private final int FINE_PERMISSION_CODE = 1;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private FirebaseFirestore firestore;

    public RecyclerViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        getLastLocation();
        return view;
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            queryMessagesNearby(currentLocation.getLatitude(), currentLocation.getLongitude());
                        }
                    }
                });
    }




    private void queryMessagesNearby(double currentLatitude, double currentLongitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        GeoLocation center = new GeoLocation(currentLatitude, currentLongitude);
        final double radiusInM = 5 * 1000; // 5km radius
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
                        List<Message> matchingMessages = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();

                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");

                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);

                                if (distanceInM <= radiusInM) {
                                    // Convert document to Message object
                                    Message message = doc.toObject(Message.class);
                                    matchingMessages.add(message);
                                }
                            }
                        }

                        // Update RecyclerView with the retrieved messages
                        adapter.setData(matchingMessages);
                    }
                });
    }

}

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.geohash.GeoHash;

public class CreatePost extends Fragment {

    EditText editTextMessage;
    Button buttonLogin;
    Spinner spinnerCategory;
    String postId;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Request location permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonLogin = view.findViewById(R.id.buttonSubmit);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Bundle bundle = getArguments();
        // If there is data in the bundle, then we are editing a post
        if (bundle != null) {
            this.postId = bundle.getString("messageId");
            String message = bundle.getString("message");
            editTextMessage.setText(message);
            String category = bundle.getString("category");
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCategory.getAdapter();
            int position = adapter.getPosition(category);
            spinnerCategory.setSelection(position);
            String timestamp = bundle.getString("time");
        }


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(CreatePost.this).commit();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth;
                FirebaseUser user;
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                String message = String.valueOf(editTextMessage.getText());
                String category = spinnerCategory.getSelectedItem().toString();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(getActivity(), "Enter Message", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserNameHandler un = new UserNameHandler();
                un.getUserName(user.getUid(), new UserNameHandler.QueryCallbackString() {

                    @Override
                    public void onQueryCompletedString(String username) {
                        postMessageToFirestore(message, category, username);

                    }
                });

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

    private void postMessageToFirestore(String message, String category, String author) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("message", message);
        postData.put("category", category);
        postData.put("timestamp", System.currentTimeMillis());
        postData.put("deleted", false);
        postData.put("author", author);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            postData.put("userId", userId);
        } else {
            Toast.makeText(getActivity(), "No user is currently signed in", Toast.LENGTH_SHORT).show();
            return;
        }

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
        // If we are editing a post, update the existing document. it used the postId from the bundle to check if there is a post
        if (postId != null) {
            db.collection("messages").document(postId)
                    .update(postData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Message updated in Firestore", Toast.LENGTH_SHORT).show();
                            // Close the fragment
                            getActivity().getSupportFragmentManager().beginTransaction().remove(CreatePost.this).commit();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error updating message in Firestore", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error updating message in Firestore", e);
                    });
        } else {

            db.collection("messages")
                    .add(postData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getActivity(), "Message posted to Firestore", Toast.LENGTH_SHORT).show();

                            Map<String, Object> initialVoteCount = new HashMap<>();
                            initialVoteCount.put("postId", documentReference.getId());
                            initialVoteCount.put("count", 0);

                            Log.i("Firestore", "Document ID: " + documentReference.getId());

                            db.collection("upvotes").document(documentReference.getId()).set(initialVoteCount)
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error creating document in upvotes collection", e);
                                    });

                            db.collection("downvotes").document(documentReference.getId()).set(initialVoteCount)
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error creating document in downvotes collection", e);
                                    });
                            getActivity().getSupportFragmentManager().beginTransaction().remove(CreatePost.this).commit();
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error posting message to Firestore", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error posting message to Firestore", e);
                    });
        }
    }
}
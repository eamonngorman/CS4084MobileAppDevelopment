package com.example.cs4084mobileappdevelopment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void handleWindowInsetsForAndroidRAndAbove() {
        final WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private FirebaseAuth auth;
    private TextView textView;
    private FirebaseUser user;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView dropdownIcon = findViewById(R.id.dropdown_icon);
        dropdownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.logout) {
                            auth.signOut();
                            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        } else if (item.getItemId() == R.id.profile) {
                            ProfileFragment profileFragment = new ProfileFragment();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, profileFragment);
                            fragmentTransaction.commit();
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleWindowInsetsForAndroidRAndAbove();
        } else {
            // For older versions, we need to make the status bar and navigation bar transparent
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        auth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        UserNameHandler un = new UserNameHandler();
        boolean hasUserName;



        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {


            un.queryCheckUserName(user.getUid(), new UserNameHandler.QueryCallback() { //need to so it times right
                @Override
                public void onQueryCompleted(boolean result) {
//                    System.out.println("Here 3: " + result); // testing

                    if(!result) { //if user has no name ....
                        un.queryAddUsername(user.getUid());
                    }
                }
            });

            un.getUserName(user.getUid(), new UserNameHandler.QueryCallbackString() {
                @Override
                public void onQueryCompletedString(String username) {
                    System.out.println("USERNAME: " +  username);
                    textView.setText("Hello " + username);
                }
            });



        }



        // Load Taskbar Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction taskbarTransaction = fragmentManager.beginTransaction();
        TaskbarFragment taskbarFragment = new TaskbarFragment();
        taskbarTransaction.replace(R.id.taskbar_container, taskbarFragment);
        taskbarTransaction.commit();

        // Load RecyclerView Fragment
        FragmentTransaction recyclerViewTransaction = fragmentManager.beginTransaction();
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewTransaction.replace(R.id.recycler_view_container, recyclerViewFragment);
        recyclerViewTransaction.commit();

        db = FirebaseFirestore.getInstance();
        db.collection("messages")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            long timestamp = document.getLong("timestamp");
                            long currentTime = System.currentTimeMillis();
                            long timeDifference = currentTime - timestamp;
                            if (timeDifference > 7 * 24 * 60 * 60 * 1000) {
                                document.getReference().update("deleted", true);
                            }
                        }
                    }
                });


    }



}
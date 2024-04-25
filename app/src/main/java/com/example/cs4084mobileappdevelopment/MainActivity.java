package com.example.cs4084mobileappdevelopment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void handleWindowInsetsForAndroidRAndAbove() {
        final WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    Button viewMap;

    Button createPost;

    ImageButton viewMapButton;

    ImageButton createPostButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is some stuff that will make it so that we can use the entire screen of the phone, removing gesture navigate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleWindowInsetsForAndroidRAndAbove();
        } else {
            // For older versions, make the status bar and navigation bar transparent
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        createPost = findViewById(R.id.createPost);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

            }
        });

        viewMap = findViewById(R.id.viewMap);

        viewMapButton = findViewById(R.id.mapImgButton);

        createPostButton = findViewById(R.id.createPostBtn);

        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

            }
        });

        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });



                    viewMap.setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

            }
        });

        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                startActivity(intent);

            }
        });


        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                startActivity(intent);

            }
        });



    }
}
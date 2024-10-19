package com.mfrankic.sketchid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button startDrawingButton = findViewById(R.id.btn_start_drawing);
        startDrawingButton.setOnClickListener(v -> {
            int userId = getIntent().getIntExtra("userId", -1);

            if (userId == -1) {
                Toast.makeText(this, "Please select a user before drawing.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(HomeActivity.this, DrawingActivity.class);
                intent.putExtra("userId", userId); // Pass userId to DrawingActivity
                startActivity(intent);
            }
        });

        // Handle Settings Button Click
        Button settingsButton = findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}

package com.mfrankic.sketchid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SketchIDPrefs";
    private static final String KEY_SELECTED_USER_ID = "selected_user_id";
    int selectedUserId;
    User selectedUser;
    TextView currentUserText;
    private AppDatabase db;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentUserText = findViewById(R.id.text_current_user);
        Button startDrawingButton = findViewById(R.id.btn_start_drawing);
        Button settingsButton = findViewById(R.id.btn_settings);

        db = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        refreshCurrentUser();

        startDrawingButton.setOnClickListener(v -> {
            if (selectedUserId == -1) {
                Toast.makeText(this, "Please select a user before drawing.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(HomeActivity.this, DrawingActivity.class);
                intent.putExtra("userID", selectedUserId); // Pass userID to DrawingActivity
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentUser();
    }

    private void refreshCurrentUser() {
        selectedUserId = getSelectedUserID();
        if (selectedUserId != -1) {
            executor.execute(() -> {
                selectedUser = db.userDao().getUserById(selectedUserId);
                if (selectedUser != null) {
                    currentUserText.setText(String.format(Locale.getDefault(), "Current User:\n%s", selectedUser.name));
                }
            });
        }
    }

    private int getSelectedUserID() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_SELECTED_USER_ID, -1);
    }
}

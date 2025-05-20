package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.KEY_SELECTED_USER;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeActivity extends BaseActivity {

  long selectedUserID;
  User selectedUser;
  TextView currentUserText;
  private AppDatabase db;
  private Executor executor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    int theme = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getInt(Constants.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    switch (theme) {
      case AppCompatDelegate.MODE_NIGHT_YES:
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        break;
      case AppCompatDelegate.MODE_NIGHT_NO:
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        break;
      default:
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        break;
    }

    setContentView(R.layout.activity_home);

    currentUserText = findViewById(R.id.text_current_user);
    Button startDrawingButton = findViewById(R.id.btn_start_drawing);
    Button settingsButton = findViewById(R.id.btn_settings);

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();

    refreshCurrentUser();

    startDrawingButton.setOnClickListener(v -> {
      if (selectedUserID == -1) {
        Toast.makeText(this, "Please select a user before drawing.", Toast.LENGTH_LONG).show();
      } else {

        String attempts = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString(KEY_DRAWING_ATTEMPTS, "-1");
        if (attempts.equals("-1")) {
          Toast
              .makeText(this,
                        "Please set the number of drawing attempts in settings.",
                        Toast.LENGTH_LONG
              )
              .show();
          return;
        }

        Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);
        if (selectedImageIds.isEmpty()) {
          Toast
              .makeText(this,
                        "No images selected. Please select images in settings.",
                        Toast.LENGTH_LONG
              )
              .show();
        } else {
          Intent intent = new Intent(HomeActivity.this, DrawingActivity.class);
          intent.putExtra("userID", selectedUser.id);
          startActivity(intent);
        }
      }
    });

    settingsButton.setOnClickListener(v -> {
      Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
      startActivity(intent);
    });
  }

  private void refreshCurrentUser() {
    selectedUserID = getSelectedUserID();

    if (selectedUserID == -1) {
      currentUserText.setText(R.string.not_selected);
      return;
    }

    executor.execute(() -> {
      selectedUser = db.userDao().getUserByID(selectedUserID);
      runOnUiThread(() -> currentUserText.setText(selectedUser != null
                                                  ? selectedUser.name
                                                  : "Not selected"));
    });
  }

  private long getSelectedUserID() {
    String savedUserID = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString(KEY_SELECTED_USER, "-1");
    return (savedUserID.equals("-1") || savedUserID.isBlank()) ? -1 : Long.parseLong(savedUserID);
  }

  @Override
  protected void onResume() {
    super.onResume();
    refreshCurrentUser();
  }
}

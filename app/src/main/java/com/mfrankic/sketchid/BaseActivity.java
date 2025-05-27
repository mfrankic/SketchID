package com.mfrankic.sketchid;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base {@link AppCompatActivity} for all activities in the SketchID application.
 * This class handles common functionality such as applying window insets to ensure
 * that UI elements are not obscured by system bars (like the status bar or navigation bar).
 * All other activities in this application should extend this class.
 */
public class BaseActivity extends AppCompatActivity {

  /**
   * Called when the activity is first created.
   * This implementation sets up a listener to apply window insets to the root view,
   * ensuring that content is padded correctly to avoid overlapping with system UI elements.
   *
   * @param savedInstanceState If the activity is being re-initialized after
   *                           previously being shut down then this Bundle contains the data it most
   *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.  <b><i
   *                           >Note: Otherwise it is null.</i></b>
   */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final View rootView = findViewById(android.R.id.content);

    ViewCompat.setOnApplyWindowInsetsListener(
        rootView, (v, windowInsets) -> {
          Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

          return windowInsets;
        }
    );
  }
} 

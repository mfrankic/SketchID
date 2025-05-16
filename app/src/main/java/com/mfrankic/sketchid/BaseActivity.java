package com.mfrankic.sketchid;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base activity that handles window insets and other common functionality.
 * All activities should extend this class to ensure proper UI rendering.
 */
public class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Ensure proper window inset handling
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
  }

  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    setupWindowInsets();
  }

  @Override
  public void setContentView(View view) {
    super.setContentView(view);
    setupWindowInsets();
  }

  private void setupWindowInsets() {
    // Set up insets to ensure content doesn't overlap with system UI
    final View rootView = findViewById(android.R.id.content);
    ViewCompat.setOnApplyWindowInsetsListener(
        rootView, (v, insets) -> {
          v.setPadding(
              v.getPaddingLeft(),
              insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
              v.getPaddingRight(),
              insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
          );
          return WindowInsetsCompat.CONSUMED;
        }
    );
  }
} 

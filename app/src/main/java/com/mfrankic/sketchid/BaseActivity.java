package com.mfrankic.sketchid;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base activity that handles window insets and other common functionality.
 * All activities should extend this class to ensure proper UI rendering.
 */
public class BaseActivity extends AppCompatActivity {

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

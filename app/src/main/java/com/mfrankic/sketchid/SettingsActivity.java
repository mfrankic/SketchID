package com.mfrankic.sketchid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends BaseActivity {

  private View exportOverlay;
  private TextView exportStatusText;
  private boolean isExportInProgress = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    SettingsFragment settingsFragment;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    setupExportOverlay();
    setupBackHandler();

    if (savedInstanceState == null) {
      settingsFragment = new SettingsFragment();
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings_container, settingsFragment)
          .commit();
    }
  }

  private void setupExportOverlay() {
    exportOverlay = new View(this);
    exportOverlay.setBackgroundColor(0x80000000); // Semi-transparent black
    exportOverlay.setClickable(true);
    exportOverlay.setFocusable(true);
    exportOverlay.setVisibility(View.GONE);

    exportStatusText = new TextView(this);
    exportStatusText.setText(R.string.export_in_progress);
    exportStatusText.setTextColor(0xFFFFFFFF);
    exportStatusText.setTextSize(18);
    exportStatusText.setGravity(android.view.Gravity.CENTER);
    exportStatusText.setVisibility(View.GONE);

    ViewGroup rootView = findViewById(android.R.id.content);

    FrameLayout.LayoutParams overlayParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.MATCH_PARENT
    );
    rootView.addView(exportOverlay, overlayParams);

    FrameLayout.LayoutParams textParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT
    );
    textParams.gravity = android.view.Gravity.CENTER;
    rootView.addView(exportStatusText, textParams);
  }

  private void setupBackHandler() {
    // If export is in progress, do nothing (block navigation)
    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (!isExportInProgress) {
          setEnabled(false);
          getOnBackPressedDispatcher().onBackPressed();
        }
        // If export is in progress, do nothing (block navigation)
      }
    };
    getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
  }

  public void showExportOverlay(boolean isExportAll) {
    if (exportOverlay != null && exportStatusText != null) {
      isExportInProgress = true;
      exportOverlay.setVisibility(View.VISIBLE);
      exportStatusText.setVisibility(View.VISIBLE);

      if (isExportAll) {
        exportStatusText.setText(R.string.export_all_in_progress);
      } else {
        exportStatusText.setText(R.string.export_in_progress);
      }

      exportOverlay.bringToFront();
      exportStatusText.bringToFront();

      // Disable action bar navigation
      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
    }
  }

  public void showUploadOverlay(boolean isUploadAll) {
    if (exportOverlay != null && exportStatusText != null) {
      isExportInProgress = true;
      exportOverlay.setVisibility(View.VISIBLE);
      exportStatusText.setVisibility(View.VISIBLE);

      if (isUploadAll) {
        exportStatusText.setText(R.string.upload_all_in_progress);
      } else {
        exportStatusText.setText(R.string.upload_in_progress);
      }

      exportOverlay.bringToFront();
      exportStatusText.bringToFront();

      // Disable action bar navigation
      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
    }
  }

  // Alias for consistency - both export and upload use the same hide method
  public void hideUploadOverlay() {
    hideExportOverlay();
  }

  public void hideExportOverlay() {
    if (exportOverlay != null && exportStatusText != null) {
      isExportInProgress = false;
      exportOverlay.setVisibility(View.GONE);
      exportStatusText.setVisibility(View.GONE);

      // Re-enable action bar navigation
      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.action_menu, menu);
    MenuItem itemButton = menu.findItem(R.id.action_bar_button);
    itemButton.setActionView(R.layout.action_menu_button);

    ImageButton actionButton = Objects
        .requireNonNull(itemButton.getActionView())
        .findViewById(R.id.action_button);
    actionButton.setBackground(null);

    int nightMode = AppCompatDelegate.getDefaultNightMode();
    switch (nightMode) {
      case AppCompatDelegate.MODE_NIGHT_YES:
        actionButton.setImageResource(R.drawable.outline_dark_mode_black_24dp);
        break;
      case AppCompatDelegate.MODE_NIGHT_NO:
        actionButton.setImageResource(R.drawable.outline_light_mode_black_24dp);
        break;
      default:
        actionButton.setImageResource(R.drawable.outline_brightness_auto_black_24dp);
        break;
    }

    actionButton.setOnClickListener(v -> {
      switch (AppCompatDelegate.getDefaultNightMode()) {
        case AppCompatDelegate.MODE_NIGHT_YES:
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
          PreferenceManager
              .getDefaultSharedPreferences(this)
              .edit()
              .putInt(Constants.KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO)
              .apply();
          actionButton.setImageResource(R.drawable.outline_light_mode_black_24dp);
          break;
        case AppCompatDelegate.MODE_NIGHT_NO:
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
          PreferenceManager
              .getDefaultSharedPreferences(this)
              .edit()
              .putInt(Constants.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
              .apply();
          actionButton.setImageResource(R.drawable.outline_brightness_auto_black_24dp);
          break;
        default:
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
          PreferenceManager
              .getDefaultSharedPreferences(this)
              .edit()
              .putInt(Constants.KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES)
              .apply();
          actionButton.setImageResource(R.drawable.outline_dark_mode_black_24dp);
          break;
      }
    });

    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (exportOverlay != null || exportStatusText != null) {
      ViewGroup rootView = findViewById(android.R.id.content);
      if (rootView != null) {
        if (exportOverlay != null) {
          rootView.removeView(exportOverlay);
        }
        if (exportStatusText != null) {
          rootView.removeView(exportStatusText);
        }
      }
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    if (!isExportInProgress) {
      finish();
      return true;
    }
    // Block navigation if export is in progress
    return false;
  }
}

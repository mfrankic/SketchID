package com.mfrankic.sketchid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings_container, new SettingsFragment())
          .commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.action_menu, menu);
    MenuItem itemButton = menu.findItem(R.id.action_bar_button);
    itemButton.setActionView(R.layout.action_menu_button);

    ImageButton actionButton = Objects.requireNonNull(itemButton.getActionView()).findViewById(
        R.id.action_button);
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
          PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.KEY_THEME,
              AppCompatDelegate.MODE_NIGHT_NO).apply();
          actionButton.setImageResource(R.drawable.outline_light_mode_black_24dp);
          break;
        case AppCompatDelegate.MODE_NIGHT_NO:
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
          PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.KEY_THEME,
              AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
          actionButton.setImageResource(R.drawable.outline_brightness_auto_black_24dp);
          break;
        default:
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
          PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.KEY_THEME,
              AppCompatDelegate.MODE_NIGHT_YES).apply();
          actionButton.setImageResource(R.drawable.outline_dark_mode_black_24dp);
          break;
      }
    });

    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    finish();
    return true;
  }
}

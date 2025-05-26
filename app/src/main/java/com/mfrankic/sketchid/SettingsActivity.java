package com.mfrankic.sketchid;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends BaseActivity {

  private final Map<String, View> progressItemViews = new HashMap<>();
  private View exportOverlay;
  private TextView exportStatusText;
  private LinearLayout progressItemsLayout;
  private boolean isExportInProgress = false;
  private View mainOverlayLayoutView;
  private Button okButton;

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
    exportOverlay.setBackgroundColor(0xCC000000);
    exportOverlay.setClickable(true);
    exportOverlay.setFocusable(true);
    exportOverlay.setVisibility(View.GONE);

    LinearLayout mainOverlayLayout = new LinearLayout(this);
    mainOverlayLayout.setOrientation(LinearLayout.VERTICAL);
    mainOverlayLayout.setGravity(Gravity.CENTER);
    mainOverlayLayout.setVisibility(View.GONE);
    mainOverlayLayoutView = mainOverlayLayout;

    exportStatusText = new TextView(this);
    exportStatusText.setText(R.string.export_in_progress);
    exportStatusText.setTextColor(0xFFFFFFFF);
    exportStatusText.setTextSize(20);
    exportStatusText.setGravity(android.view.Gravity.CENTER);
    LinearLayout.LayoutParams statusTextParams
        = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    statusTextParams.setMargins(0, 0, 0, dpToPx(20));
    mainOverlayLayout.addView(exportStatusText, statusTextParams);

    progressItemsLayout = new LinearLayout(this);
    progressItemsLayout.setOrientation(LinearLayout.VERTICAL);
    progressItemsLayout.setGravity(Gravity.START);
    LinearLayout.LayoutParams progressLayoutParams
        = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    progressLayoutParams.setMargins(dpToPx(20), 0, dpToPx(20), 0);
    mainOverlayLayout.addView(progressItemsLayout, progressLayoutParams);

    okButton = new Button(this);
    okButton.setText(android.R.string.ok);
    okButton.setVisibility(View.GONE);
    LinearLayout.LayoutParams buttonParams
        = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    buttonParams.setMargins(0, dpToPx(20), 0, 0);
    okButton.setOnClickListener(v -> hideExportOverlay());
    mainOverlayLayout.addView(okButton, buttonParams);

    ViewGroup rootView = findViewById(android.R.id.content);

    FrameLayout.LayoutParams overlayScreenParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.MATCH_PARENT
    );
    rootView.addView(exportOverlay, overlayScreenParams);

    FrameLayout.LayoutParams mainLayoutParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT
    );
    mainLayoutParams.gravity = android.view.Gravity.CENTER;
    rootView.addView(mainOverlayLayout, mainLayoutParams);
  }

  private int dpToPx(int dp) {
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        getResources().getDisplayMetrics()
    );
  }

  public void hideExportOverlay() {
    if (exportOverlay != null && mainOverlayLayoutView != null) {
      isExportInProgress = false;
      exportOverlay.setVisibility(View.GONE);
      mainOverlayLayoutView.setVisibility(View.GONE);
      if (okButton != null) {
        okButton.setVisibility(View.GONE);
      }
      clearProgressItems();

      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
    }
  }

  public void clearProgressItems() {
    runOnUiThread(() -> {
      if (progressItemsLayout != null) {
        progressItemsLayout.removeAllViews();
      }
      progressItemViews.clear();
    });
  }

  private void setupBackHandler() {
    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (!isExportInProgress) {
          setEnabled(false);
          getOnBackPressedDispatcher().onBackPressed();
        }
      }
    };
    getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
  }

  public void addProgressItem(String taskName) {
    runOnUiThread(() -> {
      if (progressItemsLayout == null) return;

      LinearLayout itemLayout = new LinearLayout(this);
      itemLayout.setOrientation(LinearLayout.HORIZONTAL);
      itemLayout.setGravity(Gravity.CENTER_VERTICAL);
      LinearLayout.LayoutParams itemParams
          = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
                                          ViewGroup.LayoutParams.WRAP_CONTENT
      );
      itemParams.setMargins(0, dpToPx(4), 0, dpToPx(4));

      ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
      spinner.setIndeterminate(true);
      LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
          dpToPx(20),
                                                                              dpToPx(20)
      );
      spinnerParams.setMarginEnd(dpToPx(10));
      itemLayout.addView(spinner, spinnerParams);

      TextView taskTextView = new TextView(this);
      taskTextView.setText(taskName);
      taskTextView.setTextColor(Color.WHITE);
      taskTextView.setTextSize(16);
      itemLayout.addView(taskTextView);

      progressItemsLayout.addView(itemLayout, itemParams);
      progressItemViews.put(taskName, itemLayout);
    });
  }

  public void updateProgressItemStatus(String taskName, boolean success) {
    runOnUiThread(() -> {
      View itemView = progressItemViews.get(taskName);
      if (itemView instanceof LinearLayout) {
        LinearLayout itemLayout = (LinearLayout) itemView;
        if (itemLayout.getChildCount() > 0 && itemLayout.getChildAt(0) instanceof ProgressBar) {
          itemLayout.removeViewAt(0);

          TextView statusView = new TextView(this);
          statusView.setText(success ? "✔" : "✘");
          statusView.setTextColor(success
                                  ? Color.parseColor("#4CAF50")
                                  : Color.parseColor("#F44336"));
          statusView.setTextSize(18);
          LinearLayout.LayoutParams statusParams
              = new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT
          );
          statusParams.setMarginEnd(dpToPx(10));
          itemLayout.addView(statusView, 0, statusParams);
        }
      }
    });
  }

  public void showExportOverlay() {
    if (exportOverlay != null && mainOverlayLayoutView != null && exportStatusText != null) {
      isExportInProgress = true;
      exportOverlay.setVisibility(View.VISIBLE);
      mainOverlayLayoutView.setVisibility(View.VISIBLE);

      exportStatusText.setText(R.string.export_in_progress);

      exportOverlay.bringToFront();
      mainOverlayLayoutView.bringToFront();

      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
    }
  }

  public void showOkButton() {
    runOnUiThread(() -> {
      if (okButton != null) {
        okButton.setVisibility(View.VISIBLE);
      }
    });
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

    if (exportOverlay != null || mainOverlayLayoutView != null) {
      ViewGroup rootView = findViewById(android.R.id.content);
      if (rootView != null) {
        if (exportOverlay != null) {
          rootView.removeView(exportOverlay);
        }
        if (mainOverlayLayoutView != null) {
          rootView.removeView(mainOverlayLayoutView);
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
    return false;
  }
}

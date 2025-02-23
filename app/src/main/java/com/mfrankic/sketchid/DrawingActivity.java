package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingActivity extends AppCompatActivity {
  private static final int ITEM_ATTEMPTS = 4;
  private final List<Item> items = new ArrayList<>();
  private final List<DrawingData> drawingDataList = new ArrayList<>();
  private FrameLayout drawingLayout;
  private CustomDrawingView drawingView;
  private ViewGroup.MarginLayoutParams drawingViewParams;
  private ImageView referenceImage;
  private int currentItemIndex = 0;
  private int currentItemAttempt = 1;
  private int selectedUserID;
  private Long startTimestamp;
  private AppDatabase db;
  private Executor executor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        new AlertDialog.Builder(DrawingActivity.this)
            .setTitle("Exit Drawing")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes", (dialog, which) -> DrawingActivity.super.finish())
            .setNegativeButton("No", null)
            .show();
      }
    });

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();

    selectedUserID = getIntent().getIntExtra("userID", -1);

    executor.execute(() -> {
      if (selectedUserID == -1 || db.userDao().getUserByID(selectedUserID) == null) {
        runOnUiThread(() -> {
          Toast.makeText(this, "Invalid user selected. Returning to Home.", Toast.LENGTH_LONG).show();
          finish();
        });
        return;
      }
      runOnUiThread(this::initializeDrawingActivity);
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadProgress();
    updateProgressText();
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveProgress();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      getOnBackPressedDispatcher().onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void saveProgress() {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putInt(KEY_CURRENT_ITEM_INDEX, currentItemIndex)
        .putInt(KEY_CURRENT_ITEM_ATTEMPT, currentItemAttempt)
        .apply();
  }

  private void loadProgress() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    currentItemIndex = prefs.getInt(KEY_CURRENT_ITEM_INDEX, 0);
    currentItemAttempt = prefs.getInt(KEY_CURRENT_ITEM_ATTEMPT, 1);
  }

  private void initializeDrawingActivity() {
    drawingLayout = findViewById(R.id.drawing_frame);
    drawingView = findViewById(R.id.drawing_view);
    drawingViewParams = (ViewGroup.MarginLayoutParams) drawingView.getLayoutParams();
    referenceImage = findViewById(R.id.reference_image);

    Button nextImageButton = findViewById(R.id.btn_next_image);
    Button clearButton = findViewById(R.id.btn_clear);

    executor.execute(this::loadItems);

    reInitializeDrawingView();

    nextImageButton.setOnClickListener(v -> {
      if (drawingDataList.isEmpty()) {
        Toast.makeText(this, "Please draw something before moving to the next image.",
            Toast.LENGTH_LONG).show();
        return;
      }

      DrawingData lastDrawingData = drawingDataList.get(drawingDataList.size() - 1).copy();
      lastDrawingData.action = "END";
      drawingDataList.add(lastDrawingData);

      saveDrawingToDatabase();

      currentItemAttempt++;

      if (currentItemAttempt > ITEM_ATTEMPTS) {
        currentItemAttempt = 1;
        currentItemIndex++;
      }

      if (currentItemIndex == (items.size() - 1) && currentItemAttempt == ITEM_ATTEMPTS) {
        nextImageButton.setText(R.string.finish);
      }

      if (currentItemIndex < items.size()) {
        loadCurrentItem();
        reInitializeDrawingView();
        updateProgressText();
      } else {
        currentItemIndex = 0;
        currentItemAttempt = 1;
        Intent intent = new Intent(DrawingActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
      }
    });

    clearButton.setOnClickListener(v -> {
      reInitializeDrawingView();
      drawingDataList.clear();
      startTimestamp = null;
    });
  }

  private void updateProgressText() {
    int currentOverallAttempt = (currentItemIndex * ITEM_ATTEMPTS) + currentItemAttempt;
    int totalAttempts = ITEM_ATTEMPTS * items.size();

    String progressText = String.format(
        Locale.getDefault(),
        "Attempt: %d/%d\tOverall: %d/%d",
        currentItemAttempt, ITEM_ATTEMPTS, currentOverallAttempt, totalAttempts
    );
    TextView attemptProgressText = findViewById(R.id.attempt_progress);
    attemptProgressText.setText(progressText);
  }

  private void reInitializeDrawingView() {
    drawingLayout.removeView(drawingView);
    drawingView = new CustomDrawingView(this, null);
    drawingView.setLayoutParams(drawingViewParams);
    drawingLayout.addView(drawingView);
    drawingView.setOnStrokeListener((x, y, timestamp, action) -> {
      long time = (startTimestamp == null) ? 0 : (timestamp - startTimestamp);
      int currentItemID = items.get(currentItemIndex).getId();
      Item.Type currentItemType = items.get(currentItemIndex).getType();

      int imageID = (currentItemType == Item.Type.IMAGE) ? currentItemID : -1;

      if (currentItemID != -1 && drawingDataList.isEmpty()) {
        startTimestamp = timestamp;
        drawingDataList.add(new DrawingData(time, x, y, "START", selectedUserID, imageID,
            items.get(currentItemIndex).getType(), currentItemAttempt));
      }

      drawingDataList.add(new DrawingData(time, x, y, action, selectedUserID, imageID,
          items.get(currentItemIndex).getType(), currentItemAttempt));
    });
  }

  private void saveDrawingToDatabase() {
    executor.execute(() -> {
      db.drawingDataDao().insertAll(drawingDataList);
      drawingDataList.clear();
      startTimestamp = null;
    });
  }

  private void loadItems() {
    List<Image> images = db.imageDao().getAllImages();

    items.clear();
    for (Image image : images) {
      items.add(new Item(image.id, image.name, image.source, Item.Type.IMAGE));
    }

    executor.execute(() -> runOnUiThread(() -> {
      if (!items.isEmpty()) {
        loadCurrentItem();
        updateProgressText();
      } else {
        Toast.makeText(this, "No items found. Please add items to draw.", Toast.LENGTH_LONG).show();
      }
    }));
  }

  private void loadCurrentItem() {
    Item currentItem = items.get(currentItemIndex);

    switch (currentItem.getType()) {
      case IMAGE:
        referenceImage.setImageResource(currentItem.getResource());
        referenceImage.setVisibility(View.VISIBLE);
        break;
    }
  }
}

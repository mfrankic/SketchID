package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingActivity extends AppCompatActivity {
  private static final int DEFAULT_ATTEMPTS = 4;
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
  private int itemAttempts;
  private String sessionId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    getOnBackPressedDispatcher().addCallback(
        this, new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            new AlertDialog.Builder(DrawingActivity.this)
                .setTitle("Exit Drawing")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> DrawingActivity.super.finish())
                .setNegativeButton("No", null)
                .show();
          }
        }
    );

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();

    selectedUserID = getIntent().getIntExtra("userID", -1);

    // Get the drawing attempts from preferences
    itemAttempts = getDrawingAttemptsFromPreferences();

    executor.execute(() -> {
      if (selectedUserID == -1 || db.userDao().getUserByID(selectedUserID) == null) {
        runOnUiThread(() -> {
          Toast
              .makeText(this, "Invalid user selected. Returning to Home.", Toast.LENGTH_LONG)
              .show();
          finish();
        });
        return;
      }

      // Initialize or get the session ID
      sessionId = UserProgressManager.getUserSessionId(this, selectedUserID);

      runOnUiThread(this::initializeDrawingActivity);
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveProgress();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadProgress();
    updateProgressText();
  }

  private void loadProgress() {
    // Load user-specific progress
    UserProgressManager.UserProgress progress = UserProgressManager.loadUserProgress(
        this,
        selectedUserID
    );
    currentItemIndex = progress.getItemIndex();
    currentItemAttempt = progress.getItemAttempt();
    sessionId = progress.getSessionId();
  }

  private void saveProgress() {
    // Save user-specific progress
    UserProgressManager.saveUserProgress(
        this,
        selectedUserID,
        currentItemIndex,
        currentItemAttempt,
        sessionId
    );
  }

  private int getDrawingAttemptsFromPreferences() {
    String attempts = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString(KEY_DRAWING_ATTEMPTS, "-1");
    return attempts.equals("-1") ? DEFAULT_ATTEMPTS : Integer.parseInt(attempts);
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
        Toast
            .makeText(
                this,
                "Please draw something before moving to the next image.",
                Toast.LENGTH_LONG
            )
            .show();
        return;
      }

      DrawingData lastDrawingData = drawingDataList.get(drawingDataList.size() - 1).copy();
      lastDrawingData.action = "END";
      drawingDataList.add(lastDrawingData);

      saveDrawingToDatabase();

      currentItemAttempt++;

      if (currentItemAttempt > itemAttempts) {
        currentItemAttempt = 1;
        currentItemIndex++;
      }

      if (currentItemIndex == (items.size() - 1) && currentItemAttempt == itemAttempts) {
        nextImageButton.setText(R.string.finish);
      }

      if (currentItemIndex < items.size()) {
        loadCurrentItem();
        reInitializeDrawingView();
        updateProgressText();
      } else {
        // Drawing session completed, mark user as finished and clear progress
        UserProgressManager.markUserAsFinished(this, selectedUserID);
        UserProgressManager.clearUserProgress(this, selectedUserID);
        // Also clear global progress keys for backward compatibility
        resetGlobalProgress();

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
        drawingDataList.add(new DrawingData(
            time,
            x,
            y,
            "START",
            selectedUserID,
            imageID,
            items.get(currentItemIndex).getType(),
            currentItemAttempt,
            sessionId
        ));
      }

      drawingDataList.add(new DrawingData(
          time,
          x,
          y,
          action,
          selectedUserID,
          imageID,
          items.get(currentItemIndex).getType(),
          currentItemAttempt,
          sessionId
      ));
    });
  }

  private void loadItems() {
    List<Image> allImages = db.imageDao().getAllImages();
    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);

    items.clear();
    List<Image> selectedImages = new ArrayList<>();
    for (Image image : allImages) {
      if (selectedImageIds.contains(image.id)) {
        selectedImages.add(image);
      }
    }

    // Check if random order is enabled
    SharedPreferences prefs = getSharedPreferences("image_order", MODE_PRIVATE);
    boolean isRandomOrder = prefs.getBoolean("random_order", false);

    if (isRandomOrder) {
      // Shuffle the selected images
      java.util.Collections.shuffle(selectedImages);
    } else {
      // Get saved order
      String json = prefs.getString("image_order", null);
      if (json != null) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        List<Integer> savedOrder = gson.fromJson(json, type);

        if (savedOrder != null && !savedOrder.isEmpty()) {
          // Sort images according to saved order
          List<Image> orderedImages = new ArrayList<>();
          for (Integer id : savedOrder) {
            for (Image image : selectedImages) {
              if (image.id == id) {
                orderedImages.add(image);
                break;
              }
            }
          }
          // Add any new images that weren't in the saved order
          for (Image image : selectedImages) {
            if (!orderedImages.contains(image)) {
              orderedImages.add(image);
            }
          }
          selectedImages = orderedImages;
        }
      }
    }

    for (Image image : selectedImages) {
      items.add(new Item(image.id, image.name, image.source, image.path, Item.Type.IMAGE));
    }

    executor.execute(() -> runOnUiThread(() -> {
      if (!items.isEmpty()) {
        loadCurrentItem();
        updateProgressText();
      } else {
        Toast
            .makeText(
                this,
                "No images selected. Please select images in settings.",
                Toast.LENGTH_LONG
            )
            .show();
        finish();
      }
    }));
  }

  private void updateProgressText() {
    int currentOverallAttempt = (currentItemIndex * itemAttempts) + currentItemAttempt;
    int totalAttempts = itemAttempts * items.size();

    String progressText = String.format(
        Locale.getDefault(),
        "Attempt: %d/%d\tOverall: %d/%d",
        currentItemAttempt,
        itemAttempts,
        currentOverallAttempt,
        totalAttempts
    );
    TextView attemptProgressText = findViewById(R.id.attempt_progress);
    attemptProgressText.setText(progressText);
  }

  private void loadCurrentItem() {
    Item currentItem = items.get(currentItemIndex);

    switch (currentItem.getType()) {
      case IMAGE:
        if (currentItem.getSource().equals("default")) {
          referenceImage.setImageTintList(getResources().getColorStateList(
              R.color.onSurface,
              null
          ));
          int resourceId = Integer.parseInt(currentItem.getPath());
          referenceImage.setImageResource(resourceId);
        } else {
          referenceImage.setImageTintList(null);
          referenceImage.setImageURI(android.net.Uri.parse(currentItem.getPath()));
        }
        referenceImage.setVisibility(View.VISIBLE);
        break;
    }
  }

  // Method to reset global progress keys used by legacy code
  private void resetGlobalProgress() {
    // For backward compatibility, also clear the global progress keys
    PreferenceManager
        .getDefaultSharedPreferences(this)
        .edit()
        .remove(KEY_CURRENT_ITEM_INDEX)
        .remove(KEY_CURRENT_ITEM_ATTEMPT)
        .apply();
  }

  private void saveDrawingToDatabase() {
    executor.execute(() -> {
      db.drawingDataDao().insertAll(drawingDataList);
      drawingDataList.clear();
      startTimestamp = null;
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      getOnBackPressedDispatcher().onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  @Override
  public void onBackPressed() {
    getOnBackPressedDispatcher().onBackPressed();
  }
}

package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.ACTION_END;
import static com.mfrankic.sketchid.Constants.ACTION_START;
import static com.mfrankic.sketchid.Constants.DIALOG_MSG_EXIT_DRAWING;
import static com.mfrankic.sketchid.Constants.DIALOG_TITLE_EXIT_DRAWING;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_ANDROID_VERSION;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_DEVICE_MODEL;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_MODE;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_SELECTED_IMAGE_IDS;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_TIME_STARTED;
import static com.mfrankic.sketchid.Constants.FORMAT_PROGRESS_TEXT;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.NO_BUTTON;
import static com.mfrankic.sketchid.Constants.PREF_IMAGE_ORDER;
import static com.mfrankic.sketchid.Constants.TOAST_INVALID_ATTEMPTS_NUMBER;
import static com.mfrankic.sketchid.Constants.TOAST_INVALID_USER;
import static com.mfrankic.sketchid.Constants.TOAST_NO_DRAWING;
import static com.mfrankic.sketchid.Constants.TOAST_NO_IMAGES_SETTINGS;
import static com.mfrankic.sketchid.Constants.YES_BUTTON;
import static com.mfrankic.sketchid.SensorDataManager.BASELINE_TARGET_SAMPLE_COUNT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingActivity extends BaseActivity {
  private final List<Item> items = new ArrayList<>();
  private final List<DrawingData> drawingDataList = new ArrayList<>();
  private FrameLayout drawingLayout;
  private CustomDrawingView drawingView;
  private ViewGroup.MarginLayoutParams drawingViewParams;
  private int currentItemIndex = 0;
  private int currentItemAttempt = 1;
  private int selectedUserID;
  private AppDatabase db;
  private Executor executor;
  private int itemAttempts;
  private String sessionId;
  private UserProgressManager.Session currentSession;
  private Button nextImageButton;
  private SensorDataManager sensorDataManager;

  private View baselineOverlay;
  private TextView baselineStatusText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    setupBackHandler();

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();
    sensorDataManager = new SensorDataManager(this);

    selectedUserID = getIntent().getIntExtra("userID", -1);

    itemAttempts = getDrawingAttemptsFromPreferences();

    executor.execute(() -> {
      if (selectedUserID == -1 || db.userDao().getUserByID(selectedUserID) == null) {
        runOnUiThread(() -> {
          Toast.makeText(this, TOAST_INVALID_USER, Toast.LENGTH_LONG).show();
          finish();
        });
        return;
      }

      initializeSession();

      runOnUiThread(this::initializeDrawingActivity);
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveProgress();

    if (baselineOverlay != null && baselineOverlay.getVisibility() == View.VISIBLE) {
      hideBaselineOverlay();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadProgress();
    updateProgressText();
  }

  private void loadProgress() {
    if (currentSession != null) {

      currentItemIndex = currentSession.getItemIndex();
      currentItemAttempt = currentSession.getItemAttempt();
    }
  }

  private void updateProgressText() {
    int currentOverallAttempt = (currentItemIndex * itemAttempts) + currentItemAttempt;
    int totalAttempts = itemAttempts * items.size();

    String progressText = String.format(
        Locale.getDefault(),
        FORMAT_PROGRESS_TEXT,
        currentItemAttempt,
        itemAttempts,
        currentOverallAttempt,
        totalAttempts
    );
    TextView attemptProgressText = findViewById(R.id.attempt_progress);
    attemptProgressText.setText(progressText);
  }

  private void saveProgress() {
    if (currentSession != null) {

      UserProgressManager.updateSessionProgress(
          this,
          selectedUserID,
          sessionId,
          currentItemIndex,
          currentItemAttempt
      );
    }
  }

  private void hideBaselineOverlay() {
    if (baselineOverlay != null && baselineStatusText != null) {
      baselineOverlay.setVisibility(View.GONE);
      baselineStatusText.setVisibility(View.GONE);

      Log.d("DrawingActivity", "Baseline overlay hidden - interactions restored");
    }
  }

  private void setupBackHandler() {
    getOnBackPressedDispatcher().addCallback(
        this, new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            showExitConfirmationDialog();
          }
        }
    );
  }

  private void showExitConfirmationDialog() {
    new AlertDialog.Builder(DrawingActivity.this)
        .setTitle(DIALOG_TITLE_EXIT_DRAWING)
        .setMessage(DIALOG_MSG_EXIT_DRAWING)
        .setPositiveButton(YES_BUTTON, (dialog, which) -> DrawingActivity.super.finish())
        .setNegativeButton(NO_BUTTON, null)
        .show();
  }

  private void initializeSession() {

    List<UserProgressManager.Session> unfinishedSessions
        = UserProgressManager.getUnfinishedSessions(this, selectedUserID);

    if (!unfinishedSessions.isEmpty()) {

      currentSession = unfinishedSessions.get(0);
    } else {

      currentSession = UserProgressManager.createDrawingSession(
          this,
          selectedUserID,
          collectDrawingSettings()
      );
    }
    sessionId = currentSession.getSessionId();
  }

  private Map<String, Object> collectDrawingSettings() {
    Map<String, Object> settings = new HashMap<>();

    settings.put(DRAWING_KEY_ATTEMPTS, itemAttempts);
    settings.put(DRAWING_KEY_TIME_STARTED, System.currentTimeMillis());

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String drawingMode = prefs.getString(Constants.KEY_DRAWING_MODE, Constants.DRAWING_MODE_NORMAL);
    settings.put(DRAWING_KEY_MODE, drawingMode);

    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);
    settings.put(DRAWING_KEY_SELECTED_IMAGE_IDS, new ArrayList<>(selectedImageIds));

    settings.put(DRAWING_KEY_DEVICE_MODEL, Build.MODEL);
    settings.put(DRAWING_KEY_ANDROID_VERSION, Build.VERSION.RELEASE);

    return settings;
  }

  private void initializeDrawingActivity() {
    findAndSetupViews();
    executor.execute(this::loadItems);
    reInitializeDrawingView();
    setupButtonListeners();
  }

  private void findAndSetupViews() {
    drawingLayout = findViewById(R.id.drawing_frame);
    drawingView = findViewById(R.id.drawing_view);
    drawingViewParams = (ViewGroup.MarginLayoutParams) drawingView.getLayoutParams();
    nextImageButton = findViewById(R.id.btn_next_image);

    setupBaselineOverlay();
  }

  private void setupBaselineOverlay() {
    baselineOverlay = new View(this);
    baselineOverlay.setBackgroundColor(0x80000000); // Semi-transparent black
    baselineOverlay.setClickable(true);
    baselineOverlay.setFocusable(true);
    baselineOverlay.setVisibility(View.GONE);

    baselineStatusText = new TextView(this);
    baselineStatusText.setText(R.string.baseline_collecting_simple);
    baselineStatusText.setTextColor(0xFFFFFFFF);
    baselineStatusText.setTextSize(18);
    baselineStatusText.setGravity(android.view.Gravity.CENTER);
    baselineStatusText.setVisibility(View.GONE);

    ViewGroup rootView = findViewById(android.R.id.content);

    FrameLayout.LayoutParams overlayParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.MATCH_PARENT
    );
    rootView.addView(baselineOverlay, overlayParams);

    FrameLayout.LayoutParams textParams
        = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT
    );
    textParams.gravity = android.view.Gravity.CENTER;
    rootView.addView(baselineStatusText, textParams);
  }

  private void setupButtonListeners() {
    setupNextImageButton();
    setupClearButton();
  }

  private void setupNextImageButton() {
    nextImageButton.setOnClickListener(v -> handleNextImageClick());
  }

  private void handleNextImageClick() {
    if (drawingDataList.isEmpty()) {
      Toast.makeText(this, TOAST_NO_DRAWING, Toast.LENGTH_LONG).show();
      return;
    }

    finalizeCurrentDrawing();
    updateDrawingProgress();
    proceedToNextDrawing();
  }

  private void finalizeCurrentDrawing() {
    sensorDataManager.stopCollecting();
    sensorDataManager.saveData();

    DrawingData lastDrawingData = drawingDataList.get(drawingDataList.size() - 1).copy();
    lastDrawingData.action = ACTION_END;
    drawingDataList.add(lastDrawingData);

    saveDrawingToDatabase();
    UserProgressManager.recordNextImage(this, selectedUserID, sessionId);
  }

  private void updateDrawingProgress() {

    currentItemAttempt++;

    if (currentItemAttempt > itemAttempts) {
      currentItemAttempt = 1;
      currentItemIndex++;
    }

    if (currentItemIndex == (items.size() - 1) && currentItemAttempt == itemAttempts) {
      nextImageButton.setText(R.string.finish);
    }
  }

  private void proceedToNextDrawing() {
    if (currentItemIndex < items.size()) {

      loadCurrentItem();
      reInitializeDrawingView();
      updateProgressText();
    } else {

      completeDrawingSession();
    }
  }

  private void completeDrawingSession() {

    UserProgressManager.markSessionFinished(this, selectedUserID, sessionId);

    currentItemIndex = 0;
    currentItemAttempt = 1;
    Intent intent = new Intent(DrawingActivity.this, HomeActivity.class);
    startActivity(intent);
    finish();
  }

  private void setupClearButton() {
    Button clearButton = findViewById(R.id.btn_clear);
    clearButton.setOnClickListener(v -> {
      reInitializeDrawingView();
      drawingDataList.clear();
      sensorDataManager.clearData();
    });
  }

  private void reInitializeDrawingView() {
    drawingLayout.removeView(drawingView);
    drawingView = new CustomDrawingView(this, null);
    drawingView.setLayoutParams(drawingViewParams);
    drawingLayout.addView(drawingView);
    setupDrawingStrokeListener();
  }

  private void setupDrawingStrokeListener() {
    drawingView.setOnStrokeListener((x, y, timestamp, action, size, pressure, orientation) -> {
      int currentItemID = items.get(currentItemIndex).getId();
      Item.Type currentItemType = items.get(currentItemIndex).getType();

      int imageID = (currentItemType == Item.Type.IMAGE) ? currentItemID : -1;

      if (currentItemID != -1 && drawingDataList.isEmpty()) {
        createDrawingDataPoint(new DrawingPointParams.Builder()
                                   .time(timestamp)
                                   .x(x)
                                   .y(y)
                                   .action(ACTION_START)
                                   .imageID(imageID)
                                   .itemType(currentItemType)
                                   .size(size)
                                   .pressure(pressure)
                                   .orientation(orientation)
                                   .build());
      }

      createDrawingDataPoint(new DrawingPointParams.Builder()
                                 .time(timestamp)
                                 .x(x)
                                 .y(y)
                                 .action(action)
                                 .imageID(imageID)
                                 .itemType(currentItemType)
                                 .size(size)
                                 .pressure(pressure)
                                 .orientation(orientation)
                                 .build());
    });
  }

  private void createDrawingDataPoint(DrawingPointParams params) {
    drawingDataList.add(new DrawingData.Builder()
                            .time(params.time)
                            .x(params.x)
                            .y(params.y)
                            .action(params.action)
                            .userID(selectedUserID)
                            .imageID(params.imageID)
                            .itemType(params.itemType)
                            .attempt(currentItemAttempt)
                            .sessionID(sessionId)
                            .size(params.size)
                            .pressure(params.pressure)
                            .orientation(params.orientation)
                            .build());
  }

  private void loadItems() {

    List<Image> allImages = db.imageDao().getAllImages();
    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);

    items.clear();
    List<Image> selectedImages = getSelectedImages(allImages, selectedImageIds);

    List<Image> orderedImages = orderImages(selectedImages);

    for (Image image : orderedImages) {
      items.add(new Item(image.id, image.name, image.source, image.path, Item.Type.IMAGE));
    }

    updateUIWithItems();
  }

  private List<Image> getSelectedImages(List<Image> allImages, Set<Integer> selectedImageIds) {
    List<Image> selectedImages = new ArrayList<>();
    for (Image image : allImages) {
      if (selectedImageIds.contains(image.id)) {
        selectedImages.add(image);
      }
    }
    return selectedImages;
  }

  private List<Image> orderImages(List<Image> selectedImages) {
    SharedPreferences prefs = getSharedPreferences(PREF_IMAGE_ORDER, MODE_PRIVATE);

    return applySavedOrder(selectedImages, prefs);
  }

  private List<Image> applySavedOrder(List<Image> selectedImages, SharedPreferences prefs) {
    String json = prefs.getString(PREF_IMAGE_ORDER, null);
    if (json == null) {
      return selectedImages;
    }

    try {
      List<Integer> savedOrder = parseOrderJson(json);
      if (savedOrder == null || savedOrder.isEmpty()) {
        return selectedImages;
      }

      return createOrderedImageList(selectedImages, savedOrder);
    } catch (Exception e) {
      return selectedImages;
    }
  }

  private List<Integer> parseOrderJson(String json) {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Integer>>() {
    }.getType();
    return gson.fromJson(json, type);
  }

  private List<Image> createOrderedImageList(List<Image> selectedImages, List<Integer> savedOrder) {
    List<Image> orderedImages = new ArrayList<>();

    for (Integer id : savedOrder) {
      for (Image image : selectedImages) {
        if (image.id == id) {
          orderedImages.add(image);
          break;
        }
      }
    }

    for (Image image : selectedImages) {
      if (!orderedImages.contains(image)) {
        orderedImages.add(image);
      }
    }

    return orderedImages;
  }

  private void updateUIWithItems() {
    executor.execute(() -> runOnUiThread(() -> {
      if (!items.isEmpty()) {
        loadCurrentItem();
        updateProgressText();
      } else {
        Toast.makeText(this, TOAST_NO_IMAGES_SETTINGS, Toast.LENGTH_LONG).show();
        finish();
      }
    }));
  }

  private void loadCurrentItem() {
    Item currentItem = items.get(currentItemIndex);

    if (currentItem.getType() == Item.Type.IMAGE) {
      displayImageItem(currentItem);
      int imageId = currentItem.getId();

      showBaselineOverlay();

      sensorDataManager.startBaselineCollection(
          selectedUserID, imageId, sessionId, currentItemAttempt, success -> runOnUiThread(() -> {
            hideBaselineOverlay();
            if (success) {
              Toast
                  .makeText(
                      DrawingActivity.this,
                      R.string.baseline_collection_success,
                      Toast.LENGTH_SHORT
                  )
                  .show();
            } else {
              Toast
                  .makeText(
                      DrawingActivity.this,
                      R.string.baseline_collection_failed,
                      Toast.LENGTH_SHORT
                  )
                  .show();
            }
            sensorDataManager.startCollecting(
                selectedUserID,
                imageId,
                sessionId,
                currentItemAttempt
            );
          })
      );
    }
  }

  private void showBaselineOverlay() {
    if (baselineOverlay != null && baselineStatusText != null) {
      baselineOverlay.setVisibility(View.VISIBLE);
      baselineStatusText.setVisibility(View.VISIBLE);
      baselineStatusText.setText(getString(
          R.string.baseline_collecting_with_count,
          BASELINE_TARGET_SAMPLE_COUNT
      ));

      baselineOverlay.bringToFront();
      baselineStatusText.bringToFront();

      Log.d("DrawingActivity", "Baseline overlay shown - all interactions blocked");
    }
  }

  private void displayImageItem(Item currentItem) {
    if (currentItem == null) {
      return;
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String drawingMode = prefs.getString(Constants.KEY_DRAWING_MODE, Constants.DRAWING_MODE_NORMAL);

    ImageView mainRefImage = findViewById(R.id.reference_image);
    ImageView overlayRefImage = findViewById(R.id.overlay_reference_image);

    ImageLoader.loadImageIntoView(this, currentItem, mainRefImage);
    ImageLoader.loadImageIntoView(this, currentItem, overlayRefImage);

    if (Constants.DRAWING_MODE_OVERLAY.equals(drawingMode)) {

      mainRefImage.setVisibility(View.GONE);
      overlayRefImage.setVisibility(View.VISIBLE);
    } else {

      mainRefImage.setVisibility(View.VISIBLE);
      overlayRefImage.setVisibility(View.GONE);
    }
  }

  private void saveDrawingToDatabase() {
    executor.execute(() -> {
      db.drawingDataDao().insertAll(drawingDataList);
      drawingDataList.clear();
    });
  }

  private int getDrawingAttemptsFromPreferences() {
    String attempts = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString(KEY_DRAWING_ATTEMPTS, "-1");
    int att = -1;
    try {
      att = Integer.parseInt(attempts);
    } catch (NumberFormatException e) {
      Toast.makeText(this, TOAST_INVALID_ATTEMPTS_NUMBER, Toast.LENGTH_LONG).show();
      finish();
    }

    if (att == -1) {
      Toast.makeText(this, TOAST_INVALID_ATTEMPTS_NUMBER, Toast.LENGTH_LONG).show();
      finish();
    }

    return att;
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
  public void onDestroy() {
    super.onDestroy();

    if (sensorDataManager != null) {
      sensorDataManager.stopCollecting();
    }

    if (baselineOverlay != null || baselineStatusText != null) {
      ViewGroup rootView = findViewById(android.R.id.content);
      if (rootView != null) {
        if (baselineOverlay != null) {
          rootView.removeView(baselineOverlay);
        }
        if (baselineStatusText != null) {
          rootView.removeView(baselineStatusText);
        }
      }
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }

  private static class DrawingPointParams {
    final long time;
    final float x;
    final float y;
    final String action;
    final int imageID;
    final Item.Type itemType;
    final float size;
    final float pressure;
    final float orientation;

    private DrawingPointParams(Builder builder) {
      this.time = builder.time;
      this.x = builder.x;
      this.y = builder.y;
      this.action = builder.action;
      this.imageID = builder.imageID;
      this.itemType = builder.itemType;
      this.size = builder.size;
      this.pressure = builder.pressure;
      this.orientation = builder.orientation;
    }

    static class Builder {
      private long time;
      private float x;
      private float y;
      private String action;
      private int imageID;
      private Item.Type itemType;
      private float size;
      private float pressure;
      private float orientation;

      Builder time(long time) {
        this.time = time;
        return this;
      }

      Builder x(float x) {
        this.x = x;
        return this;
      }

      Builder y(float y) {
        this.y = y;
        return this;
      }

      Builder action(String action) {
        this.action = action;
        return this;
      }

      Builder imageID(int imageID) {
        this.imageID = imageID;
        return this;
      }

      Builder itemType(Item.Type itemType) {
        this.itemType = itemType;
        return this;
      }

      Builder size(float size) {
        this.size = size;
        return this;
      }

      Builder pressure(float pressure) {
        this.pressure = pressure;
        return this;
      }

      Builder orientation(float orientation) {
        this.orientation = orientation;
        return this;
      }

      DrawingPointParams build() {
        return new DrawingPointParams(this);
      }
    }
  }
}

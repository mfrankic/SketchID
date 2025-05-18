package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.ACTION_END;
import static com.mfrankic.sketchid.Constants.ACTION_START;
import static com.mfrankic.sketchid.Constants.DIALOG_MSG_EXIT_DRAWING;
import static com.mfrankic.sketchid.Constants.DIALOG_TITLE_EXIT_DRAWING;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_ANDROID_VERSION;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_DEVICE_MODEL;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_SELECTED_IMAGE_IDS;
import static com.mfrankic.sketchid.Constants.DRAWING_KEY_TIME_STARTED;
import static com.mfrankic.sketchid.Constants.FORMAT_PROGRESS_TEXT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.NO_BUTTON;
import static com.mfrankic.sketchid.Constants.PREF_IMAGE_ORDER;
import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;
import static com.mfrankic.sketchid.Constants.TOAST_INVALID_ATTEMPTS_NUMBER;
import static com.mfrankic.sketchid.Constants.TOAST_INVALID_USER;
import static com.mfrankic.sketchid.Constants.TOAST_NO_DRAWING;
import static com.mfrankic.sketchid.Constants.TOAST_NO_IMAGES_SETTINGS;
import static com.mfrankic.sketchid.Constants.YES_BUTTON;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingActivity extends AppCompatActivity {
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
  private UserProgressManager.Session currentSession;
  private Button nextImageButton;

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

    selectedUserID = getIntent().getIntExtra("userID", -1);

    // Get the drawing attempts from preferences
    itemAttempts = getDrawingAttemptsFromPreferences();

    executor.execute(() -> {
      if (selectedUserID == -1 || db.userDao().getUserByID(selectedUserID) == null) {
        runOnUiThread(() -> {
          Toast.makeText(this, TOAST_INVALID_USER, Toast.LENGTH_LONG).show();
          finish();
        });
        return;
      }

      // Get or create a new drawing session for the user
      initializeSession();

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
    if (currentSession != null) {
      // Load progress from the session
      currentItemIndex = currentSession.getItemIndex();
      currentItemAttempt = currentSession.getItemAttempt();
    } else {
      // Fallback to legacy method
      UserProgressManager.UserProgress progress = UserProgressManager.loadUserProgress(
          this,
          selectedUserID
      );
      currentItemIndex = progress.getItemIndex();
      currentItemAttempt = progress.getItemAttempt();
      sessionId = progress.getSessionId();
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
      // Update session progress
      UserProgressManager.updateSessionProgress(
          this,
          selectedUserID,
          sessionId,
          currentItemIndex,
          currentItemAttempt
      );
    } else {
      // Fallback to legacy method
      UserProgressManager.saveUserProgress(
          this,
          selectedUserID,
          currentItemIndex,
          currentItemAttempt,
          sessionId
      );
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
    // Check for existing sessions
    List<UserProgressManager.Session> unfinishedSessions
        = UserProgressManager.getUnfinishedSessions(this, selectedUserID);

    if (!unfinishedSessions.isEmpty()) {
      // Use the first unfinished session
      currentSession = unfinishedSessions.get(0);
    } else {
      // Create a new session with drawing settings
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

    // Save important drawing settings to the session
    settings.put(DRAWING_KEY_ATTEMPTS, itemAttempts);
    settings.put(DRAWING_KEY_TIME_STARTED, System.currentTimeMillis());

    // Add any other relevant settings
    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);
    settings.put(DRAWING_KEY_SELECTED_IMAGE_IDS, new ArrayList<>(selectedImageIds));

    // Add device info
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
    referenceImage = findViewById(R.id.reference_image);
    nextImageButton = findViewById(R.id.btn_next_image);
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
    // Add END action to the drawing data
    DrawingData lastDrawingData = drawingDataList.get(drawingDataList.size() - 1).copy();
    lastDrawingData.action = ACTION_END;
    drawingDataList.add(lastDrawingData);

    // Save data and record progress
    saveDrawingToDatabase();
    UserProgressManager.recordNextImage(this, selectedUserID, sessionId);
  }

  private void updateDrawingProgress() {
    // Update attempt counters
    currentItemAttempt++;

    if (currentItemAttempt > itemAttempts) {
      currentItemAttempt = 1;
      currentItemIndex++;
    }

    // Update UI for last item
    if (currentItemIndex == (items.size() - 1) && currentItemAttempt == itemAttempts) {
      nextImageButton.setText(R.string.finish);
    }
  }

  private void proceedToNextDrawing() {
    if (currentItemIndex < items.size()) {
      // Continue with next image
      loadCurrentItem();
      reInitializeDrawingView();
      updateProgressText();
    } else {
      // Complete the drawing session
      completeDrawingSession();
    }
  }

  private void completeDrawingSession() {
    // Mark session as finished
    UserProgressManager.markSessionFinished(this, selectedUserID, sessionId);

    // Legacy: Mark user as finished and clear progress
    UserProgressManager.markUserAsFinished(this, selectedUserID);

    // Also clear global progress keys for backward compatibility
    resetGlobalProgress();

    // Reset state and navigate to home
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
      startTimestamp = null;
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
    drawingView.setOnStrokeListener((x, y, timestamp, action) -> {
      long time = (startTimestamp == null) ? 0 : (timestamp - startTimestamp);
      int currentItemID = items.get(currentItemIndex).getId();
      Item.Type currentItemType = items.get(currentItemIndex).getType();

      int imageID = (currentItemType == Item.Type.IMAGE) ? currentItemID : -1;

      // Create initial START event if this is the first point
      if (currentItemID != -1 && drawingDataList.isEmpty()) {
        startTimestamp = timestamp;
        createDrawingDataPoint(time, x, y, ACTION_START, imageID, currentItemType);
      }

      // Always add the current point with the provided action
      createDrawingDataPoint(time, x, y, action, imageID, currentItemType);
    });
  }

  private void createDrawingDataPoint(
      long time,
      float x,
      float y,
      String action,
      int imageID,
      Item.Type itemType
  ) {
    drawingDataList.add(new DrawingData.Builder()
                            .time(time)
                            .x(x)
                            .y(y)
                            .action(action)
                            .userID(selectedUserID)
                            .imageID(imageID)
                            .itemType(itemType)
                            .attempt(currentItemAttempt)
                            .sessionID(sessionId)
                            .build());
  }

  private void loadItems() {
    // Get raw data
    List<Image> allImages = db.imageDao().getAllImages();
    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);

    // Filter to selected images
    items.clear();
    List<Image> selectedImages = getSelectedImages(allImages, selectedImageIds);

    // Process ordering
    List<Image> orderedImages = orderImages(selectedImages);

    // Convert to items
    for (Image image : orderedImages) {
      items.add(new Item(image.id, image.name, image.source, image.path, Item.Type.IMAGE));
    }

    // Update UI
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

    // First add images in specified order
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
    }
  }

  private void displayImageItem(Item currentItem) {
    if (currentItem.getSource().equals(SOURCE_DEFAULT)) {
      // Default image from resources
      referenceImage.setImageTintList(getResources().getColorStateList(R.color.onSurface, null));
      int resourceId = Integer.parseInt(currentItem.getPath());
      referenceImage.setImageResource(resourceId);
    } else {
      // Custom image from URI
      referenceImage.setImageTintList(null);
      referenceImage.setImageURI(android.net.Uri.parse(currentItem.getPath()));
    }
    referenceImage.setVisibility(View.VISIBLE);
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
  }

  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }
}

package com.mfrankic.sketchid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ImageSelectionActivity extends AppCompatActivity {
  private static final String PREF_NAME = "image_order";
  private static final String KEY_RANDOM_ORDER = "random_order";

  private final List<Image> allImages = new ArrayList<>();
  private final List<Image> selectedImages = new ArrayList<>();
  private RecyclerView recyclerViewUnselected;
  private RecyclerView recyclerViewSelected;
  private TextView textViewSelected;
  private TextView textViewSelectedNumber;
  private TextView textViewReorderingDisabled;
  private SwitchMaterial switchRandomOrder;
  private ImageAdapter unselectedAdapter;
  private ImageOrderAdapter selectedAdapter;
  private AppDatabase db;
  private Executor executor;
  private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
      new ActivityResultContracts.GetMultipleContents(), uris -> {
        if (uris != null && !uris.isEmpty()) {
          for (Uri uri : uris) {
            try {
              getContentResolver().takePersistableUriPermission(
                  uri,
                  Intent.FLAG_GRANT_READ_URI_PERMISSION
              );
              handleNewImageSelection(uri);
            } catch (SecurityException e) {
              Toast
                  .makeText(this, "Failed to get permission for: " + uri, Toast.LENGTH_SHORT)
                  .show();
            }
          }
        }
      }
  );
  private View btnAddImage;
  private boolean hasChanges = false;
  private View multiSelectControls;
  private boolean isMultiSelectMode = false;
  private ImageButton checkboxSelectAll;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_selection);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();

    // Setup multi-select controls
    multiSelectControls = findViewById(R.id.multiSelectControls);
    checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
    checkboxSelectAll.setOnClickListener(v -> handleSelectAllCheckboxClick());

    findViewById(R.id.btnAddToSelection).setOnClickListener(v -> addSelectedImagesToSelection());
    findViewById(R.id.btnRemoveSelected).setOnClickListener(v -> showRemoveConfirmationDialog());
    findViewById(R.id.btnExitMultiSelect).setOnClickListener(v -> exitMultiSelectMode());

    // Setup unselected images RecyclerView
    recyclerViewUnselected = findViewById(R.id.recyclerViewUnselected);
    int spanCount = calculateSpanCount();
    recyclerViewUnselected.setLayoutManager(new GridLayoutManager(this, spanCount));
    int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
    recyclerViewUnselected.addItemDecoration(new GridSpacingItemDecoration(
        spanCount,
                                                                           spacingInPixels,
                                                                           true
    ));
    unselectedAdapter = new ImageAdapter(
        new ArrayList<>(),
                                         this::handleImageSelection,
                                         this::showImageOptionsDialog,
                                         this::handleImageLongClick
    );

    unselectedAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        updateSelectAllCheckboxState();
      }
    });

    recyclerViewUnselected.setAdapter(unselectedAdapter);

    // Setup selected images section
    textViewSelected = findViewById(R.id.textViewSelected);
    textViewSelectedNumber = findViewById(R.id.textViewSelectedNumber);
    textViewReorderingDisabled = findViewById(R.id.textViewReorderingDisabled);
    recyclerViewSelected = findViewById(R.id.recyclerViewSelected);
    recyclerViewSelected.setLayoutManager(new LinearLayoutManager(this));
    selectedAdapter = new ImageOrderAdapter(this, selectedImages);
    selectedAdapter.setOnImageUnselectListener(this::handleImageSelection);
    selectedAdapter.setOnReorderListener(() -> {
      hasChanges = true;
      saveImageOrder();
    });
    recyclerViewSelected.setAdapter(selectedAdapter);
    selectedAdapter.attachToRecyclerView(recyclerViewSelected);

    // Setup add image button
    btnAddImage = findViewById(R.id.btnAddImage);
    btnAddImage.setOnClickListener(v -> openImagePicker());

    // Setup random order switch
    switchRandomOrder = findViewById(R.id.switch_random_order);
    switchRandomOrder.setChecked(isRandomOrderEnabled());

    // Style the switch with theme colors
    int primaryColor = getResources().getColor(R.color.primary, getTheme());
    int trackColor = ColorUtils.setAlphaComponent(
        primaryColor,
        128
    ); // Semi-transparent primary color

    // Create color state lists for thumb and track
    ColorStateList thumbColorStateList = new ColorStateList(
        new int[][]{
            new int[]{android.R.attr.state_checked}, new int[]{}
        }, new int[]{
        primaryColor,         // Checked state - primary color
        Color.LTGRAY          // Unchecked state - light gray
    }
    );

    ColorStateList trackColorStateList = new ColorStateList(
        new int[][]{
            new int[]{android.R.attr.state_checked}, new int[]{}
        }, new int[]{
        trackColor,           // Checked state - semi-transparent primary
        Color.GRAY            // Unchecked state - gray
    }
    );

    switchRandomOrder.setThumbTintList(thumbColorStateList);
    switchRandomOrder.setTrackTintList(trackColorStateList);

    // Make sure the reordering disabled text is initially set to correct visibility
    textViewReorderingDisabled.setVisibility(switchRandomOrder.isChecked()
                                             ? View.VISIBLE
                                             : View.GONE);

    switchRandomOrder.setOnCheckedChangeListener((buttonView, isChecked) -> {
      saveRandomOrderPreference(isChecked);

      // Toggle visibility of the reordering disabled text
      textViewReorderingDisabled.setVisibility(isChecked ? View.VISIBLE : View.GONE);

      updateSelectedSectionVisibility();
    });

    // Initially show the selected section
    textViewSelected.setVisibility(View.VISIBLE);
    updateSelectedSectionVisibility();

    loadImages();

    // Initialize multi-select controls
    updateMultiSelectControlsVisibility();
  }

  private void updateSelectAllCheckboxState() {
    if (!isMultiSelectMode) return;

    // For all states, use transparent background with primary color for border and checkmark/minus
    int bgColor = Color.TRANSPARENT;
    int borderColor = getResources().getColor(R.color.primary, getTheme());
    int iconColor = getResources().getColor(R.color.primary, getTheme());

    int checkboxState = getCheckboxState();
    if (checkboxState > 0) {
      CheckboxUtils.styleCustomCheckboxWithState(
          this,
          checkboxSelectAll,
          checkboxState,
          bgColor,
          borderColor,
          iconColor
      );
    } else {
      checkboxSelectAll.setImageResource(R.drawable.empty_square);
      checkboxSelectAll.setColorFilter(getResources().getColor(R.color.primary, getTheme()));
    }
  }

  private int getCheckboxState() {
    int total = unselectedAdapter.getItemCount();
    int selected = unselectedAdapter.getMultiSelectedImages().size();

    // Set the checkbox drawable programmatically based on selection state
    boolean isAllSelected = (selected > 0 && selected == total);
    boolean isIndeterminate = (selected > 0 && selected < total);

    // Determine checkbox state:
    // 0 = unchecked (nothing selected)
    // 1 = checked (all selected)
    // 2 = indeterminate (some selected) - will show minus sign
    int checkboxState = 0;
    if (isAllSelected) checkboxState = 1;
    else if (isIndeterminate) checkboxState = 2;
    return checkboxState;
  }

  private void handleSelectAllCheckboxClick() {
    int total = unselectedAdapter.getItemCount();
    int selected = unselectedAdapter.getMultiSelectedImages().size();

    if (selected < total) {
      // Not all selected, so select all
      selectAllImages();
    } else {
      // All selected, so deselect all
      deselectAllImages();
    }
  }

  private void selectAllImages() {
    List<Image> unselectedList = new ArrayList<>();
    for (int i = 0; i < unselectedAdapter.getItemCount(); i++) {
      Image image = unselectedAdapter.getImageAt(i);
      if (!unselectedAdapter.getMultiSelectedImages().contains(image)) {
        unselectedList.add(image);
      }
    }

    for (Image image : unselectedList) {
      unselectedAdapter.toggleImageSelection(image);
    }
  }

  private void handleImageSelection(Image image) {
    if (selectedImages.contains(image)) {
      selectedImages.remove(image);

      // Save the new order if we're not in random mode
      if (!switchRandomOrder.isChecked()) {
        saveImageOrder();
      }
    } else {
      selectedImages.add(image);

      // Save the new order if we're not in random mode
      if (!switchRandomOrder.isChecked()) {
        saveImageOrder();
      }
    }
    hasChanges = true;

    // Update count immediately for better UX - with parentheses
    textViewSelectedNumber.setText("(" + selectedImages.size() + ")");

    updateSelectedSectionVisibility();
    selectedAdapter.setImages(selectedImages);
    updateUnselectedImages();
  }

  private void showImageOptionsDialog(Image image) {
    // Inflate custom dialog view
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_options, null);

    // Find views
    ImageView imagePreview = dialogView.findViewById(R.id.imagePreview);
    EditText editImageName = dialogView.findViewById(R.id.editImageName);
    Button btnDeleteImage = dialogView.findViewById(R.id.btnDeleteImage);
    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);
    TextInputLayout textInputLayout = (TextInputLayout) editImageName.getParent().getParent();

    // Set current image name
    editImageName.setText(image.name);

    // Initial validation in case name is empty
    if (image.name.trim().isEmpty()) {
      textInputLayout.setError("Name cannot be empty");
      textInputLayout.setErrorEnabled(true);
    }

    // Add text change listener to validate input in real-time
    editImageName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Not used
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Check if text is empty after trimming whitespace
        String text = s.toString().trim();
        if (text.isEmpty()) {
          // Show error immediately when field becomes empty
          textInputLayout.setError("Name cannot be empty");
          textInputLayout.setErrorEnabled(true);
        } else {
          // Clear error when there is valid input
          textInputLayout.setError(null);
          textInputLayout.setErrorEnabled(false);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        // Not used
      }
    });

    // Apply checkboard pattern background for transparency
    int lightColor = Color.rgb(238, 238, 238); // #EEEEEE
    int darkColor = Color.rgb(204, 204, 204);  // #CCCCCC
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 16);
    imagePreview.setBackground(checkerboardDrawable);

    // Load image into preview
    if (image.source.equals("default")) {
      // First try to find resource by name
      int resourceId = getResources().getIdentifier(
          image.name.toLowerCase(),
          "drawable",
          getPackageName()
      );

      // If not found by name, try to use the path which should contain the resource ID
      if (resourceId == 0 && image.path != null && !image.path.isEmpty()) {
        try {
          resourceId = Integer.parseInt(image.path);
        } catch (NumberFormatException e) {
          // Path is not a valid resource ID
          resourceId = 0;
        }
      }

      if (resourceId != 0) {
        // Use a request with centerInside to keep aspect ratio and show checkerboard behind
        // transparent areas
        Glide.with(this).load(resourceId).fitCenter() // Fit within bounds keeping aspect ratio
             .into(imagePreview);
      } else {
        Toast.makeText(this, "Failed to load default image", Toast.LENGTH_SHORT).show();
      }
    } else {
      // Load custom image from URI
      Uri uri = Uri.parse(image.path);
      try {
        getContentResolver().takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
        // Use a request with centerInside to keep aspect ratio and show checkerboard behind
        // transparent areas
        Glide.with(this).load(uri).fitCenter() // Fit within bounds keeping aspect ratio
             .into(imagePreview);
      } catch (Exception e) {
        Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }

    // Hide delete button for default images
    btnDeleteImage.setVisibility(image.source.equals("default") ? View.GONE : View.VISIBLE);

    // Use error color from theme for delete button
    btnDeleteImage.setTextColor(getResources().getColor(R.color.error, getTheme()));

    // Create and show dialog
    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Image Options")
        .setView(dialogView)
        .create();

    // Set up button click listeners
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    btnSave.setOnClickListener(v -> {
      String newName = editImageName.getText().toString().trim();
      if (newName.isEmpty()) {
        // Show error on the TextInputLayout for empty name
        textInputLayout.setError("Name cannot be empty");
        textInputLayout.setErrorEnabled(true);
        return; // Don't proceed
      }

      // Check if name is different from original
      if (!newName.equals(image.name)) {
        // Only rename if the name has actually changed
        renameImage(image, newName);
      }
      dialog.dismiss();
    });

    btnDeleteImage.setOnClickListener(v -> {
      dialog.dismiss();
      deleteImage(image);
    });

    dialog.show();
  }

  private void loadImages() {
    executor.execute(() -> {
      db.imageDao().deleteInvalidCustomImages();

      List<Image> defaultImages = db.imageDao().getImagesBySource("default");
      List<Image> customImages = db.imageDao().getImagesBySource("custom");

      // Store the current selected image IDs and preserve their order
      List<Integer> currentSelectedIdsInOrder = new ArrayList<>();
      for (Image image : selectedImages) {
        currentSelectedIdsInOrder.add(image.id);
      }

      // Also keep a set for faster lookup
      Set<Integer> currentSelectedIds = new HashSet<>(currentSelectedIdsInOrder);

      allImages.clear();
      allImages.addAll(defaultImages);
      allImages.addAll(customImages);

      // Map of all images by ID for faster lookup
      Map<Integer, Image> allImagesById = new HashMap<>();
      for (Image image : allImages) {
        allImagesById.put(image.id, image);
      }

      if (selectedImages.isEmpty()) {
        // No in-memory selection, load from persisted preferences
        Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);
        List<Image> newlySelectedImages = new ArrayList<>();
        for (Image image : allImages) {
          if (selectedImageIds.contains(image.id)) {
            newlySelectedImages.add(image);
          }
        }

        // Get the saved order if not in random mode
        if (!isRandomOrderEnabled() && !newlySelectedImages.isEmpty()) {
          SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
          String json = prefs.getString("image_order", null);
          if (json != null) {
            try {
              Gson gson = new Gson();
              java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Integer>>() {
              }.getType();
              List<Integer> savedOrder = gson.fromJson(json, type);

              if (savedOrder != null && !savedOrder.isEmpty()) {
                // Sort images according to saved order
                List<Image> orderedImages = new ArrayList<>();
                for (Integer id : savedOrder) {
                  for (Image image : newlySelectedImages) {
                    if (image.id == id) {
                      orderedImages.add(image);
                      break;
                    }
                  }
                }

                // Add any new images that weren't in the saved order
                for (Image image : newlySelectedImages) {
                  if (!orderedImages.contains(image)) {
                    orderedImages.add(image);
                  }
                }

                selectedImages.clear();
                selectedImages.addAll(orderedImages);
              } else {
                selectedImages.clear();
                selectedImages.addAll(newlySelectedImages);
              }
            } catch (Exception e) {
              // If there's an error parsing the JSON, just use the unordered list
              selectedImages.clear();
              selectedImages.addAll(newlySelectedImages);
            }
          } else {
            selectedImages.clear();
            selectedImages.addAll(newlySelectedImages);
          }
        } else {
          selectedImages.clear();
          selectedImages.addAll(newlySelectedImages);
        }
      } else {
        // We already have selections in memory, preserve their order but filter out deleted images
        List<Image> orderedSelectedImages = new ArrayList<>();

        // First keep the existing order by iterating through the original ordered list
        for (Integer imageId : currentSelectedIdsInOrder) {
          Image image = allImagesById.get(imageId);
          if (image != null) {
            // This image still exists in the database
            orderedSelectedImages.add(image);
          }
        }

        selectedImages.clear();
        selectedImages.addAll(orderedSelectedImages);
      }

      runOnUiThread(() -> {
        // Update count immediately after loading - with parentheses
        textViewSelectedNumber.setText("(" + selectedImages.size() + ")");

        updateUnselectedImages();
        selectedAdapter.notifyDataSetChanged();
        updateSelectedSectionVisibility();
      });
    });
  }

  private void showRemoveConfirmationDialog() {
    Set<Image> selectedForRemoval = unselectedAdapter.getMultiSelectedImages();
    if (selectedForRemoval.isEmpty()) {
      Toast.makeText(this, "No images selected for removal", Toast.LENGTH_SHORT).show();
      return;
    }

    boolean hasDefaultImages = selectedForRemoval
        .stream()
        .anyMatch(image -> image.source.equals("default"));

    AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle("Remove Images")
        .setNegativeButton("Cancel", null);

    if (hasDefaultImages) {
      builder
          .setMessage(
              "Only non-default images can be removed. If you proceed, only custom images will be"
              + " deleted. Are you sure you want to continue?")
          .setPositiveButton("Remove Custom Only", (dialog, which) -> removeSelectedImages());
    } else {
      builder
          .setMessage("Are you sure you want to remove the selected images?")
          .setPositiveButton("Remove", (dialog, which) -> removeSelectedImages());
    }

    builder.show();
  }

  private void removeSelectedImages() {
    Set<Image> selectedForRemoval = unselectedAdapter.getMultiSelectedImages();
    List<Image> customImagesToRemove = selectedForRemoval
        .stream()
        .filter(image -> !image.source.equals("default"))
        .collect(Collectors.toList());

    if (!customImagesToRemove.isEmpty()) {
      executor.execute(() -> {
        try {
          for (Image image : customImagesToRemove) {
            db.imageDao().deleteImage(image);
          }

          runOnUiThread(() -> {
            loadImages();
            Toast.makeText(this, "Selected images deleted", Toast.LENGTH_SHORT).show();
            exitMultiSelectMode();
          });
        } catch (Exception e) {
          runOnUiThread(() -> Toast
              .makeText(this, "Failed to delete images: " + e.getMessage(), Toast.LENGTH_LONG)
              .show());
        }
      });
    } else if (selectedForRemoval.size() > 0) {
      // Only default images were selected
      Toast.makeText(this, "Default images cannot be removed", Toast.LENGTH_SHORT).show();
      exitMultiSelectMode();
    }
  }

  private boolean isRandomOrderEnabled() {
    return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RANDOM_ORDER, false);
  }

  @Override
  public boolean onSupportNavigateUp() {
    if (isMultiSelectMode) {
      exitMultiSelectMode();
      return true;
    } else if (hasChanges) {
      showExitConfirmationDialog();
      return true;
    }
    finish();
    return true;
  }

  private void showExitConfirmationDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Unsaved Changes")
        .setMessage("You have unsaved changes. What would you like to do?")
        .setPositiveButton("Save and Exit", (dialog, which) -> saveAndExit())
        .setNegativeButton("Exit Without Saving", (dialog, which) -> finish())
        .setNeutralButton("Save", (dialog, which) -> saveSelectedImages())
        .show();
  }

  private void saveAndExit() {
    Set<Integer> selectedImageIds = new HashSet<>();
    // Also save the order of images for non-random mode
    List<Integer> orderedImageIds = new ArrayList<>();

    for (Image image : selectedImages) {
      selectedImageIds.add(image.id);
      orderedImageIds.add(image.id);
    }

    // Save which images are selected
    SelectedImagesManager.saveSelectedImages(this, selectedImageIds);

    // Save the order of selected images if not in random mode
    if (!switchRandomOrder.isChecked()) {
      // Save the order using Gson
      SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
      Gson gson = new Gson();
      String json = gson.toJson(orderedImageIds);
      prefs.edit().putString("image_order", json).apply();
    }

    // Save the random order preference
    saveRandomOrderPreference(switchRandomOrder.isChecked());

    Intent resultIntent = new Intent();
    resultIntent.putExtra(
        "selected_image_ids",
        selectedImageIds.stream().mapToInt(Integer::intValue).toArray()
    );
    resultIntent.putExtra("random_order", switchRandomOrder.isChecked());
    setResult(RESULT_OK, resultIntent);
    finish();
  }

  private void saveRandomOrderPreference(boolean isRandom) {
    getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_RANDOM_ORDER, isRandom)
        .apply();
  }

  // Add a new method to save selected images without exiting
  private void saveSelectedImages() {
    Set<Integer> selectedImageIds = new HashSet<>();
    // Also save the order of images for non-random mode
    List<Integer> orderedImageIds = new ArrayList<>();

    for (Image image : selectedImages) {
      selectedImageIds.add(image.id);
      orderedImageIds.add(image.id);
    }

    // Save which images are selected
    SelectedImagesManager.saveSelectedImages(this, selectedImageIds);

    // Save the order of selected images if not in random mode
    if (!switchRandomOrder.isChecked()) {
      // Save the order using Gson
      SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
      Gson gson = new Gson();
      String json = gson.toJson(orderedImageIds);
      prefs.edit().putString("image_order", json).apply();
    }

    // Save the random order preference
    saveRandomOrderPreference(switchRandomOrder.isChecked());

    // Reset the changes flag
    hasChanges = false;

    // Show confirmation toast
    Toast.makeText(this, "Selection saved", Toast.LENGTH_SHORT).show();
  }

  private void exitMultiSelectMode() {
    isMultiSelectMode = false;
    unselectedAdapter.setMultiSelectMode(false);
    updateMultiSelectControlsVisibility();
  }

  private void updateMultiSelectControlsVisibility() {
    if (isMultiSelectMode) {
      multiSelectControls.setVisibility(View.VISIBLE);
      btnAddImage.setVisibility(View.GONE);
    } else {
      multiSelectControls.setVisibility(View.GONE);
      btnAddImage.setVisibility(View.VISIBLE);
    }
  }

  private void addSelectedImagesToSelection() {
    Set<Image> multiSelectedImages = unselectedAdapter.getMultiSelectedImages();
    if (!multiSelectedImages.isEmpty()) {
      hasChanges = true;
      selectedImages.addAll(multiSelectedImages);

      // Update count immediately - with parentheses
      textViewSelectedNumber.setText("(" + selectedImages.size() + ")");

      // Save the order immediately if we're not in random mode
      if (!switchRandomOrder.isChecked()) {
        saveImageOrder();
      }

      updateUnselectedImages();
      selectedAdapter.notifyDataSetChanged();
      updateSelectedSectionVisibility();
      exitMultiSelectMode();
    } else {
      Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
    }
  }

  private void updateSelectedSectionVisibility() {
    boolean hasSelectedImages = !selectedImages.isEmpty();
    boolean isRandomOrder = switchRandomOrder.isChecked();
    int selectedCount = selectedImages.size();

    textViewSelected.setVisibility(View.VISIBLE);
    recyclerViewSelected.setVisibility(hasSelectedImages ? View.VISIBLE : View.GONE);

    // Ensure the reordering disabled text has the right visibility
    textViewReorderingDisabled.setVisibility(isRandomOrder ? View.VISIBLE : View.GONE);

    // Update the selected count text - always in parentheses
    if (hasSelectedImages) {
      textViewSelectedNumber.setText("(" + selectedCount + ")");
      textViewSelectedNumber.setVisibility(View.VISIBLE);
    } else {
      textViewSelectedNumber.setText("(0)");
      textViewSelectedNumber.setVisibility(View.VISIBLE);
    }

    // Update adapter's drag-enabled state based on random order setting
    selectedAdapter.setDragEnabled(!isRandomOrder);

    // Apply visual indication of random order state
    if (hasSelectedImages) {
      if (isRandomOrder) {
        // Apply disabled appearance when random order is enabled
        recyclerViewSelected.setAlpha(0.7f); // Slightly transparent
        textViewSelected.setText("Selected Images");
      } else {
        // Normal appearance when manual ordering is enabled
        recyclerViewSelected.setAlpha(1.0f);
        textViewSelected.setText("Selected Images");

        // Force a layout pass to ensure visual changes are applied
        recyclerViewSelected.post(() -> {
          if (recyclerViewSelected.getAdapter() != null) {
            recyclerViewSelected.getAdapter().notifyDataSetChanged();
          }
        });
      }
    } else {
      textViewSelected.setText("Selected Images");
    }
  }

  private void saveImageOrder() {
    // Only save the order if not in random mode
    if (!switchRandomOrder.isChecked()) {
      List<Integer> orderedImageIds = new ArrayList<>();
      for (Image image : selectedImages) {
        orderedImageIds.add(image.id);
      }

      // Save the order using SharedPreferences
      SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
      Gson gson = new Gson();
      String json = gson.toJson(orderedImageIds);
      prefs.edit().putString("image_order", json).apply();
    }
  }

  private void renameImage(Image image, String newName) {
    executor.execute(() -> {
      try {
        image.name = newName;
        db.imageDao().updateImage(image);
        runOnUiThread(() -> {
          loadImages();
          Toast.makeText(this, "Image renamed successfully", Toast.LENGTH_SHORT).show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast
            .makeText(this, "Failed to rename image: " + e.getMessage(), Toast.LENGTH_LONG)
            .show());
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_image_selection, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home && isMultiSelectMode) {
      exitMultiSelectMode();
      return true;
    } else if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      if (selectedImages.isEmpty()) {
        Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
        return true;
      }
      saveSelectedImages();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (isMultiSelectMode) {
      exitMultiSelectMode();
    } else if (hasChanges) {
      showExitConfirmationDialog();
    } else {
      super.onBackPressed();
    }
  }

  private void openImagePicker() {
    pickImageLauncher.launch("image/*");
  }

  private void handleNewImageSelection(Uri imageUri) {
    String fileName = "Custom_" + System.currentTimeMillis();
    Image newImage = new Image(fileName, "custom", imageUri.toString());

    executor.execute(() -> {
      try {
        long id = db.imageDao().insertImage(newImage);
        newImage.id = (int) id;
        runOnUiThread(() -> {
          allImages.add(newImage);
          updateUnselectedImages();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast
            .makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_LONG)
            .show());
      }
    });
  }

  private void updateUnselectedImages() {
    List<Image> unselected = new ArrayList<>(allImages);
    unselected.removeAll(selectedImages);
    unselectedAdapter.updateImages(unselected);
  }

  private void deleteImage(Image image) {
    executor.execute(() -> {
      try {
        db.imageDao().deleteImage(image);
        runOnUiThread(() -> {
          loadImages();
          Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast
            .makeText(this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_LONG)
            .show());
      }
    });
  }

  private boolean handleImageLongClick(Image image) {
    if (!isMultiSelectMode) {
      // Start multi-select mode
      isMultiSelectMode = true;
      unselectedAdapter.setMultiSelectMode(true);
      unselectedAdapter.toggleImageSelection(image);
      updateMultiSelectControlsVisibility();
      updateSelectAllCheckboxState();
      return true;
    }
    return false;
  }

  private void deselectAllImages() {
    unselectedAdapter.clearSelections();
    updateSelectAllCheckboxState();
  }

  /**
   * Calculate number of columns for the grid based on screen width
   */
  private int calculateSpanCount() {
    // Get the screen width
    int screenWidth = getResources().getDisplayMetrics().widthPixels;
    // Target width for each item in dp
    int idealItemWidth = 100; // dp
    // Convert dp to pixels
    float density = getResources().getDisplayMetrics().density;
    int itemWidthInPixels = (int) (idealItemWidth * density);
    // Calculate span count
    int spanCount = Math.max(2, screenWidth / itemWidthInPixels);
    return Math.min(spanCount, 4); // Cap at 4 columns
  }

  /**
   * ItemDecoration to add spacing between grid items
   */
  public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int spacing;
    private final boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
      this.spanCount = spanCount;
      this.spacing = spacing;
      this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(
        android.graphics.Rect outRect,
        View view,
        RecyclerView parent,
        RecyclerView.State state
    ) {
      int position = parent.getChildAdapterPosition(view);
      int column = position % spanCount;

      if (includeEdge) {
        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;

        if (position < spanCount) { // top edge
          outRect.top = spacing;
        }
        outRect.bottom = spacing; // item bottom
      } else {
        outRect.left = column * spacing / spanCount;
        outRect.right = spacing - (column + 1) * spacing / spanCount;
        if (position >= spanCount) {
          outRect.top = spacing; // item top
        }
      }
    }
  }
} 

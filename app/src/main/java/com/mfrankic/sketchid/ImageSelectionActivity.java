package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.ERROR_NAME_EMPTY;
import static com.mfrankic.sketchid.Constants.PREF_IMAGE_ORDER;
import static com.mfrankic.sketchid.Constants.SOURCE_CUSTOM;
import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ImageSelectionActivity extends BaseActivity {
  private static final String PREF_NAME = PREF_IMAGE_ORDER;

  private final List<Image> allImages = new ArrayList<>();
  private List<Image> selectedImages = new ArrayList<>();
  private RecyclerView recyclerViewSelected;
  private TextView textViewSelected;
  private TextView textViewSelectedNumber;
  private ImageAdapter unselectedAdapter;
  private ImageOrderAdapter selectedAdapter;
  private AppDatabase db;
  private Executor executor;
  private final ActivityResultLauncher<String> pickImageLauncher
      = registerForActivityResult(
      new ActivityResultContracts.GetMultipleContents(),
      this::handleMultipleImageSelection
  );
  // Keep track of initial state to restore if user cancels
  private List<Image> initialSelectedImages = new ArrayList<>();
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
      getSupportActionBar().setTitle("Select Images");
    }

    db = AppDatabase.getInstance(this);
    executor = Executors.newSingleThreadExecutor();

    // Setup back press handling
    setupBackPressHandler();

    // Multi-select mode controls
    multiSelectControls = findViewById(R.id.multiSelectControls);
    Button btnAddToSelection = findViewById(R.id.btnAddToSelection);
    Button btnRemoveSelected = findViewById(R.id.btnRemoveSelected);
    Button btnExitMultiSelect = findViewById(R.id.btnExitMultiSelect);
    checkboxSelectAll = findViewById(R.id.checkboxSelectAll);

    checkboxSelectAll.setOnClickListener(v -> handleSelectAllCheckboxClick());
    btnAddToSelection.setOnClickListener(v -> addSelectedImagesToSelection());
    btnRemoveSelected.setOnClickListener(v -> showRemoveConfirmationDialog());
    btnExitMultiSelect.setOnClickListener(v -> exitMultiSelectMode());

    // Setup unselected images section
    RecyclerView recyclerViewUnselected = findViewById(R.id.recyclerViewUnselected);
    int spanCount = calculateSpanCount();
    GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
    recyclerViewUnselected.setLayoutManager(layoutManager);
    recyclerViewUnselected.addItemDecoration(new StableGridSpacingDecoration(getResources().getDimensionPixelSize(
        R.dimen.grid_spacing) / 2));

    // Setup the adapter for unselected images
    setupUnselectedAdapter();

    // Setup selected images section
    textViewSelected = findViewById(R.id.textViewSelected);
    textViewSelectedNumber = findViewById(R.id.textViewSelectedNumber);
    recyclerViewSelected = findViewById(R.id.recyclerViewSelected);
    recyclerViewSelected.setLayoutManager(new LinearLayoutManager(this));
    selectedAdapter = new ImageOrderAdapter(this, selectedImages);
    selectedAdapter.setOnImageUnselectListener(this::handleImageSelection);
    selectedAdapter.setOnReorderListener(this::saveImageOrder);
    recyclerViewSelected.setAdapter(selectedAdapter);
    selectedAdapter.attachToRecyclerView(recyclerViewSelected);

    // Setup add image button
    btnAddImage = findViewById(R.id.btnAddImage);
    btnAddImage.setOnClickListener(v -> openImagePicker());

    // Load images from database
    loadImages();
  }

  /**
   * Setup the back press handler using the modern OnBackPressedCallback approach
   */
  private void setupBackPressHandler() {
    getOnBackPressedDispatcher().addCallback(
        this, new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            if (isMultiSelectMode) {
              exitMultiSelectMode();
            } else if (hasChanges) {
              showExitConfirmationDialog();
            } else {
              // Disable this callback to allow the system to handle the back press
              this.setEnabled(false);
              getOnBackPressedDispatcher().onBackPressed();
              // Re-enable the callback for future back presses
              this.setEnabled(true);
            }
          }
        }
    );
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

    // Update checkbox state after changing selections
    updateSelectAllCheckboxState();
  }

  private void handleImageSelection(Image image) {
    // Step 1: Process the selection
    processImageSelectionToggle(image);

    // Step 2: Update unselected list
    updateUnselectedImages();

    // Step 3: Track order changes but don't save to preferences yet
    saveImageOrder();

    // Step 4: Update UI
    updateUIAfterImageSelection();
  }

  private void processImageSelectionToggle(Image image) {
    // Step 1: Get currently selected images from the adapter
    List<Image> currentlySelected = getCurrentlySelectedImages();

    // Step 2: Create a completely new list and toggle the selection
    boolean wasSelected = isImageAlreadySelected(image, currentlySelected);
    List<Image> newSelectedList = toggleImageInSelection(image, currentlySelected, wasSelected);

    // Step 3: Officially update the selectedImages variable
    selectedImages = newSelectedList;

    // Step 4: Create a verified adapter list and update the adapter
    updateSelectionAdapter(newSelectedList);
  }

  private List<Image> getCurrentlySelectedImages() {
    List<Image> currentlySelected = new ArrayList<>();
    if (selectedAdapter != null && selectedAdapter.getImages() != null) {
      currentlySelected.addAll(selectedAdapter.getImages());
    }
    return currentlySelected;
  }

  private boolean isImageAlreadySelected(Image image, List<Image> currentlySelected) {
    for (Image selected : currentlySelected) {
      if (selected.id == image.id) {
        return true;
      }
    }
    return false;
  }

  private List<Image> toggleImageInSelection(
      Image image,
      List<Image> currentlySelected,
      boolean wasSelected
  ) {
    List<Image> newSelectedList = new ArrayList<>();

    if (wasSelected) {
      // Remove from selection (add all except the clicked image)
      for (Image selected : currentlySelected) {
        if (selected.id != image.id) {
          newSelectedList.add(selected);
        }
      }
    } else {
      // Add to selection (add all current selections first, then the new one)
      newSelectedList.addAll(currentlySelected);
      newSelectedList.add(image);
    }

    return newSelectedList;
  }

  private void updateSelectionAdapter(List<Image> newSelectedList) {
    List<Image> adapterImages = new ArrayList<>();
    if (!newSelectedList.isEmpty()) {
      for (int i = 0; i < newSelectedList.size(); i++) {
        Image img = newSelectedList.get(i);
        if (img != null) {
          adapterImages.add(img);
        }
      }
    }

    // Update the adapter with verified list
    selectedAdapter.setImages(adapterImages);
  }

  private void updateUIAfterImageSelection() {
    // Update count immediately for better UX - with parentheses
    textViewSelectedNumber.setText(String.format(
        Locale.getDefault(),
        getString(R.string.selected_count_format),
        selectedImages.size()
    ));

    updateSelectedSectionVisibility();
  }

  private void showImageOptionsDialog(Image image) {
    // Create a proper parent ViewGroup for layout parameters to work correctly
    FrameLayout parentContainer = new FrameLayout(this);
    parentContainer.setLayoutParams(new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                                                 ViewGroup.LayoutParams.WRAP_CONTENT
    ));

    // Inflate the view with proper parent
    View dialogView = LayoutInflater
        .from(this)
        .inflate(R.layout.dialog_image_options, parentContainer, false);

    // Setup dialog components
    DialogComponents components = setupDialogComponents(dialogView, image);

    // Load image into preview
    loadImagePreview(image, components.imagePreview);

    // Create and show dialog
    AlertDialog dialog = createImageOptionsDialog(dialogView);

    // Setup button click listeners
    setupDialogButtonListeners(dialog, image, components);

    dialog.show();
  }

  private DialogComponents setupDialogComponents(View dialogView, Image image) {
    // Find views
    ImageView imagePreview = dialogView.findViewById(R.id.image_preview);
    EditText editImageName = dialogView.findViewById(R.id.editImageName);
    Button btnDeleteImage = dialogView.findViewById(R.id.btnDeleteImage);
    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);
    TextInputLayout textInputLayout = (TextInputLayout) editImageName.getParent().getParent();

    // Set current image name
    editImageName.setText(image.name);

    // Initial validation check
    validateImageName(image.name, textInputLayout);

    // Add text change listener to validate input in real-time
    setupImageNameValidator(editImageName, textInputLayout);

    // Apply checkerboard pattern background for transparency
    setupCheckerboardBackground(imagePreview);

    // Hide delete button for default images
    btnDeleteImage.setVisibility(image.source.equals(SOURCE_DEFAULT) ? View.GONE : View.VISIBLE);

    // Use error color from theme for delete button
    btnDeleteImage.setTextColor(getResources().getColor(R.color.error, getTheme()));

    return new DialogComponents(
        imagePreview,
                                editImageName,
                                btnDeleteImage,
                                btnCancel,
                                btnSave,
                                textInputLayout
    );
  }

  private void validateImageName(String name, TextInputLayout textInputLayout) {
    if (name.trim().isEmpty()) {
      textInputLayout.setError(ERROR_NAME_EMPTY);
      textInputLayout.setErrorEnabled(true);
    }
  }

  private void setupCheckerboardBackground(ImageView imagePreview) {
    int lightColor = Color.rgb(238, 238, 238); // #EEEEEE
    int darkColor = Color.rgb(204, 204, 204);  // #CCCCCC
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 16);
    imagePreview.setBackground(checkerboardDrawable);
  }

  private void setupImageNameValidator(EditText editImageName, TextInputLayout textInputLayout) {
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
          textInputLayout.setError(ERROR_NAME_EMPTY);
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
  }

  private AlertDialog createImageOptionsDialog(View dialogView) {
    return new AlertDialog.Builder(this)
        .setTitle(Constants.DIALOG_TITLE_IMAGE_OPTIONS)
        .setView(dialogView)
        .create();
  }

  private void setupDialogButtonListeners(
      AlertDialog dialog,
      Image image,
      DialogComponents components
  ) {
    // Cancel button listener
    components.btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Save button listener
    components.btnSave.setOnClickListener(v -> handleSaveButtonClick(dialog, image, components));

    // Delete button listener
    components.btnDeleteImage.setOnClickListener(v -> {
      dialog.dismiss();
      deleteImage(image);
    });
  }

  private void handleSaveButtonClick(AlertDialog dialog, Image image, DialogComponents components) {
    String newName = components.editImageName.getText().toString().trim();
    if (newName.isEmpty()) {
      // Show error on the TextInputLayout for empty name
      components.textInputLayout.setError(ERROR_NAME_EMPTY);
      components.textInputLayout.setErrorEnabled(true);
      return; // Don't proceed
    }

    // Check if name is different from original
    if (!newName.equals(image.name)) {
      // Only rename if the name has actually changed
      renameImage(image, newName);
    }
    dialog.dismiss();
  }

  private void loadImagePreview(Image image, ImageView imagePreview) {
    if (image.source.equals(SOURCE_DEFAULT)) {
      loadDefaultImage(image, imagePreview);
    } else {
      loadCustomImage(image, imagePreview);
    }
  }

  private void loadDefaultImage(Image image, ImageView imagePreview) {
    // Use ResourceUtils with non-deprecated method
    int resourceId = ResourceUtils.getDrawableResourceByName(image.name);

    // If not found by name, try to use the path which should contain the resource ID
    if (resourceId == 0 && !TextUtils.isEmpty(image.path)) {
      try {
        resourceId = Integer.parseInt(image.path);
      } catch (NumberFormatException e) {
        // Path is not a valid resource ID
      }
    }

    if (resourceId != 0) {
      // Use a request with centerInside to keep aspect ratio and show checkerboard behind
      // transparent areas
      Glide.with(this).load(resourceId).fitCenter() // Fit within bounds keeping aspect ratio
           .into(imagePreview);
    } else {
      Toast.makeText(this, Constants.TOAST_FAILED_LOAD_DEFAULT, Toast.LENGTH_SHORT).show();
    }
  }

  private void loadCustomImage(Image image, ImageView imagePreview) {
    // Load custom image from URI
    Uri uri = Uri.parse(image.path);
    try {
      getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      // Use a request with centerInside to keep aspect ratio and show checkerboard behind
      // transparent areas
      Glide.with(this).load(uri).fitCenter() // Fit within bounds keeping aspect ratio
           .into(imagePreview);
    } catch (Exception e) {
      Toast
          .makeText(this, Constants.TOAST_FAILED_LOAD_IMAGE + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void loadImages() {
    executor.execute(() -> {
      db.imageDao().deleteInvalidCustomImages();

      // Load images and apply changes on the UI thread
      List<Image> fetchedImages = fetchAllImages();
      processLoadedImages(fetchedImages);
    });
  }

  private List<Image> fetchAllImages() {
    // First fetch all images from database
    List<Image> fetchedImages = new ArrayList<>();
    fetchedImages.addAll(db.imageDao().getImagesBySource(SOURCE_DEFAULT));
    fetchedImages.addAll(db.imageDao().getImagesBySource(SOURCE_CUSTOM));
    return fetchedImages;
  }

  private void processLoadedImages(List<Image> fetchedImages) {
    // Update allImages list
    allImages.clear();
    allImages.addAll(fetchedImages);

    // Handle selected images
    processSelectedImages();

    // Save initial state for possible restoration if user cancels
    initialSelectedImages = new ArrayList<>(selectedImages);

    updateUIAfterLoading();
  }

  private void processSelectedImages() {
    // Load selected images from preferences if empty
    if (selectedImages.isEmpty()) {
      loadSelectionsFromPreferences();
    } else {
      filterSelectedImagesAgainstDatabase();
    }
  }

  private void filterSelectedImagesAgainstDatabase() {
    // Filter out deleted images from selected images
    List<Image> validSelectedImages = new ArrayList<>();

    for (Image selectedImage : selectedImages) {
      boolean imageStillExists = isImageInDatabase(selectedImage.id);
      if (imageStillExists) {
        // Find and use the fresh copy from database
        for (Image image : allImages) {
          if (image.id == selectedImage.id) {
            validSelectedImages.add(image);
            break;
          }
        }
      } else {
        // Image was deleted, skip it
        hasChanges = true;
      }
    }

    selectedImages.clear();
    selectedImages.addAll(validSelectedImages);
  }

  private boolean isImageInDatabase(int imageId) {
    for (Image image : allImages) {
      if (image.id == imageId) {
        return true;
      }
    }
    return false;
  }

  private void updateUIAfterLoading() {
    runOnUiThread(() -> {
      // Update count immediately after loading - with parentheses
      textViewSelectedNumber.setText(String.format(
          Locale.getDefault(),
          getString(R.string.selected_count_format),
          selectedImages.size()
      ));

      // Make sure to use a new copy of the list for the adapter
      selectedAdapter.setImages(new ArrayList<>(selectedImages));

      updateUnselectedImages();
      updateSelectedSectionVisibility();
      // Initial update of multi-select controls visibility
      updateMultiSelectControlsVisibility();
    });
  }

  private void loadSelectionsFromPreferences() {
    // Load selected image IDs from preferences
    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);

    // Also load the saved image order
    List<Integer> savedOrderIds = loadImageOrderFromPreferences();

    if (!savedOrderIds.isEmpty()) {
      loadImagesInSavedOrder(selectedImageIds, savedOrderIds);
    } else {
      loadImagesWithoutOrder(selectedImageIds);
    }

    // Save initial state for possible restoration if user cancels
    initialSelectedImages = new ArrayList<>(selectedImages);
  }

  private void loadImagesInSavedOrder(Set<Integer> selectedImageIds, List<Integer> savedOrderIds) {
    // First add images in the saved order
    addImagesFromSavedOrder(selectedImageIds, savedOrderIds);

    // Then add any selected images not in the saved order
    addRemainingSelectedImages(selectedImageIds, savedOrderIds);
  }

  private void addImagesFromSavedOrder(Set<Integer> selectedImageIds, List<Integer> savedOrderIds) {
    for (Integer id : savedOrderIds) {
      if (selectedImageIds.contains(id)) {
        addImageWithIdToSelected(id);
      }
    }
  }

  private void addRemainingSelectedImages(
      Set<Integer> selectedImageIds,
      List<Integer> savedOrderIds
  ) {
    for (Integer id : selectedImageIds) {
      if (!savedOrderIds.contains(id)) {
        addImageWithIdToSelected(id);
      }
    }
  }

  private void loadImagesWithoutOrder(Set<Integer> selectedImageIds) {
    // No saved order, just add all selected images
    for (Image image : allImages) {
      if (selectedImageIds.contains(image.id)) {
        selectedImages.add(image);
      }
    }
  }

  private void addImageWithIdToSelected(int id) {
    for (Image image : allImages) {
      if (image.id == id) {
        selectedImages.add(image);
        break;
      }
    }
  }

  private List<Integer> loadImageOrderFromPreferences() {
    List<Integer> result = new ArrayList<>();
    SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    String json = prefs.getString(PREF_IMAGE_ORDER, null);

    if (json != null) {
      try {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Integer>>() {
        }.getType();
        List<Integer> loadedIds = gson.fromJson(json, type);
        if (loadedIds != null) {
          result.addAll(loadedIds);
        }
      } catch (Exception e) {
        // Error parsing JSON, return empty list
      }
    }

    return result;
  }

  private void showRemoveConfirmationDialog() {
    Set<Image> selectedForRemoval = unselectedAdapter.getMultiSelectedImages();
    if (selectedForRemoval.isEmpty()) {
      Toast.makeText(this, Constants.TOAST_NO_IMAGES_SELECTED, Toast.LENGTH_SHORT).show();
      return;
    }

    boolean hasDefaultImages = selectedForRemoval
        .stream()
        .anyMatch(image -> image.source.equals(SOURCE_DEFAULT));

    AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle(Constants.DIALOG_TITLE_REMOVE_IMAGES)
        .setNegativeButton(Constants.CANCEL_BUTTON, null);

    if (hasDefaultImages) {
      builder
          .setMessage(Constants.DIALOG_MSG_DEFAULT_IMAGES)
          .setPositiveButton(
              Constants.REMOVE_CUSTOM_ONLY_BUTTON,
              (dialog, which) -> removeSelectedImages()
          );
    } else {
      builder
          .setMessage(Constants.DIALOG_MSG_REMOVE_IMAGES)
          .setPositiveButton(Constants.REMOVE_BUTTON, (dialog, which) -> removeSelectedImages());
    }

    builder.show();
  }

  private void removeSelectedImages() {
    Set<Image> selectedForRemoval = unselectedAdapter.getMultiSelectedImages();
    List<Image> customImagesToRemove = selectedForRemoval
        .stream()
        .filter(image -> !image.source.equals(SOURCE_DEFAULT))
        .collect(Collectors.toList());

    if (!customImagesToRemove.isEmpty()) {
      executor.execute(() -> {
        try {
          for (Image image : customImagesToRemove) {
            db.imageDao().deleteImage(image);
          }

          runOnUiThread(() -> {
            loadImages();
            Toast.makeText(this, Constants.TOAST_IMAGES_DELETED, Toast.LENGTH_SHORT).show();
            exitMultiSelectMode();
          });
        } catch (Exception e) {
          runOnUiThread(() -> Toast
              .makeText(this, Constants.TOAST_FAILED_DELETE + e.getMessage(), Toast.LENGTH_LONG)
              .show());
        }
      });
    } else if (!selectedForRemoval.isEmpty()) {
      // Only default images were selected
      Toast.makeText(this, Constants.TOAST_DEFAULT_NOT_REMOVED, Toast.LENGTH_SHORT).show();
      exitMultiSelectMode();
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    if (isMultiSelectMode) {
      exitMultiSelectMode();
      return false;
    }

    if (hasChanges) {
      showExitConfirmationDialog();
      return false;
    }

    finish();
    return true;
  }

  private void showExitConfirmationDialog() {
    new AlertDialog.Builder(this)
        .setTitle(Constants.DIALOG_TITLE_UNSAVED_CHANGES)
        .setMessage(Constants.DIALOG_MSG_UNSAVED_CHANGES)
        .setPositiveButton(Constants.SAVE_AND_EXIT_BUTTON, (dialog, which) -> saveAndExit())
        .setNegativeButton(
            Constants.EXIT_WITHOUT_SAVING_BUTTON, (dialog, which) -> {
              // Restore the original state before exiting
              selectedImages = new ArrayList<>(initialSelectedImages);
              hasChanges = false;
              // Important: Don't call any methods that might save to preferences here
              finish();
            }
        )
        .setNeutralButton(Constants.SAVE_BUTTON, (dialog, which) -> saveSelectedImages())
        .show();
  }

  private void saveAndExit() {
    // Call our method that now correctly syncs and saves
    saveSelectedImages();

    // Create result intent with selected image IDs
    Intent resultIntent = new Intent();
    resultIntent.putExtra(
        "selected_image_ids",
        selectedImages.stream().mapToInt(image -> image.id).toArray()
    );
    setResult(RESULT_OK, resultIntent);
    finish();
  }

  private void saveImageOrder() {
    // Get the current ordered list from the adapter
    List<Image> currentOrderedImages = selectedAdapter.getImages();
    if (!currentOrderedImages.isEmpty()) {
      // Create a completely new list to avoid reference issues
      selectedImages = new ArrayList<>();
      selectedImages.addAll(currentOrderedImages);
    }

    // Mark as changed but DON'T save to preferences
    hasChanges = true;
  }

  private void addSelectedImagesToSelection() {
    // Get the IDs of all selected images
    List<Integer> selectedImageIds = unselectedAdapter.getSelectedImageIds();

    if (selectedImageIds.isEmpty()) {
      Toast.makeText(this, Constants.TOAST_NO_IMAGES_SELECTED_ADD, Toast.LENGTH_SHORT).show();
      return;
    }

    hasChanges = true;

    // Process the selected images
    List<Image> currentlySelected = getCurrentlySelectedImages();
    List<Image> imagesToAdd = findImagesToAdd(selectedImageIds, currentlySelected);
    List<Image> newCombinedList = combineImageLists(currentlySelected, imagesToAdd);

    // Update the adapter and UI
    updateSelectionWithNewList(newCombinedList);
    updateUiAfterSelectionChange();

    // Exit multi-select mode
    exitMultiSelectMode();
  }

  private List<Image> findImagesToAdd(
      List<Integer> selectedImageIds,
      List<Image> currentlySelected
  ) {
    List<Image> imagesToAdd = new ArrayList<>();

    for (Image image : allImages) {
      if (selectedImageIds.contains(image.id) && !isImageAlreadySelected(
          image,
          currentlySelected
      )) {
        imagesToAdd.add(image);
      }
    }

    return imagesToAdd;
  }

  private List<Image> combineImageLists(List<Image> currentlySelected, List<Image> imagesToAdd) {
    List<Image> combinedList = new ArrayList<>(currentlySelected);
    combinedList.addAll(imagesToAdd);
    return combinedList;
  }

  private void updateSelectionWithNewList(List<Image> newSelectedList) {
    // Update the main selection list
    selectedImages = newSelectedList;

    // Create a verified adapter list
    List<Image> adapterImages = new ArrayList<>();
    for (Image img : newSelectedList) {
      if (img != null) {
        adapterImages.add(img);
      }
    }

    // Update the adapter
    selectedAdapter.setImages(adapterImages);
  }

  private void updateUiAfterSelectionChange() {
    // Update count display
    textViewSelectedNumber.setText(String.format(
        Locale.getDefault(),
        getString(R.string.selected_count_format),
        selectedImages.size()
    ));

    // Save the order
    saveImageOrder();

    // Update the unselected images list
    updateUnselectedImages();

    // Update UI visibility
    updateSelectedSectionVisibility();
  }

  private void updateSelectedSectionVisibility() {
    boolean hasSelectedImages = !selectedImages.isEmpty();

    textViewSelected.setVisibility(View.VISIBLE);
    recyclerViewSelected.setVisibility(hasSelectedImages ? View.VISIBLE : View.GONE);

    // Update the selected count text - always in parentheses
    if (hasSelectedImages) {
      textViewSelectedNumber.setText(String.format(
          Locale.getDefault(),
          getString(R.string.selected_count_format),
          selectedImages.size()
      ));
      textViewSelectedNumber.setVisibility(View.VISIBLE);
    } else {
      textViewSelectedNumber.setText(String.format(
          Locale.getDefault(),
          getString(R.string.selected_count_format),
          0
      ));
      textViewSelectedNumber.setVisibility(View.VISIBLE);
    }
  }

  private void renameImage(Image image, String newName) {
    executor.execute(() -> {
      try {
        image.name = newName;
        db.imageDao().updateImage(image);
        runOnUiThread(() -> {
          loadImages();
          Toast.makeText(this, Constants.TOAST_IMAGE_RENAMED, Toast.LENGTH_SHORT).show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast
            .makeText(this, Constants.TOAST_FAILED_RENAME + e.getMessage(), Toast.LENGTH_LONG)
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
      // Use the dispatcher instead of calling onBackPressed directly
      getOnBackPressedDispatcher().onBackPressed();
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      if (selectedImages.isEmpty()) {
        Toast.makeText(this, Constants.TOAST_SELECT_AT_LEAST_ONE, Toast.LENGTH_SHORT).show();
        return true;
      }
      saveSelectedImages();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void finish() {
    // Ensure the original state is maintained if we didn't explicitly save
    if (hasChanges) {
      // Restore original state (this won't be seen by the user since we're finishing)
      selectedImages = initialSelectedImages;
    }
    super.finish();
  }

  // Save selected images without exiting
  private void saveSelectedImages() {
    // First sync with the adapter's current order
    List<Image> currentOrderedImages = selectedAdapter.getImages();
    if (currentOrderedImages != null && !currentOrderedImages.isEmpty()) {
      selectedImages = new ArrayList<>(currentOrderedImages);
    }

    Set<Integer> selectedImageIds = new HashSet<>();

    for (Image image : selectedImages) {
      selectedImageIds.add(image.id);
    }

    // Save which images are selected
    SelectedImagesManager.saveSelectedImages(this, selectedImageIds);

    // Save the order of selected images using the dedicated method
    persistImageOrder();

    // Reset the changes flag
    hasChanges = false;

    // Show confirmation toast
    Toast.makeText(this, Constants.TOAST_SELECTION_SAVED, Toast.LENGTH_SHORT).show();
  }

  // Only called when explicitly saving
  private void persistImageOrder() {
    // Save the selected images order to preferences
    List<Integer> imageIds = new ArrayList<>();
    for (Image image : selectedImages) {
      imageIds.add(image.id);
    }

    SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    Gson gson = new Gson();
    String json = gson.toJson(imageIds);
    prefs.edit().putString(PREF_IMAGE_ORDER, json).apply();
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

  private void openImagePicker() {
    // Launch the image picker with support for multiple images
    pickImageLauncher.launch("image/*");
    Toast.makeText(this, Constants.TOAST_MULTI_SELECT_HINT, Toast.LENGTH_SHORT).show();
  }

  private void handleMultipleImageSelection(List<Uri> imageUris) {
    // Handle case when user cancels selection or doesn't select any images
    if (imageUris == null || imageUris.isEmpty()) {
      return; // Just return silently, no need for toast message on cancellation
    }

    final int imageCount = imageUris.size();
    processAndSaveImages(imageUris, imageCount);
  }

  private void processAndSaveImages(List<Uri> imageUris, final int imageCount) {
    final List<Image> newImages = new ArrayList<>();

    executor.execute(() -> {
      try {
        // Process each image
        for (Uri imageUri : imageUris) {
          processImageUri(imageUri, newImages);
        }

        // Update UI after all images are processed
        updateUIAfterImageProcessing(imageCount, newImages);
      } catch (Exception e) {
        showImageSaveError(e);
      }
    });
  }

  private void processImageUri(Uri imageUri, List<Image> newImages) {
    String fileName = "Custom_" + System.currentTimeMillis();
    Image newImage = new Image(fileName, SOURCE_CUSTOM, imageUri.toString());

    // Take persistent read permissions on the URI
    try {
      getContentResolver().takePersistableUriPermission(
          imageUri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
      );
    } catch (SecurityException e) {
      // Continue even if we can't get persistent permissions
    }

    // Insert the image into the database
    long id = db.imageDao().insertImage(newImage);
    newImage.id = (int) id;
    newImages.add(newImage);
  }

  private void updateUIAfterImageProcessing(int imageCount, List<Image> newImages) {
    runOnUiThread(() -> {
      allImages.addAll(newImages);
      updateUnselectedImages();
      showImagesAddedToast(imageCount);
    });
  }

  private void showImagesAddedToast(int imageCount) {
    Toast.makeText(
        this,
        imageCount > 1
        ? imageCount + " " + Constants.TOAST_IMAGES_ADDED
        : Constants.TOAST_IMAGES_ADDED,
        Toast.LENGTH_SHORT
    ).show();
  }

  private void updateUnselectedImages() {
    // Create a completely new list to avoid any reference issues
    List<Image> unselected = new ArrayList<>();

    // Get the currently selected images directly from the adapter
    List<Image> currentlySelected = new ArrayList<>();
    if (selectedAdapter != null && selectedAdapter.getImages() != null) {
      currentlySelected.addAll(selectedAdapter.getImages());
    }

    // Add all images that aren't in the selected list
    for (Image image : allImages) {
      boolean isSelected = false;
      for (Image selected : currentlySelected) {
        if (image.id == selected.id) {
          isSelected = true;
          break;
        }
      }

      if (!isSelected) {
        unselected.add(image);
      }
    }

    // Update the adapter with the new list
    unselectedAdapter.updateImages(unselected);
  }

  private void showImageSaveError(Exception e) {
    runOnUiThread(() -> Toast
        .makeText(this, Constants.TOAST_FAILED_SAVE_IMAGE + e.getMessage(), Toast.LENGTH_LONG)
        .show());
  }

  private void deleteImage(Image image) {
    executor.execute(() -> {
      try {
        db.imageDao().deleteImage(image);
        runOnUiThread(() -> {
          loadImages();
          Toast.makeText(this, Constants.TOAST_IMAGE_DELETED, Toast.LENGTH_SHORT).show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast
            .makeText(this, Constants.TOAST_FAILED_DELETE + e.getMessage(), Toast.LENGTH_LONG)
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

  // Make the ImageAdapter notify our activity when selections change
  private void setupUnselectedAdapter() {
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

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        updateSelectAllCheckboxState();
      }
    });

    RecyclerView recyclerViewUnselected = findViewById(R.id.recyclerViewUnselected);
    recyclerViewUnselected.setAdapter(unselectedAdapter);
  }

  private static class DialogComponents {
    final ImageView imagePreview;
    final EditText editImageName;
    final Button btnDeleteImage;
    final Button btnCancel;
    final Button btnSave;
    final TextInputLayout textInputLayout;

    DialogComponents(
        ImageView imagePreview,
        EditText editImageName,
        Button btnDeleteImage,
        Button btnCancel,
        Button btnSave,
        TextInputLayout textInputLayout
    ) {
      this.imagePreview = imagePreview;
      this.editImageName = editImageName;
      this.btnDeleteImage = btnDeleteImage;
      this.btnCancel = btnCancel;
      this.btnSave = btnSave;
      this.textInputLayout = textInputLayout;
    }
  }

  /**
   * A simpler, more stable implementation of grid spacing
   */
  public static class StableGridSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public StableGridSpacingDecoration(int spacing) {
      this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(
        @NonNull android.graphics.Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state
    ) {
      outRect.left = spacing;
      outRect.right = spacing;
      outRect.top = spacing;
      outRect.bottom = spacing;
    }
  }
} 


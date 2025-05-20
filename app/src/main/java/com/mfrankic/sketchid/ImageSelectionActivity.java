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

    setupBackPressHandler();

    multiSelectControls = findViewById(R.id.multiSelectControls);
    Button btnAddToSelection = findViewById(R.id.btnAddToSelection);
    Button btnRemoveSelected = findViewById(R.id.btnRemoveSelected);
    Button btnExitMultiSelect = findViewById(R.id.btnExitMultiSelect);
    checkboxSelectAll = findViewById(R.id.checkboxSelectAll);

    checkboxSelectAll.setOnClickListener(v -> handleSelectAllCheckboxClick());
    btnAddToSelection.setOnClickListener(v -> addSelectedImagesToSelection());
    btnRemoveSelected.setOnClickListener(v -> showRemoveConfirmationDialog());
    btnExitMultiSelect.setOnClickListener(v -> exitMultiSelectMode());

    RecyclerView recyclerViewUnselected = findViewById(R.id.recyclerViewUnselected);
    int spanCount = calculateSpanCount();
    GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
    recyclerViewUnselected.setLayoutManager(layoutManager);
    recyclerViewUnselected.addItemDecoration(new StableGridSpacingDecoration(getResources().getDimensionPixelSize(
        R.dimen.grid_spacing) / 2));

    setupUnselectedAdapter();

    textViewSelected = findViewById(R.id.textViewSelected);
    textViewSelectedNumber = findViewById(R.id.textViewSelectedNumber);
    recyclerViewSelected = findViewById(R.id.recyclerViewSelected);
    recyclerViewSelected.setLayoutManager(new LinearLayoutManager(this));
    selectedAdapter = new ImageOrderAdapter(this, selectedImages);
    selectedAdapter.setOnImageUnselectListener(this::handleImageSelection);
    selectedAdapter.setOnReorderListener(this::saveImageOrder);
    recyclerViewSelected.setAdapter(selectedAdapter);
    selectedAdapter.attachToRecyclerView(recyclerViewSelected);

    btnAddImage = findViewById(R.id.btnAddImage);
    btnAddImage.setOnClickListener(v -> openImagePicker());

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

              this.setEnabled(false);
              getOnBackPressedDispatcher().onBackPressed();

              this.setEnabled(true);
            }
          }
        }
    );
  }

  private void updateSelectAllCheckboxState() {
    if (!isMultiSelectMode) return;

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

    boolean isAllSelected = (selected > 0 && selected == total);
    boolean isIndeterminate = (selected > 0 && selected < total);

    int checkboxState = 0;
    if (isAllSelected) checkboxState = 1;
    else if (isIndeterminate) checkboxState = 2;
    return checkboxState;
  }

  private void handleSelectAllCheckboxClick() {
    int total = unselectedAdapter.getItemCount();
    int selected = unselectedAdapter.getMultiSelectedImages().size();

    if (selected < total) {

      selectAllImages();
    } else {

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

    updateSelectAllCheckboxState();
  }

  private void handleImageSelection(Image image) {

    processImageSelectionToggle(image);

    updateUnselectedImages();

    saveImageOrder();

    updateUIAfterImageSelection();
  }

  private void processImageSelectionToggle(Image image) {

    List<Image> currentlySelected = getCurrentlySelectedImages();

    boolean wasSelected = isImageAlreadySelected(image, currentlySelected);
    List<Image> newSelectedList = toggleImageInSelection(image, currentlySelected, wasSelected);

    selectedImages = newSelectedList;

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

      for (Image selected : currentlySelected) {
        if (selected.id != image.id) {
          newSelectedList.add(selected);
        }
      }
    } else {

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

    selectedAdapter.setImages(adapterImages);
  }

  private void updateUIAfterImageSelection() {

    textViewSelectedNumber.setText(String.format(
        Locale.getDefault(),
        getString(R.string.selected_count_format),
        selectedImages.size()
    ));

    updateSelectedSectionVisibility();
  }

  private void saveImageOrder() {

    List<Image> currentOrderedImages = selectedAdapter.getImages();
    if (!currentOrderedImages.isEmpty()) {

      selectedImages = new ArrayList<>();
      selectedImages.addAll(currentOrderedImages);
    }

    hasChanges = true;
  }

  private void validateImageName(String name, TextInputLayout textInputLayout) {
    if (name.trim().isEmpty()) {
      textInputLayout.setError(ERROR_NAME_EMPTY);
      textInputLayout.setErrorEnabled(true);
    }
  }

  private void showImageOptionsDialog(Image image) {

    FrameLayout parentContainer = new FrameLayout(this);
    parentContainer.setLayoutParams(new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
                                                                 ViewGroup.LayoutParams.WRAP_CONTENT
    ));

    View dialogView = LayoutInflater
        .from(this)
        .inflate(R.layout.dialog_image_options, parentContainer, false);

    DialogComponents components = setupDialogComponents(dialogView, image);

    loadImagePreview(image, components.imagePreview);

    AlertDialog dialog = createImageOptionsDialog(dialogView);

    setupDialogButtonListeners(dialog, image, components);

    dialog.show();
  }

  private DialogComponents setupDialogComponents(View dialogView, Image image) {

    ImageView imagePreview = dialogView.findViewById(R.id.image_preview);
    EditText editImageName = dialogView.findViewById(R.id.editImageName);
    Button btnDeleteImage = dialogView.findViewById(R.id.btnDeleteImage);
    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);
    TextInputLayout textInputLayout = (TextInputLayout) editImageName.getParent().getParent();

    editImageName.setText(image.name);

    validateImageName(image.name, textInputLayout);

    setupImageNameValidator(editImageName, textInputLayout);

    setupCheckerboardBackground(imagePreview);

    btnDeleteImage.setVisibility(image.source.equals(SOURCE_DEFAULT) ? View.GONE : View.VISIBLE);

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

  private AlertDialog createImageOptionsDialog(View dialogView) {
    return new AlertDialog.Builder(this)
        .setTitle(Constants.DIALOG_TITLE_IMAGE_OPTIONS)
        .setView(dialogView)
        .create();
  }

  private void setupCheckerboardBackground(ImageView imagePreview) {
    int lightColor = Color.rgb(238, 238, 238);
    int darkColor = Color.rgb(204, 204, 204);
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 16);
    imagePreview.setBackground(checkerboardDrawable);
  }

  private void setupImageNameValidator(EditText editImageName, TextInputLayout textInputLayout) {
    editImageName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

        String text = s.toString().trim();
        if (text.isEmpty()) {

          textInputLayout.setError(ERROR_NAME_EMPTY);
          textInputLayout.setErrorEnabled(true);
        } else {

          textInputLayout.setError(null);
          textInputLayout.setErrorEnabled(false);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        // No action needed
      }
    });
  }

  private void loadImagePreview(Image image, ImageView imagePreview) {
    if (image.source.equals(SOURCE_DEFAULT)) {
      loadDefaultImage(image, imagePreview);
    } else {
      loadCustomImage(image, imagePreview);
    }
  }

  private void setupDialogButtonListeners(
      AlertDialog dialog,
      Image image,
      DialogComponents components
  ) {

    components.btnCancel.setOnClickListener(v -> dialog.dismiss());

    components.btnSave.setOnClickListener(v -> handleSaveButtonClick(dialog, image, components));

    components.btnDeleteImage.setOnClickListener(v -> {
      dialog.dismiss();
      deleteImage(image);
    });
  }

  private void handleSaveButtonClick(AlertDialog dialog, Image image, DialogComponents components) {
    String newName = components.editImageName.getText().toString().trim();
    if (newName.isEmpty()) {

      components.textInputLayout.setError(ERROR_NAME_EMPTY);
      components.textInputLayout.setErrorEnabled(true);
      return;
    }

    if (!newName.equals(image.name)) {

      renameImage(image, newName);
    }
    dialog.dismiss();
  }

  private void loadImages() {
    executor.execute(() -> {
      db.imageDao().deleteInvalidCustomImages();

      List<Image> fetchedImages = fetchAllImages();
      processLoadedImages(fetchedImages);
    });
  }

  private void loadDefaultImage(Image image, ImageView imagePreview) {

    int resourceId = ResourceUtils.getDrawableResourceByName(image.name);

    if (resourceId == 0 && !TextUtils.isEmpty(image.path)) {
      try {
        resourceId = Integer.parseInt(image.path);
      } catch (NumberFormatException ignored) {
        // Ignore the exception
      }
    }

    if (resourceId != 0) {

      Glide.with(this).load(resourceId).fitCenter().into(imagePreview);
    } else {
      Toast.makeText(this, Constants.TOAST_FAILED_LOAD_DEFAULT, Toast.LENGTH_SHORT).show();
    }
  }

  private void loadCustomImage(Image image, ImageView imagePreview) {

    Uri uri = Uri.parse(image.path);
    try {
      getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

      Glide.with(this).load(uri).fitCenter().into(imagePreview);
    } catch (Exception e) {
      Toast
          .makeText(this, Constants.TOAST_FAILED_LOAD_IMAGE + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private List<Image> fetchAllImages() {

    List<Image> fetchedImages = new ArrayList<>();
    fetchedImages.addAll(db.imageDao().getImagesBySource(SOURCE_DEFAULT));
    fetchedImages.addAll(db.imageDao().getImagesBySource(SOURCE_CUSTOM));
    return fetchedImages;
  }

  private void processLoadedImages(List<Image> fetchedImages) {

    allImages.clear();
    allImages.addAll(fetchedImages);

    processSelectedImages();

    initialSelectedImages = new ArrayList<>(selectedImages);

    updateUIAfterLoading();
  }

  private boolean isImageInDatabase(int imageId) {
    for (Image image : allImages) {
      if (image.id == imageId) {
        return true;
      }
    }
    return false;
  }

  private void processSelectedImages() {

    if (selectedImages.isEmpty()) {
      loadSelectionsFromPreferences();
    } else {
      filterSelectedImagesAgainstDatabase();
    }
  }

  private void filterSelectedImagesAgainstDatabase() {

    List<Image> validSelectedImages = new ArrayList<>();

    for (Image selectedImage : selectedImages) {
      boolean imageStillExists = isImageInDatabase(selectedImage.id);
      if (imageStillExists) {

        for (Image image : allImages) {
          if (image.id == selectedImage.id) {
            validSelectedImages.add(image);
            break;
          }
        }
      } else {

        hasChanges = true;
      }
    }

    selectedImages.clear();
    selectedImages.addAll(validSelectedImages);
  }

  private void loadSelectionsFromPreferences() {

    Set<Integer> selectedImageIds = SelectedImagesManager.getSelectedImages(this);

    List<Integer> savedOrderIds = loadImageOrderFromPreferences();

    if (!savedOrderIds.isEmpty()) {
      loadImagesInSavedOrder(selectedImageIds, savedOrderIds);
    } else {
      loadImagesWithoutOrder(selectedImageIds);
    }

    initialSelectedImages = new ArrayList<>(selectedImages);
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

  private void loadImagesInSavedOrder(Set<Integer> selectedImageIds, List<Integer> savedOrderIds) {

    addImagesFromSavedOrder(selectedImageIds, savedOrderIds);

    addRemainingSelectedImages(selectedImageIds, savedOrderIds);
  }

  private void addImageWithIdToSelected(int id) {
    for (Image image : allImages) {
      if (image.id == id) {
        selectedImages.add(image);
        break;
      }
    }
  }

  private void loadImagesWithoutOrder(Set<Integer> selectedImageIds) {

    for (Image image : allImages) {
      if (selectedImageIds.contains(image.id)) {
        selectedImages.add(image);
      }
    }
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
      } catch (Exception ignored) {
        // Ignore the exception
      }
    }

    return result;
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

  private void updateUIAfterLoading() {
    runOnUiThread(() -> {

      textViewSelectedNumber.setText(String.format(
          Locale.getDefault(),
          getString(R.string.selected_count_format),
          selectedImages.size()
      ));

      selectedAdapter.setImages(new ArrayList<>(selectedImages));

      updateUnselectedImages();
      updateSelectedSectionVisibility();

      updateMultiSelectControlsVisibility();
    });
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

      Toast.makeText(this, Constants.TOAST_DEFAULT_NOT_REMOVED, Toast.LENGTH_SHORT).show();
      exitMultiSelectMode();
    }
  }

  private void showExitConfirmationDialog() {
    new AlertDialog.Builder(this)
        .setTitle(Constants.DIALOG_TITLE_UNSAVED_CHANGES)
        .setMessage(Constants.DIALOG_MSG_UNSAVED_CHANGES)
        .setPositiveButton(Constants.SAVE_AND_EXIT_BUTTON, (dialog, which) -> saveAndExit())
        .setNegativeButton(
            Constants.EXIT_WITHOUT_SAVING_BUTTON, (dialog, which) -> {

              selectedImages = new ArrayList<>(initialSelectedImages);
              hasChanges = false;

              finish();
            }
        )
        .setNeutralButton(Constants.SAVE_BUTTON, (dialog, which) -> saveSelectedImages())
        .show();
  }

  private void saveAndExit() {

    saveSelectedImages();

    Intent resultIntent = new Intent();
    resultIntent.putExtra(
        "selected_image_ids",
        selectedImages.stream().mapToInt(image -> image.id).toArray()
    );
    setResult(RESULT_OK, resultIntent);
    finish();
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

  private void addSelectedImagesToSelection() {

    List<Integer> selectedImageIds = unselectedAdapter.getSelectedImageIds();

    if (selectedImageIds.isEmpty()) {
      Toast.makeText(this, Constants.TOAST_NO_IMAGES_SELECTED_ADD, Toast.LENGTH_SHORT).show();
      return;
    }

    hasChanges = true;

    List<Image> currentlySelected = getCurrentlySelectedImages();
    List<Image> imagesToAdd = findImagesToAdd(selectedImageIds, currentlySelected);
    List<Image> newCombinedList = combineImageLists(currentlySelected, imagesToAdd);

    updateSelectionWithNewList(newCombinedList);
    updateUiAfterSelectionChange();

    exitMultiSelectMode();
  }

  private void updateSelectionWithNewList(List<Image> newSelectedList) {

    selectedImages = newSelectedList;

    List<Image> adapterImages = new ArrayList<>();
    for (Image img : newSelectedList) {
      if (img != null) {
        adapterImages.add(img);
      }
    }

    selectedAdapter.setImages(adapterImages);
  }

  private void updateSelectedSectionVisibility() {
    boolean hasSelectedImages = !selectedImages.isEmpty();

    textViewSelected.setVisibility(View.VISIBLE);
    recyclerViewSelected.setVisibility(hasSelectedImages ? View.VISIBLE : View.GONE);

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

    if (hasChanges) {

      selectedImages = initialSelectedImages;
    }
    super.finish();
  }

  private void saveSelectedImages() {

    List<Image> currentOrderedImages = selectedAdapter.getImages();
    if (currentOrderedImages != null && !currentOrderedImages.isEmpty()) {
      selectedImages = new ArrayList<>(currentOrderedImages);
    }

    Set<Integer> selectedImageIds = new HashSet<>();

    for (Image image : selectedImages) {
      selectedImageIds.add(image.id);
    }

    SelectedImagesManager.saveSelectedImages(this, selectedImageIds);

    persistImageOrder();

    hasChanges = false;

    Toast.makeText(this, Constants.TOAST_SELECTION_SAVED, Toast.LENGTH_SHORT).show();
  }

  private void persistImageOrder() {

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

  private void updateUiAfterSelectionChange() {

    textViewSelectedNumber.setText(String.format(
        Locale.getDefault(),
        getString(R.string.selected_count_format),
        selectedImages.size()
    ));

    saveImageOrder();

    updateUnselectedImages();

    updateSelectedSectionVisibility();
  }

  private void openImagePicker() {

    pickImageLauncher.launch("image/*");
    Toast.makeText(this, Constants.TOAST_MULTI_SELECT_HINT, Toast.LENGTH_SHORT).show();
  }

  private void handleMultipleImageSelection(List<Uri> imageUris) {

    if (imageUris == null || imageUris.isEmpty()) {
      return;
    }

    final int imageCount = imageUris.size();
    processAndSaveImages(imageUris, imageCount);
  }

  private void processAndSaveImages(List<Uri> imageUris, final int imageCount) {
    final List<Image> newImages = new ArrayList<>();

    executor.execute(() -> {
      try {

        for (Uri imageUri : imageUris) {
          processImageUri(imageUri, newImages);
        }

        updateUIAfterImageProcessing(imageCount, newImages);
      } catch (Exception e) {
        showImageSaveError(e);
      }
    });
  }

  private void updateUIAfterImageProcessing(int imageCount, List<Image> newImages) {
    runOnUiThread(() -> {
      allImages.addAll(newImages);
      updateUnselectedImages();
      showImagesAddedToast(imageCount);
    });
  }

  private void updateUnselectedImages() {

    List<Image> unselected = new ArrayList<>();

    List<Image> currentlySelected = new ArrayList<>();
    if (selectedAdapter != null && selectedAdapter.getImages() != null) {
      currentlySelected.addAll(selectedAdapter.getImages());
    }

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

    unselectedAdapter.updateImages(unselected);
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

  private void processImageUri(Uri imageUri, List<Image> newImages) {
    String fileName = "Custom_" + System.currentTimeMillis();
    Image newImage = new Image(fileName, SOURCE_CUSTOM, imageUri.toString());

    try {
      getContentResolver().takePersistableUriPermission(
          imageUri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
      );
    } catch (SecurityException ignored) {
      // Ignore the exception
    }

    long id = db.imageDao().insertImage(newImage);
    newImage.id = (int) id;
    newImages.add(newImage);
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

    int screenWidth = getResources().getDisplayMetrics().widthPixels;

    int idealItemWidth = 100;

    float density = getResources().getDisplayMetrics().density;
    int itemWidthInPixels = (int) (idealItemWidth * density);

    int spanCount = Math.max(2, screenWidth / itemWidthInPixels);
    return Math.min(spanCount, 4);
  }

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


package com.mfrankic.sketchid;

public final class Constants {

  public static final String KEY_CURRENT_ITEM_ATTEMPT = "current_item_attempt";
  public static final String KEY_CURRENT_ITEM_INDEX = "current_item_index";
  public static final String KEY_DRAWING_ATTEMPTS = "drawing_attempts";
  public static final String KEY_SELECTED_USER = "selected_user";
  public static final String KEY_THEME = "theme";
  public static final String KEY_USER_PROGRESS_PREFIX = "user_progress_";

  // Common source values
  public static final String SOURCE_DEFAULT = "default";
  public static final String SOURCE_CUSTOM = "custom";

  // Default values
  public static final int DEFAULT_ATTEMPTS = 4;

  // Common string literals
  public static final String ERROR_NAME_EMPTY = "Name cannot be empty";
  public static final String DRAWING_DATA_PREFIX = "_drawing_data_";
  public static final String FILE_EXPORT_PATTERN = "%d,%d,%s,%d,%d,%s,%s,%s,%s,%d,%s%n";
  public static final String SKETCHID_DATA_DIR = "SketchIDData";
  public static final String IMAGES_VIEWED = "imagesViewed";
  public static final String FINISHED = "finished";
  public static final String SETTINGS = "settings";
  public static final String ACTION_START = "START";
  public static final String ACTION_END = "END";

  // Dialog button labels
  public static final String DELETE_BUTTON = "Delete";
  public static final String CANCEL_BUTTON = "Cancel";
  public static final String REMOVE_BUTTON = "Remove";
  public static final String REMOVE_CUSTOM_ONLY_BUTTON = "Remove Custom Only";
  public static final String SAVE_BUTTON = "Save";
  public static final String SAVE_AND_EXIT_BUTTON = "Save and Exit";
  public static final String EXIT_WITHOUT_SAVING_BUTTON = "Exit Without Saving";
  public static final String YES_BUTTON = "Yes";
  public static final String NO_BUTTON = "No";

  // Dialog titles and messages
  public static final String DIALOG_TITLE_IMAGE_OPTIONS = "Image Options";
  public static final String DIALOG_TITLE_REMOVE_IMAGES = "Remove Images";
  public static final String DIALOG_TITLE_UNSAVED_CHANGES = "Unsaved Changes";
  public static final String DIALOG_TITLE_EXIT_DRAWING = "Exit Drawing";
  public static final String DIALOG_MSG_DEFAULT_IMAGES =
      "Only non-default images can be removed. If you proceed, only custom images will be "
      + "deleted. Are you sure you want to continue?";
  public static final String DIALOG_MSG_REMOVE_IMAGES
      = "Are you sure you want to remove the selected images?";
  public static final String DIALOG_MSG_UNSAVED_CHANGES
      = "You have unsaved changes. What would you like to do?";
  public static final String DIALOG_MSG_EXIT_DRAWING = "Are you sure you want to exit?";

  // Toast messages
  public static final String TOAST_FAILED_LOAD_DEFAULT = "Failed to load default image";
  public static final String TOAST_FAILED_LOAD_IMAGE = "Failed to load image: ";
  public static final String TOAST_NO_IMAGES_SELECTED = "No images selected for removal";
  public static final String TOAST_IMAGES_DELETED = "Selected images deleted";
  public static final String TOAST_DEFAULT_NOT_REMOVED = "Default images cannot be removed";
  public static final String TOAST_SELECT_AT_LEAST_ONE = "Please select at least one image";
  public static final String TOAST_SELECTION_SAVED = "Selection saved";
  public static final String TOAST_NO_IMAGES_SELECTED_ADD = "No images selected";
  public static final String TOAST_IMAGE_RENAMED = "Image renamed successfully";
  public static final String TOAST_FAILED_RENAME = "Failed to rename image: ";
  public static final String TOAST_FAILED_DELETE = "Failed to delete images: ";
  public static final String TOAST_IMAGE_DELETED = "Image deleted successfully";
  public static final String TOAST_FAILED_SAVE_IMAGE = "Failed to save image: ";
  public static final String TOAST_INVALID_USER = "Invalid user selected. Returning to Home.";
  public static final String TOAST_NO_DRAWING
      = "Please draw something before moving to the next image.";
  public static final String TOAST_NO_IMAGES_SETTINGS
      = "No images selected. Please select images in settings.";
  public static final String TOAST_IMAGES_ADDED = "Images added successfully";
  public static final String TOAST_MULTI_SELECT_HINT = "You can select multiple images";

  // Drawing-related constants
  public static final String DRAWING_KEY_ATTEMPTS = "drawing_attempts";
  public static final String DRAWING_KEY_TIME_STARTED = "time_started";
  public static final String DRAWING_KEY_SELECTED_IMAGE_IDS = "selected_image_ids";
  public static final String DRAWING_KEY_DEVICE_MODEL = "device_model";
  public static final String DRAWING_KEY_ANDROID_VERSION = "android_version";

  // Format patterns
  public static final String FORMAT_PROGRESS_TEXT = "Attempt: %d/%d\tOverall: %d/%d";

  // Preference file names
  public static final String PREF_IMAGE_ORDER = "image_order";
  public static final String PREF_SELECTED_IMAGES = "selected_images";
  public static final String PREF_FILE_SELECTED_IMAGES = "selected_images";

  // File format patterns
  public static final String COLUMN_HEADERS
      = "id,userID,userName,attempt,time,x,y,action,itemType,imageID,imageName\n";
  public static final String EXPORT_DATE_FORMAT = "yyyyMMdd_HHmmss";
  public static final String FAILED_EXPORT_MESSAGE = "Failed to create export directory";

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

}

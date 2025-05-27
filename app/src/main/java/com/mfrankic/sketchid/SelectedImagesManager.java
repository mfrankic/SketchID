package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.PREF_FILE_SELECTED_IMAGES;
import static com.mfrankic.sketchid.Constants.PREF_SELECTED_IMAGES;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for managing the set of selected image IDs using {@link SharedPreferences}.
 * This class provides methods to save and retrieve the IDs of images that the user has selected
 * for drawing sessions or other purposes within the application.
 * This class is not meant to be instantiated.
 */
public final class SelectedImagesManager {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private SelectedImagesManager() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Retrieves the set of selected image IDs from {@link SharedPreferences}.
   * Image IDs are stored as strings and converted back to integers.
   * Any non-integer values stored in preferences are ignored.
   *
   * @param context The application context, used to access {@link SharedPreferences}.
   * @return A {@link Set} of {@link Integer} representing the IDs of selected images.
   * Returns an empty set if no images are selected or if an error occurs.
   */
  public static Set<Integer> getSelectedImages(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(
        PREF_FILE_SELECTED_IMAGES,
        Context.MODE_PRIVATE
    );
    Set<String> stringSet = prefs.getStringSet(PREF_SELECTED_IMAGES, new HashSet<>());
    Set<Integer> intSet = new HashSet<>();

    for (String s : stringSet) {
      try {
        intSet.add(Integer.parseInt(s));
      } catch (NumberFormatException ignored) {
        // Ignore any non-integer values, log if necessary for debugging
      }
    }

    return intSet;
  }

  /**
   * Saves the set of selected image IDs to {@link SharedPreferences}.
   * Image IDs are converted to strings before saving.
   *
   * @param context          The application context, used to access {@link SharedPreferences}.
   * @param selectedImageIds A {@link Set} of {@link Integer} representing the IDs of images to
   *                         be saved.
   */
  public static void saveSelectedImages(Context context, Set<Integer> selectedImageIds) {
    Set<String> stringSet = new HashSet<>();
    for (Integer id : selectedImageIds) {
      stringSet.add(id.toString());
    }

    context
        .getSharedPreferences(PREF_FILE_SELECTED_IMAGES, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(PREF_SELECTED_IMAGES, stringSet)
        .apply();
  }
} 

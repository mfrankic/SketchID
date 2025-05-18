package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.PREF_FILE_SELECTED_IMAGES;
import static com.mfrankic.sketchid.Constants.PREF_SELECTED_IMAGES;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to manage selected images across the app
 */
public final class SelectedImagesManager {

  /**
   * Private constructor to prevent instantiation
   */
  private SelectedImagesManager() {
    throw new IllegalStateException("Utility class");
  }

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
      } catch (NumberFormatException e) {
        // Skip invalid entries
      }
    }

    return intSet;
  }

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

package com.mfrankic.sketchid;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SelectedImagesManager {
  private static final String PREF_NAME = "selected_images";
  private static final String KEY_SELECTED_IDS = "selected_image_ids";

  public static void saveSelectedImages(Context context, Set<Integer> selectedIds) {
    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    Set<String> stringIds = new HashSet<>();
    for (Integer id : selectedIds) {
      stringIds.add(String.valueOf(id));
    }
    prefs.edit().putStringSet(KEY_SELECTED_IDS, stringIds).apply();
  }

  public static Set<Integer> getSelectedImages(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    Set<String> stringIds = prefs.getStringSet(KEY_SELECTED_IDS, new HashSet<>());
    Set<Integer> ids = new HashSet<>();
    for (String id : stringIds) {
      ids.add(Integer.parseInt(id));
    }
    return ids;
  }
} 

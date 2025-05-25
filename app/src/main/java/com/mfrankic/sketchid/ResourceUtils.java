package com.mfrankic.sketchid;

import android.util.Log;

import androidx.annotation.DrawableRes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class to handle resource mapping without using resource reflection (getIdentifier).
 * This class creates a direct mapping between image names and their resource IDs.
 */
public class ResourceUtils {
  private static final String TAG = "ResourceUtils";
  private static final Map<String, Integer> drawableResourceMap = new HashMap<>();
  private static boolean initialized = false;

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private ResourceUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Initialize the resource mappings. Should be called once during app startup.
   */
  public static void initialize() {
    if (initialized) return;

    initializeDrawableMap();

    initialized = true;
  }

  private static void initializeDrawableMap() {

    drawableResourceMap.put("arrow", R.drawable.arrow);
    drawableResourceMap.put("checkmark", R.drawable.checkmark);
    drawableResourceMap.put("crown", R.drawable.crown);
    drawableResourceMap.put("envelope", R.drawable.envelope);
    drawableResourceMap.put("grid", R.drawable.grid);
    drawableResourceMap.put("heart", R.drawable.heart);
    drawableResourceMap.put("lightbulb", R.drawable.lightbulb);
    drawableResourceMap.put("smiley", R.drawable.smiley);
    drawableResourceMap.put("star", R.drawable.star);
    drawableResourceMap.put("umbrella", R.drawable.umbrella);
    drawableResourceMap.put("upload", R.drawable.upload);
  }

  /**
   * Get drawable resource ID for a given image name
   *
   * @param imageName Name of the image
   * @return Resource ID or 0 if not found
   */
  @DrawableRes
  public static int getDrawableResourceByName(String imageName) {
    if (!initialized) {
      Log.e(TAG, "ResourceUtils not initialized! Call initialize() first.");
      return 0;
    }

    if (imageName == null || imageName.isEmpty()) {
      return 0;
    }

    String normalizedName = imageName.toLowerCase(Locale.ROOT);
    Integer resourceId = drawableResourceMap.get(normalizedName);

    return resourceId != null ? resourceId : 0;
  }
} 

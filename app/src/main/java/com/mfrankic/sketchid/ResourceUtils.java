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

    // Add all drawable resources that will be accessed by name
    // This is much more efficient than using getIdentifier at runtime
    initializeDrawableMap();

    initialized = true;

    // Log all mappings in debug builds
    logDrawableMappings();
  }

  private static void initializeDrawableMap() {
    // Map all drawable resources found in the project
    drawableResourceMap.put("arrow", R.drawable.arrow);
    drawableResourceMap.put("crown", R.drawable.crown);
    drawableResourceMap.put("envelope", R.drawable.envelope);
    drawableResourceMap.put("grid", R.drawable.grid);
    drawableResourceMap.put("house", R.drawable.house);
    drawableResourceMap.put("lightbulb", R.drawable.lightbulb);
    drawableResourceMap.put("moon", R.drawable.moon);
    drawableResourceMap.put("smiley", R.drawable.smiley);
    drawableResourceMap.put("star", R.drawable.star);
    drawableResourceMap.put("sun", R.drawable.sun);
    drawableResourceMap.put("umbrella", R.drawable.umbrella);
  }

  /**
   * Log all available drawable mappings for debugging
   */
  private static void logDrawableMappings() {
    Log.d(TAG, "Available drawable mappings:");
    for (Map.Entry<String, Integer> entry : drawableResourceMap.entrySet()) {
      Log.d(TAG, "  - " + entry.getKey() + " => " + entry.getValue());
    }
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

    String normalizedName = imageName.toLowerCase(Locale.ROOT);
    Integer resourceId = drawableResourceMap.get(normalizedName);

    return resourceId != null ? resourceId : 0;
  }
} 

package com.mfrankic.sketchid;

import android.util.Log;

import androidx.annotation.DrawableRes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for managing and accessing drawable resources.
 * This class provides a way to get drawable resource IDs by their names without using
 * reflection ({@code getIdentifier}), which can be less performant.
 * It pre-populates a map of known drawable names to their resource IDs upon initialization.
 * <p>
 * Call {@link #initialize()} once during application startup (e.g., in
 * {@link SketchIDApplication#onCreate()})
 * to prepare the resource mappings.
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
   * Initializes the drawable resource mappings.
   * This method should be called once during application startup to populate the internal map
   * of drawable names to resource IDs. If already initialized, this method does nothing.
   */
  public static void initialize() {
    if (initialized) return;

    initializeDrawableMap();

    initialized = true;
  }

  /**
   * Populates the {@code drawableResourceMap} with mappings from drawable names (lowercase)
   * to their corresponding {@code R.drawable} resource IDs.
   */
  private static void initializeDrawableMap() {

    drawableResourceMap.put("arrow", R.drawable.arrow);
    drawableResourceMap.put("checkmark", R.drawable.check);
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
   * Retrieves the drawable resource ID for a given image name.
   * The image name is case-insensitive.
   *
   * @param imageName The name of the image (e.g., "arrow", "Heart").
   * @return The resource ID (e.g., {@code R.drawable.arrow}) if found, or 0 if the name is null,
   * empty, not found, or if {@link #initialize()} has not been called.
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

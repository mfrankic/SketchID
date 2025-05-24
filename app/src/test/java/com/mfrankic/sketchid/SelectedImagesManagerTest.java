package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for the SelectedImagesManager utility class
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34) // Use SDK 34 to avoid compatibility issues with Robolectric
public class SelectedImagesManagerTest {

  private Context context;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.getApplication();
    // Clear any existing preferences before each test
    clearPreferences();
  }

  private void clearPreferences() {
    context
        .getSharedPreferences(Constants.PREF_FILE_SELECTED_IMAGES, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply();
  }

  @Test
  public void testConstructorThrowsException() {
    // Test that the utility class constructor throws IllegalStateException
    try {
      // This should throw an exception since it's a utility class
      java.lang.reflect.Constructor<SelectedImagesManager> constructor
          = SelectedImagesManager.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Expected IllegalStateException to be thrown");
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
      assertEquals("Utility class", e.getCause().getMessage());
    }
  }

  @Test
  public void testGetSelectedImagesEmpty() {
    // Test getting selected images when no images are saved
    Set<Integer> selectedImages = SelectedImagesManager.getSelectedImages(context);

    assertNotNull(selectedImages);
    assertTrue(selectedImages.isEmpty());
  }

  @Test
  public void testSaveAndGetSelectedImages() {
    // Test saving and retrieving selected images
    Set<Integer> imagesToSave = new HashSet<>();
    imagesToSave.add(1);
    imagesToSave.add(2);
    imagesToSave.add(3);

    SelectedImagesManager.saveSelectedImages(context, imagesToSave);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(imagesToSave, retrievedImages);
    assertEquals(3, retrievedImages.size());
    assertTrue(retrievedImages.contains(1));
    assertTrue(retrievedImages.contains(2));
    assertTrue(retrievedImages.contains(3));
  }

  @Test
  public void testSaveEmptySet() {
    // Test saving an empty set
    Set<Integer> emptySet = new HashSet<>();

    SelectedImagesManager.saveSelectedImages(context, emptySet);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertNotNull(retrievedImages);
    assertTrue(retrievedImages.isEmpty());
  }

  @Test
  public void testOverwritePreviousSelection() {
    // Test that saving new images overwrites previous selection
    Set<Integer> firstSelection = new HashSet<>();
    firstSelection.add(1);
    firstSelection.add(2);

    SelectedImagesManager.saveSelectedImages(context, firstSelection);

    Set<Integer> secondSelection = new HashSet<>();
    secondSelection.add(3);
    secondSelection.add(4);
    secondSelection.add(5);

    SelectedImagesManager.saveSelectedImages(context, secondSelection);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(secondSelection, retrievedImages);
    assertEquals(3, retrievedImages.size());
    assertFalse(retrievedImages.contains(1));
    assertFalse(retrievedImages.contains(2));
    assertTrue(retrievedImages.contains(3));
    assertTrue(retrievedImages.contains(4));
    assertTrue(retrievedImages.contains(5));
  }

  @Test
  public void testSaveNullSet() {
    // Test saving a null set - should throw NullPointerException
    try {
      SelectedImagesManager.saveSelectedImages(context, null);
      fail("Expected NullPointerException to be thrown");
    } catch (NullPointerException e) {
      // Expected behavior - the method doesn't handle null input
      assertTrue(true);
    }

    // Verify that no data was corrupted
    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);
    assertNotNull(retrievedImages);
    assertTrue(retrievedImages.isEmpty());
  }

  @Test
  public void testWithNegativeNumbers() {
    // Test with negative image IDs
    Set<Integer> imagesToSave = new HashSet<>();
    imagesToSave.add(-1);
    imagesToSave.add(-10);
    imagesToSave.add(0);
    imagesToSave.add(5);

    SelectedImagesManager.saveSelectedImages(context, imagesToSave);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(imagesToSave, retrievedImages);
    assertTrue(retrievedImages.contains(-1));
    assertTrue(retrievedImages.contains(-10));
    assertTrue(retrievedImages.contains(0));
    assertTrue(retrievedImages.contains(5));
  }

  @Test
  public void testWithLargeNumbers() {
    // Test with very large image IDs
    Set<Integer> imagesToSave = new HashSet<>();
    imagesToSave.add(Integer.MAX_VALUE);
    imagesToSave.add(Integer.MIN_VALUE);
    imagesToSave.add(1000000);

    SelectedImagesManager.saveSelectedImages(context, imagesToSave);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(imagesToSave, retrievedImages);
    assertTrue(retrievedImages.contains(Integer.MAX_VALUE));
    assertTrue(retrievedImages.contains(Integer.MIN_VALUE));
    assertTrue(retrievedImages.contains(1000000));
  }

  @Test
  public void testDuplicateValues() {
    // Test that duplicates are handled correctly (Sets don't allow duplicates)
    Set<Integer> imagesToSave = new HashSet<>();
    imagesToSave.add(1);
    imagesToSave.add(2);
    imagesToSave.add(1); // Duplicate - should be ignored

    SelectedImagesManager.saveSelectedImages(context, imagesToSave);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(2, retrievedImages.size()); // Should only have 2 unique values
    assertTrue(retrievedImages.contains(1));
    assertTrue(retrievedImages.contains(2));
  }

  @Test
  public void testCorruptedPreferences() {
    // Test handling of corrupted preferences (non-integer values)
    SharedPreferences prefs = context.getSharedPreferences(
        Constants.PREF_FILE_SELECTED_IMAGES,
        Context.MODE_PRIVATE
    );

    // Manually insert some invalid data
    Set<String> corruptedData = new HashSet<>();
    corruptedData.add("1");      // Valid
    corruptedData.add("2");      // Valid
    corruptedData.add("invalid"); // Invalid - should be ignored
    corruptedData.add("3.14");   // Invalid - should be ignored
    corruptedData.add("");       // Invalid - should be ignored

    prefs.edit().putStringSet(Constants.PREF_SELECTED_IMAGES, corruptedData).apply();

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    // Should only contain the valid integers
    assertEquals(2, retrievedImages.size());
    assertTrue(retrievedImages.contains(1));
    assertTrue(retrievedImages.contains(2));
  }

  @Test
  public void testMultipleContexts() {
    // Test that different contexts are independent
    // (In this case, we'll just verify the same context works consistently)
    Set<Integer> imagesToSave = new HashSet<>();
    imagesToSave.add(1);
    imagesToSave.add(2);

    SelectedImagesManager.saveSelectedImages(context, imagesToSave);

    // Get from same context
    Set<Integer> retrievedImages1 = SelectedImagesManager.getSelectedImages(context);
    Set<Integer> retrievedImages2 = SelectedImagesManager.getSelectedImages(context);

    assertEquals(retrievedImages1, retrievedImages2);
    assertEquals(imagesToSave, retrievedImages1);
  }

  @Test
  public void testLargeDataSet() {
    // Test with a large number of selected images
    Set<Integer> largeSet = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      largeSet.add(i);
    }

    SelectedImagesManager.saveSelectedImages(context, largeSet);

    Set<Integer> retrievedImages = SelectedImagesManager.getSelectedImages(context);

    assertEquals(largeSet, retrievedImages);
    assertEquals(1000, retrievedImages.size());

    // Verify some random values
    assertTrue(retrievedImages.contains(0));
    assertTrue(retrievedImages.contains(500));
    assertTrue(retrievedImages.contains(999));
  }

  @Test
  public void testPersistenceBetweenOperations() {
    // Test that data persists between multiple operations
    Set<Integer> firstBatch = new HashSet<>();
    firstBatch.add(1);
    firstBatch.add(2);

    SelectedImagesManager.saveSelectedImages(context, firstBatch);

    // Verify first batch
    Set<Integer> retrieved1 = SelectedImagesManager.getSelectedImages(context);
    assertEquals(firstBatch, retrieved1);

    // Add more images
    Set<Integer> secondBatch = new HashSet<>();
    secondBatch.add(3);
    secondBatch.add(4);
    secondBatch.add(5);

    SelectedImagesManager.saveSelectedImages(context, secondBatch);

    // Verify second batch (should overwrite first)
    Set<Integer> retrieved2 = SelectedImagesManager.getSelectedImages(context);
    assertEquals(secondBatch, retrieved2);
    assertNotEquals(firstBatch, retrieved2);
  }
} 

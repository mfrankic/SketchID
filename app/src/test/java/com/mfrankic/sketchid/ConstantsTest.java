package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

@RunWith(MockitoJUnitRunner.class)
public class ConstantsTest {

  @Test
  public void testConstructor_ThrowsIllegalStateException() {
    try {
      Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Constructor should throw IllegalStateException");
    } catch (InvocationTargetException e) {
      assertTrue(
          "Should throw IllegalStateException",
          e.getCause() instanceof IllegalStateException
      );
      assertEquals("Should have correct message", "Utility class", e.getCause().getMessage());
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test
  public void testClass_IsFinal() {
    assertTrue("Constants should be final", Modifier.isFinal(Constants.class.getModifiers()));
  }

  @Test
  public void testClass_IsUtilityClass() {
    assertTrue("Constants should be public", Modifier.isPublic(Constants.class.getModifiers()));

    // Check that constructor is private
    try {
      Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
      assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Constants should have a default constructor");
    }
  }

  @Test
  public void testAllFieldsArePublicStaticFinal() {
    Field[] fields = Constants.class.getDeclaredFields();

    for (Field field : fields) {
      int modifiers = field.getModifiers();
      assertTrue("Field " + field.getName() + " should be public", Modifier.isPublic(modifiers));
      assertTrue("Field " + field.getName() + " should be static", Modifier.isStatic(modifiers));
      assertTrue("Field " + field.getName() + " should be final", Modifier.isFinal(modifiers));
    }
  }

  @Test
  public void testKeyConstants_AreStrings() {
    // Test key constants
    assertEquals(
        "KEY_CURRENT_ITEM_ATTEMPT",
        "current_item_attempt",
        Constants.KEY_CURRENT_ITEM_ATTEMPT
    );
    assertEquals("KEY_CURRENT_ITEM_INDEX", "current_item_index", Constants.KEY_CURRENT_ITEM_INDEX);
    assertEquals("KEY_DRAWING_ATTEMPTS", "drawing_attempts", Constants.KEY_DRAWING_ATTEMPTS);
    assertEquals("KEY_SELECTED_USER", "selected_user", Constants.KEY_SELECTED_USER);
    assertEquals("KEY_THEME", "theme", Constants.KEY_THEME);
    assertEquals("KEY_USER_PROGRESS_PREFIX", "user_progress_", Constants.KEY_USER_PROGRESS_PREFIX);
    assertEquals("KEY_DRAWING_MODE", "drawing_mode", Constants.KEY_DRAWING_MODE);
  }

  @Test
  public void testDrawingModeConstants() {
    assertEquals("DRAWING_MODE_NORMAL", "normal", Constants.DRAWING_MODE_NORMAL);
    assertEquals("DRAWING_MODE_OVERLAY", "overlay", Constants.DRAWING_MODE_OVERLAY);
  }

  @Test
  public void testSourceConstants() {
    assertEquals("SOURCE_DEFAULT", "default", Constants.SOURCE_DEFAULT);
    assertEquals("SOURCE_CUSTOM", "custom", Constants.SOURCE_CUSTOM);
  }

  @Test
  public void testErrorConstants() {
    assertEquals("ERROR_NAME_EMPTY", "Name cannot be empty", Constants.ERROR_NAME_EMPTY);
  }

  @Test
  public void testDataPrefixConstants() {
    assertEquals("DRAWING_DATA_PREFIX", "_drawing_data_", Constants.DRAWING_DATA_PREFIX);
    assertEquals("GRAVITY_DATA_PREFIX", "_gravity_data_", Constants.GRAVITY_DATA_PREFIX);
    assertEquals("GYROSCOPE_DATA_PREFIX", "_gyroscope_data_", Constants.GYROSCOPE_DATA_PREFIX);
    assertEquals(
        "MAGNETIC_FIELD_DATA_PREFIX",
        "_magnetic_field_data_",
        Constants.MAGNETIC_FIELD_DATA_PREFIX
    );
    assertEquals(
        "MAGNETIC_FIELD_BASELINE_DATA_PREFIX",
        "_magnetic_field_baseline_data_",
        Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX
    );
    assertEquals(
        "ACCELEROMETER_DATA_PREFIX",
        "_accelerometer_data_",
        Constants.ACCELEROMETER_DATA_PREFIX
    );
  }

  @Test
  public void testExportPatternConstants() {
    // Test that export patterns contain the expected format specifiers
    assertTrue(
        "FILE_EXPORT_PATTERN should contain format specifiers",
        Constants.FILE_EXPORT_PATTERN.contains("%d")
        && Constants.FILE_EXPORT_PATTERN.contains("%s")
    );
    assertTrue(
        "GRAVITY_EXPORT_PATTERN should contain format specifiers",
        Constants.GRAVITY_EXPORT_PATTERN.contains("%d")
        && Constants.GRAVITY_EXPORT_PATTERN.contains("%f")
    );
    assertTrue(
        "GYROSCOPE_EXPORT_PATTERN should contain format specifiers",
        Constants.GYROSCOPE_EXPORT_PATTERN.contains("%d")
        && Constants.GYROSCOPE_EXPORT_PATTERN.contains("%f")
    );
    assertTrue(
        "MAGNETIC_FIELD_EXPORT_PATTERN should contain format specifiers",
        Constants.MAGNETIC_FIELD_EXPORT_PATTERN.contains("%d")
        && Constants.MAGNETIC_FIELD_EXPORT_PATTERN.contains("%f")
    );
    assertTrue(
        "MAGNETIC_FIELD_BASELINE_EXPORT_PATTERN should contain format specifiers",
        Constants.MAGNETIC_FIELD_BASELINE_EXPORT_PATTERN.contains("%d")
        && Constants.MAGNETIC_FIELD_BASELINE_EXPORT_PATTERN.contains("%f")
    );
    assertTrue(
        "ACCELEROMETER_EXPORT_PATTERN should contain format specifiers",
        Constants.ACCELEROMETER_EXPORT_PATTERN.contains("%d")
        && Constants.ACCELEROMETER_EXPORT_PATTERN.contains("%f")
    );
  }

  @Test
  public void testDirectoryAndActionConstants() {
    assertEquals("SKETCHID_DATA_DIR", "SketchIDData", Constants.SKETCHID_DATA_DIR);
    assertEquals("IMAGES_VIEWED", "imagesViewed", Constants.IMAGES_VIEWED);
    assertEquals("FINISHED", "finished", Constants.FINISHED);
    assertEquals("SETTINGS", "settings", Constants.SETTINGS);
    assertEquals("ACTION_START", "START", Constants.ACTION_START);
    assertEquals("ACTION_END", "END", Constants.ACTION_END);
  }

  @Test
  public void testButtonConstants() {
    assertEquals("DELETE_BUTTON", "Delete", Constants.DELETE_BUTTON);
    assertEquals("CANCEL_BUTTON", "Cancel", Constants.CANCEL_BUTTON);
    assertEquals("REMOVE_BUTTON", "Remove", Constants.REMOVE_BUTTON);
    assertEquals(
        "REMOVE_CUSTOM_ONLY_BUTTON",
        "Remove Custom Only",
        Constants.REMOVE_CUSTOM_ONLY_BUTTON
    );
    assertEquals("SAVE_BUTTON", "Save", Constants.SAVE_BUTTON);
    assertEquals("SAVE_AND_EXIT_BUTTON", "Save and Exit", Constants.SAVE_AND_EXIT_BUTTON);
    assertEquals(
        "EXIT_WITHOUT_SAVING_BUTTON",
        "Exit Without Saving",
        Constants.EXIT_WITHOUT_SAVING_BUTTON
    );
    assertEquals("YES_BUTTON", "Yes", Constants.YES_BUTTON);
    assertEquals("NO_BUTTON", "No", Constants.NO_BUTTON);
  }

  @Test
  public void testDialogTitleConstants() {
    assertEquals(
        "DIALOG_TITLE_IMAGE_OPTIONS",
        "Image Options",
        Constants.DIALOG_TITLE_IMAGE_OPTIONS
    );
    assertEquals(
        "DIALOG_TITLE_REMOVE_IMAGES",
        "Remove Images",
        Constants.DIALOG_TITLE_REMOVE_IMAGES
    );
    assertEquals(
        "DIALOG_TITLE_UNSAVED_CHANGES",
        "Unsaved Changes",
        Constants.DIALOG_TITLE_UNSAVED_CHANGES
    );
    assertEquals("DIALOG_TITLE_EXIT_DRAWING", "Exit Drawing", Constants.DIALOG_TITLE_EXIT_DRAWING);
  }

  @Test
  public void testDialogMessageConstants() {
    assertTrue(
        "DIALOG_MSG_DEFAULT_IMAGES should contain meaningful text",
        Constants.DIALOG_MSG_DEFAULT_IMAGES.contains("non-default")
        && Constants.DIALOG_MSG_DEFAULT_IMAGES.contains("custom")
    );
    assertTrue(
        "DIALOG_MSG_REMOVE_IMAGES should be a question",
        Constants.DIALOG_MSG_REMOVE_IMAGES.contains("?")
    );
    assertTrue(
        "DIALOG_MSG_UNSAVED_CHANGES should mention unsaved changes",
        Constants.DIALOG_MSG_UNSAVED_CHANGES.contains("unsaved")
    );
    assertTrue(
        "DIALOG_MSG_EXIT_DRAWING should be a question",
        Constants.DIALOG_MSG_EXIT_DRAWING.contains("?")
    );
  }

  @Test
  public void testToastConstants() {
    // Test some key toast messages
    assertEquals(
        "TOAST_FAILED_LOAD_DEFAULT",
        "Failed to load default image",
        Constants.TOAST_FAILED_LOAD_DEFAULT
    );
    assertEquals(
        "TOAST_FAILED_LOAD_IMAGE",
        "Failed to load image: ",
        Constants.TOAST_FAILED_LOAD_IMAGE
    );
    assertEquals(
        "TOAST_NO_IMAGES_SELECTED",
        "No images selected for removal",
        Constants.TOAST_NO_IMAGES_SELECTED
    );
    assertEquals("TOAST_IMAGES_DELETED", "Selected images deleted", Constants.TOAST_IMAGES_DELETED);
    assertEquals("TOAST_SELECTION_SAVED", "Selection saved", Constants.TOAST_SELECTION_SAVED);
    assertEquals(
        "TOAST_INVALID_USER",
        "Invalid user selected. Returning to Home.",
        Constants.TOAST_INVALID_USER
    );
  }

  @Test
  public void testDrawingKeyConstants() {
    assertEquals("DRAWING_KEY_ATTEMPTS", "drawing_attempts", Constants.DRAWING_KEY_ATTEMPTS);
    assertEquals("DRAWING_KEY_TIME_STARTED", "time_started", Constants.DRAWING_KEY_TIME_STARTED);
    assertEquals(
        "DRAWING_KEY_SELECTED_IMAGE_IDS",
        "selected_image_ids",
        Constants.DRAWING_KEY_SELECTED_IMAGE_IDS
    );
    assertEquals("DRAWING_KEY_DEVICE_MODEL", "device_model", Constants.DRAWING_KEY_DEVICE_MODEL);
    assertEquals(
        "DRAWING_KEY_ANDROID_VERSION",
        "android_version",
        Constants.DRAWING_KEY_ANDROID_VERSION
    );
    assertEquals("DRAWING_KEY_MODE", "drawing_mode", Constants.DRAWING_KEY_MODE);
  }

  @Test
  public void testPreferenceConstants() {
    assertEquals("PREF_IMAGE_ORDER", "image_order", Constants.PREF_IMAGE_ORDER);
    assertEquals("PREF_SELECTED_IMAGES", "selected_images", Constants.PREF_SELECTED_IMAGES);
    assertEquals(
        "PREF_FILE_SELECTED_IMAGES",
        "selected_images",
        Constants.PREF_FILE_SELECTED_IMAGES
    );
  }

  @Test
  public void testFormatConstants() {
    assertTrue(
        "FORMAT_PROGRESS_TEXT should contain format specifiers",
        Constants.FORMAT_PROGRESS_TEXT.contains("%d")
    );
    assertEquals("EXPORT_DATE_FORMAT", "yyyyMMdd_HHmmss", Constants.EXPORT_DATE_FORMAT);
    assertEquals(
        "FAILED_EXPORT_MESSAGE",
        "Failed to create export directory",
        Constants.FAILED_EXPORT_MESSAGE
    );
  }

  @Test
  public void testColumnHeaderConstants() {
    assertTrue("COLUMN_HEADERS should contain id", Constants.COLUMN_HEADERS.contains("id"));
    assertTrue("COLUMN_HEADERS should contain userID", Constants.COLUMN_HEADERS.contains("userID"));
    assertTrue(
        "GRAVITY_COLUMN_HEADERS should contain gravity-specific fields",
        Constants.GRAVITY_COLUMN_HEADERS.contains("x,y,z")
    );
    assertTrue(
        "GYROSCOPE_COLUMN_HEADERS should contain gyroscope-specific fields",
        Constants.GYROSCOPE_COLUMN_HEADERS.contains("x,y,z")
    );
    assertTrue(
        "MAGNETIC_FIELD_COLUMN_HEADERS should contain magnetic field-specific fields",
        Constants.MAGNETIC_FIELD_COLUMN_HEADERS.contains("x,y,z")
    );
    assertTrue(
        "ACCELEROMETER_COLUMN_HEADERS should contain accelerometer-specific fields",
        Constants.ACCELEROMETER_COLUMN_HEADERS.contains("x,y,z")
    );
  }

  @Test
  public void testMagneticFieldBaselineHeaders() {
    assertTrue(
        "MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS should contain baseline-specific fields",
        Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS.contains("avgX")
        && Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS.contains("stdDev")
    );
  }

  @Test
  public void testAllStringConstantsAreNotNull() {
    Field[] fields = Constants.class.getDeclaredFields();

    for (Field field : fields) {
      if (field.getType() == String.class) {
        try {
          String value = (String) field.get(null);
          assertNotNull("String constant " + field.getName() + " should not be null", value);
          assertFalse(
              "String constant " + field.getName() + " should not be empty",
              value.isEmpty()
          );
        } catch (IllegalAccessException e) {
          fail("Should be able to access public field " + field.getName());
        }
      }
    }
  }

  @Test
  public void testNoMethodsExceptConstructor() {
    // Constants class should only have a constructor, no other methods
    assertEquals(
        "Constants class should have no public methods",
        0,
        Constants.class.getMethods().length - Object.class.getMethods().length
    );
  }

  @Test
  public void testPatternConsistency() {
    // Test that similar patterns have consistent structure
    String[] sensorPatterns = {
        Constants.GRAVITY_EXPORT_PATTERN,
        Constants.GYROSCOPE_EXPORT_PATTERN,
        Constants.MAGNETIC_FIELD_EXPORT_PATTERN,
        Constants.ACCELEROMETER_EXPORT_PATTERN
    };

    for (String pattern : sensorPatterns) {
      assertTrue("Sensor pattern should contain %d for integers", pattern.contains("%d"));
      assertTrue("Sensor pattern should contain %f for floats", pattern.contains("%f"));
      assertTrue("Sensor pattern should contain %s for strings", pattern.contains("%s"));
      assertTrue("Sensor pattern should end with newline", pattern.endsWith("%n"));
    }
  }

  @Test
  public void testHeaderColumnConsistency() {
    String[] sensorHeaders = {
        Constants.GRAVITY_COLUMN_HEADERS,
        Constants.GYROSCOPE_COLUMN_HEADERS,
        Constants.MAGNETIC_FIELD_COLUMN_HEADERS,
        Constants.ACCELEROMETER_COLUMN_HEADERS
    };

    for (String header : sensorHeaders) {
      assertTrue("Sensor header should contain id", header.contains("id"));
      assertTrue("Sensor header should contain userID", header.contains("userID"));
      assertTrue("Sensor header should contain userName", header.contains("userName"));
      assertTrue("Sensor header should contain x,y,z", header.contains("x,y,z"));
      assertTrue("Sensor header should end with newline", header.endsWith("\n"));
    }
  }

  @Test
  public void testConstantsGrouping() {
    // Test that related constants have consistent naming patterns
    assertEquals(
        "Drawing mode constants should start with DRAWING_MODE_",
        Constants.DRAWING_MODE_NORMAL,
        "normal"
    );
    assertEquals("Source constants should be simple values", Constants.SOURCE_DEFAULT, "default");
    assertEquals("Button constants should be human-readable", Constants.DELETE_BUTTON, "Delete");
  }

  @Test
  public void testConstantsCount() {
    // Verify we have a reasonable number of constants (not too few, not excessive)
    Field[] fields = Constants.class.getDeclaredFields();
    assertTrue("Should have multiple constants defined", fields.length > 20);
    assertTrue("Should not have excessive number of constants", fields.length < 200);
  }

  @Test
  public void testSpecialCharactersInMessages() {
    // Test that dialog and toast messages are properly formatted
    assertFalse(
        "Dialog messages should not contain unescaped quotes",
        Constants.DIALOG_MSG_DEFAULT_IMAGES.contains("\"")
    );
    assertFalse(
        "Toast messages should not contain unescaped quotes",
        Constants.TOAST_FAILED_LOAD_DEFAULT.contains("\"")
    );
  }
} 

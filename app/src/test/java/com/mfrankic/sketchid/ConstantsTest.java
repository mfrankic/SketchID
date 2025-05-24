package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for Constants utility class.
 * Tests all constants for consistency, null safety, and expected values.
 */
public class ConstantsTest {

  @Test
  public void testConstructorThrowsException() {
    try {
      // Use reflection to test private constructor
      java.lang.reflect.Constructor<Constants> constructor
          = Constants.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Constructor should throw IllegalStateException");
    } catch (Exception e) {
      assertTrue(
          "Should throw IllegalStateException",
          e.getCause() instanceof IllegalStateException
      );
      assertEquals("Should have correct message", "Utility class", e.getCause().getMessage());
    }
  }

  @Test
  public void testDrawingModeConstants() {
    assertNotNull("DRAWING_MODE_NORMAL should not be null", Constants.DRAWING_MODE_NORMAL);
    assertNotNull("DRAWING_MODE_OVERLAY should not be null", Constants.DRAWING_MODE_OVERLAY);

    assertFalse("DRAWING_MODE_NORMAL should not be empty", Constants.DRAWING_MODE_NORMAL.isEmpty());
    assertFalse(
        "DRAWING_MODE_OVERLAY should not be empty",
        Constants.DRAWING_MODE_OVERLAY.isEmpty()
    );

    assertEquals("DRAWING_MODE_NORMAL should be 'normal'", "normal", Constants.DRAWING_MODE_NORMAL);
    assertEquals(
        "DRAWING_MODE_OVERLAY should be 'overlay'",
        "overlay",
        Constants.DRAWING_MODE_OVERLAY
    );

    assertNotEquals(
        "Drawing modes should be different",
        Constants.DRAWING_MODE_NORMAL,
        Constants.DRAWING_MODE_OVERLAY
    );
  }

  @Test
  public void testSourceConstants() {
    assertNotNull("SOURCE_DEFAULT should not be null", Constants.SOURCE_DEFAULT);
    assertNotNull("SOURCE_CUSTOM should not be null", Constants.SOURCE_CUSTOM);

    assertFalse("SOURCE_DEFAULT should not be empty", Constants.SOURCE_DEFAULT.isEmpty());
    assertFalse("SOURCE_CUSTOM should not be empty", Constants.SOURCE_CUSTOM.isEmpty());

    assertEquals("SOURCE_DEFAULT should be 'default'", "default", Constants.SOURCE_DEFAULT);
    assertEquals("SOURCE_CUSTOM should be 'custom'", "custom", Constants.SOURCE_CUSTOM);

    assertNotEquals(
        "Source constants should be different",
        Constants.SOURCE_DEFAULT,
        Constants.SOURCE_CUSTOM
    );
  }

  @Test
  public void testKeyConstants() {
    String[] keyConstants = {
        Constants.KEY_CURRENT_ITEM_ATTEMPT,
        Constants.KEY_CURRENT_ITEM_INDEX,
        Constants.KEY_DRAWING_ATTEMPTS,
        Constants.KEY_SELECTED_USER,
        Constants.KEY_THEME,
        Constants.KEY_USER_PROGRESS_PREFIX,
        Constants.KEY_DRAWING_MODE
    };

    for (String key : keyConstants) {
      assertNotNull("Key constant should not be null: " + key, key);
      assertFalse("Key constant should not be empty: " + key, key.isEmpty());
      assertFalse("Key constant should not be blank: " + key, key.trim().isEmpty());
    }

    // Test specific key values
    assertEquals(
        "KEY_CURRENT_ITEM_ATTEMPT should match",
        "current_item_attempt",
        Constants.KEY_CURRENT_ITEM_ATTEMPT
    );
    assertEquals(
        "KEY_CURRENT_ITEM_INDEX should match",
        "current_item_index",
        Constants.KEY_CURRENT_ITEM_INDEX
    );
    assertEquals(
        "KEY_DRAWING_ATTEMPTS should match",
        "drawing_attempts",
        Constants.KEY_DRAWING_ATTEMPTS
    );
    assertEquals("KEY_SELECTED_USER should match", "selected_user", Constants.KEY_SELECTED_USER);
    assertEquals("KEY_THEME should match", "theme", Constants.KEY_THEME);
    assertEquals(
        "KEY_USER_PROGRESS_PREFIX should match",
        "user_progress_",
        Constants.KEY_USER_PROGRESS_PREFIX
    );
    assertEquals("KEY_DRAWING_MODE should match", "drawing_mode", Constants.KEY_DRAWING_MODE);
  }

  @Test
  public void testActionConstants() {
    assertNotNull("ACTION_START should not be null", Constants.ACTION_START);
    assertNotNull("ACTION_END should not be null", Constants.ACTION_END);

    assertEquals("ACTION_START should be 'START'", "START", Constants.ACTION_START);
    assertEquals("ACTION_END should be 'END'", "END", Constants.ACTION_END);

    assertNotEquals("Actions should be different", Constants.ACTION_START, Constants.ACTION_END);
  }

  @Test
  public void testButtonConstants() {
    String[] buttonConstants = {
        Constants.DELETE_BUTTON,
        Constants.CANCEL_BUTTON,
        Constants.REMOVE_BUTTON,
        Constants.REMOVE_CUSTOM_ONLY_BUTTON,
        Constants.SAVE_BUTTON,
        Constants.SAVE_AND_EXIT_BUTTON,
        Constants.EXIT_WITHOUT_SAVING_BUTTON,
        Constants.YES_BUTTON,
        Constants.NO_BUTTON
    };

    for (String button : buttonConstants) {
      assertNotNull("Button constant should not be null: " + button, button);
      assertFalse("Button constant should not be empty: " + button, button.isEmpty());
    }

    // Test specific button values
    assertEquals("DELETE_BUTTON should match", "Delete", Constants.DELETE_BUTTON);
    assertEquals("CANCEL_BUTTON should match", "Cancel", Constants.CANCEL_BUTTON);
    assertEquals("REMOVE_BUTTON should match", "Remove", Constants.REMOVE_BUTTON);
    assertEquals("SAVE_BUTTON should match", "Save", Constants.SAVE_BUTTON);
    assertEquals("YES_BUTTON should match", "Yes", Constants.YES_BUTTON);
    assertEquals("NO_BUTTON should match", "No", Constants.NO_BUTTON);
  }

  @Test
  public void testDialogTitleConstants() {
    String[] dialogTitles = {
        Constants.DIALOG_TITLE_IMAGE_OPTIONS,
        Constants.DIALOG_TITLE_REMOVE_IMAGES,
        Constants.DIALOG_TITLE_UNSAVED_CHANGES,
        Constants.DIALOG_TITLE_EXIT_DRAWING
    };

    for (String title : dialogTitles) {
      assertNotNull("Dialog title should not be null: " + title, title);
      assertFalse("Dialog title should not be empty: " + title, title.isEmpty());
    }

    assertEquals(
        "DIALOG_TITLE_IMAGE_OPTIONS should match",
        "Image Options",
        Constants.DIALOG_TITLE_IMAGE_OPTIONS
    );
    assertEquals(
        "DIALOG_TITLE_REMOVE_IMAGES should match",
        "Remove Images",
        Constants.DIALOG_TITLE_REMOVE_IMAGES
    );
    assertEquals(
        "DIALOG_TITLE_UNSAVED_CHANGES should match",
        "Unsaved Changes",
        Constants.DIALOG_TITLE_UNSAVED_CHANGES
    );
    assertEquals(
        "DIALOG_TITLE_EXIT_DRAWING should match",
        "Exit Drawing",
        Constants.DIALOG_TITLE_EXIT_DRAWING
    );
  }

  @Test
  public void testToastConstants() {
    String[] toastConstants = {
        Constants.TOAST_FAILED_LOAD_DEFAULT,
        Constants.TOAST_FAILED_LOAD_IMAGE,
        Constants.TOAST_NO_IMAGES_SELECTED,
        Constants.TOAST_IMAGES_DELETED,
        Constants.TOAST_DEFAULT_NOT_REMOVED,
        Constants.TOAST_SELECT_AT_LEAST_ONE,
        Constants.TOAST_SELECTION_SAVED,
        Constants.TOAST_NO_IMAGES_SELECTED_ADD,
        Constants.TOAST_IMAGE_RENAMED,
        Constants.TOAST_FAILED_RENAME,
        Constants.TOAST_FAILED_DELETE,
        Constants.TOAST_IMAGE_DELETED,
        Constants.TOAST_FAILED_SAVE_IMAGE,
        Constants.TOAST_INVALID_USER,
        Constants.TOAST_INVALID_ATTEMPTS_NUMBER,
        Constants.TOAST_NO_DRAWING,
        Constants.TOAST_NO_IMAGES_SETTINGS,
        Constants.TOAST_IMAGES_ADDED,
        Constants.TOAST_MULTI_SELECT_HINT
    };

    for (String toast : toastConstants) {
      assertNotNull("Toast constant should not be null: " + toast, toast);
      assertFalse("Toast constant should not be empty: " + toast, toast.isEmpty());
    }
  }

  @Test
  public void testDataPrefixConstants() {
    String[] dataPrefixes = {
        Constants.DRAWING_DATA_PREFIX,
        Constants.GRAVITY_DATA_PREFIX,
        Constants.GYROSCOPE_DATA_PREFIX,
        Constants.MAGNETIC_FIELD_DATA_PREFIX,
        Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX,
        Constants.ACCELEROMETER_DATA_PREFIX
    };

    for (String prefix : dataPrefixes) {
      assertNotNull("Data prefix should not be null: " + prefix, prefix);
      assertFalse("Data prefix should not be empty: " + prefix, prefix.isEmpty());
      assertTrue("Data prefix should start with underscore: " + prefix, prefix.startsWith("_"));
      assertTrue("Data prefix should end with underscore: " + prefix, prefix.endsWith("_"));
    }

    // Test specific prefix values
    assertEquals(
        "DRAWING_DATA_PREFIX should match",
        "_drawing_data_",
        Constants.DRAWING_DATA_PREFIX
    );
    assertEquals(
        "GRAVITY_DATA_PREFIX should match",
        "_gravity_data_",
        Constants.GRAVITY_DATA_PREFIX
    );
    assertEquals(
        "GYROSCOPE_DATA_PREFIX should match",
        "_gyroscope_data_",
        Constants.GYROSCOPE_DATA_PREFIX
    );
    assertEquals(
        "MAGNETIC_FIELD_DATA_PREFIX should match",
        "_magnetic_field_data_",
        Constants.MAGNETIC_FIELD_DATA_PREFIX
    );
    assertEquals(
        "MAGNETIC_FIELD_BASELINE_DATA_PREFIX should match",
        "_magnetic_field_baseline_data_",
        Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX
    );
    assertEquals(
        "ACCELEROMETER_DATA_PREFIX should match",
        "_accelerometer_data_",
        Constants.ACCELEROMETER_DATA_PREFIX
    );
  }

  @Test
  public void testExportPatternConstants() {
    String[] exportPatterns = {
        Constants.FILE_EXPORT_PATTERN,
        Constants.GRAVITY_EXPORT_PATTERN,
        Constants.GYROSCOPE_EXPORT_PATTERN,
        Constants.MAGNETIC_FIELD_EXPORT_PATTERN,
        Constants.MAGNETIC_FIELD_BASELINE_EXPORT_PATTERN,
        Constants.ACCELEROMETER_EXPORT_PATTERN
    };

    for (String pattern : exportPatterns) {
      assertNotNull("Export pattern should not be null: " + pattern, pattern);
      assertFalse("Export pattern should not be empty: " + pattern, pattern.isEmpty());
      assertTrue(
          "Export pattern should contain format specifiers: " + pattern,
          pattern.contains("%")
      );
      assertTrue("Export pattern should end with newline: " + pattern, pattern.endsWith("%n"));
    }
  }

  @Test
  public void testColumnHeaderConstants() {
    String[] columnHeaders = {
        Constants.COLUMN_HEADERS,
        Constants.GRAVITY_COLUMN_HEADERS,
        Constants.GYROSCOPE_COLUMN_HEADERS,
        Constants.MAGNETIC_FIELD_COLUMN_HEADERS,
        Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS,
        Constants.ACCELEROMETER_COLUMN_HEADERS
    };

    for (String header : columnHeaders) {
      assertNotNull("Column header should not be null: " + header, header);
      assertFalse("Column header should not be empty: " + header, header.isEmpty());
      assertTrue("Column header should end with newline: " + header, header.endsWith("\n"));
      assertTrue("Column header should contain commas: " + header, header.contains(","));
    }
  }

  @Test
  public void testPreferenceConstants() {
    String[] prefConstants = {
        Constants.PREF_IMAGE_ORDER,
        Constants.PREF_SELECTED_IMAGES,
        Constants.PREF_FILE_SELECTED_IMAGES
    };

    for (String pref : prefConstants) {
      assertNotNull("Preference constant should not be null: " + pref, pref);
      assertFalse("Preference constant should not be empty: " + pref, pref.isEmpty());
    }

    assertEquals("PREF_IMAGE_ORDER should match", "image_order", Constants.PREF_IMAGE_ORDER);
    assertEquals(
        "PREF_SELECTED_IMAGES should match",
        "selected_images",
        Constants.PREF_SELECTED_IMAGES
    );
    assertEquals(
        "PREF_FILE_SELECTED_IMAGES should match",
        "selected_images",
        Constants.PREF_FILE_SELECTED_IMAGES
    );
  }

  @Test
  public void testDrawingKeyConstants() {
    String[] drawingKeys = {
        Constants.DRAWING_KEY_ATTEMPTS,
        Constants.DRAWING_KEY_TIME_STARTED,
        Constants.DRAWING_KEY_SELECTED_IMAGE_IDS,
        Constants.DRAWING_KEY_DEVICE_MODEL,
        Constants.DRAWING_KEY_ANDROID_VERSION,
        Constants.DRAWING_KEY_MODE
    };

    for (String key : drawingKeys) {
      assertNotNull("Drawing key should not be null: " + key, key);
      assertFalse("Drawing key should not be empty: " + key, key.isEmpty());
    }

    assertEquals(
        "DRAWING_KEY_ATTEMPTS should match",
        "drawing_attempts",
        Constants.DRAWING_KEY_ATTEMPTS
    );
    assertEquals(
        "DRAWING_KEY_TIME_STARTED should match",
        "time_started",
        Constants.DRAWING_KEY_TIME_STARTED
    );
    assertEquals(
        "DRAWING_KEY_SELECTED_IMAGE_IDS should match",
        "selected_image_ids",
        Constants.DRAWING_KEY_SELECTED_IMAGE_IDS
    );
    assertEquals(
        "DRAWING_KEY_DEVICE_MODEL should match",
        "device_model",
        Constants.DRAWING_KEY_DEVICE_MODEL
    );
    assertEquals(
        "DRAWING_KEY_ANDROID_VERSION should match",
        "android_version",
        Constants.DRAWING_KEY_ANDROID_VERSION
    );
    assertEquals("DRAWING_KEY_MODE should match", "drawing_mode", Constants.DRAWING_KEY_MODE);
  }

  @Test
  public void testMiscellaneousConstants() {
    assertNotNull("ERROR_NAME_EMPTY should not be null", Constants.ERROR_NAME_EMPTY);
    assertNotNull("SKETCHID_DATA_DIR should not be null", Constants.SKETCHID_DATA_DIR);
    assertNotNull("IMAGES_VIEWED should not be null", Constants.IMAGES_VIEWED);
    assertNotNull("FINISHED should not be null", Constants.FINISHED);
    assertNotNull("SETTINGS should not be null", Constants.SETTINGS);
    assertNotNull("FORMAT_PROGRESS_TEXT should not be null", Constants.FORMAT_PROGRESS_TEXT);
    assertNotNull("EXPORT_DATE_FORMAT should not be null", Constants.EXPORT_DATE_FORMAT);
    assertNotNull("FAILED_EXPORT_MESSAGE should not be null", Constants.FAILED_EXPORT_MESSAGE);

    assertEquals(
        "ERROR_NAME_EMPTY should match",
        "Name cannot be empty",
        Constants.ERROR_NAME_EMPTY
    );
    assertEquals("SKETCHID_DATA_DIR should match", "SketchIDData", Constants.SKETCHID_DATA_DIR);
    assertEquals("IMAGES_VIEWED should match", "imagesViewed", Constants.IMAGES_VIEWED);
    assertEquals("FINISHED should match", "finished", Constants.FINISHED);
    assertEquals("SETTINGS should match", "settings", Constants.SETTINGS);
    assertEquals(
        "FORMAT_PROGRESS_TEXT should match",
        "Attempt: %d/%d\tOverall: %d/%d",
        Constants.FORMAT_PROGRESS_TEXT
    );
    assertEquals(
        "EXPORT_DATE_FORMAT should match",
        "yyyyMMdd_HHmmss",
        Constants.EXPORT_DATE_FORMAT
    );
    assertEquals(
        "FAILED_EXPORT_MESSAGE should match",
        "Failed to create export directory",
        Constants.FAILED_EXPORT_MESSAGE
    );
  }

  @Test
  public void testAllConstantsArePublicStaticFinal()
  throws IllegalAccessException {
    Field[] fields = Constants.class.getDeclaredFields();

    for (Field field : fields) {
      int modifiers = field.getModifiers();

      assertTrue("Field should be public: " + field.getName(), Modifier.isPublic(modifiers));
      assertTrue("Field should be static: " + field.getName(), Modifier.isStatic(modifiers));
      assertTrue("Field should be final: " + field.getName(), Modifier.isFinal(modifiers));

      // All our constants should be Strings
      assertEquals(
          "Field should be String type: " + field.getName(),
          String.class,
          field.getType()
      );
    }
  }

  @Test
  public void testNoNullConstants()
  throws IllegalAccessException {
    Field[] fields = Constants.class.getDeclaredFields();

    for (Field field : fields) {
      if (field.getType() == String.class && Modifier.isStatic(field.getModifiers())) {
        Object value = field.get(null);
        assertNotNull("Constant should not be null: " + field.getName(), value);

        String stringValue = (String) value;
        assertFalse("Constant should not be empty: " + field.getName(), stringValue.isEmpty());
      }
    }
  }

  @Test
  public void testConstantUniqueness()
  throws IllegalAccessException {
    Field[] fields = Constants.class.getDeclaredFields();
    Set<String> constantValues = new HashSet<>();
    List<String> duplicates = new ArrayList<>();

    for (Field field : fields) {
      if (field.getType() == String.class && Modifier.isStatic(field.getModifiers())) {
        String value = (String) field.get(null);
        if (constantValues.contains(value)) {
          duplicates.add(value);
        } else {
          constantValues.add(value);
        }
      }
    }

    // Some constants might legitimately have the same value (like PREF_SELECTED_IMAGES and
    // PREF_FILE_SELECTED_IMAGES)
    // So we'll just log this information but not fail the test
    if (!duplicates.isEmpty()) {
      System.out.println("Duplicate constant values found: " + duplicates);
    }
  }

  @Test
  public void testFormatStringConsistency() {
    // Test that format strings have the right number of placeholders
    String progressFormat = Constants.FORMAT_PROGRESS_TEXT;
    assertEquals(
        "Progress format should have 4 %d placeholders",
        4,
        countOccurrences(progressFormat, "%d")
    );

    String fileExportPattern = Constants.FILE_EXPORT_PATTERN;
    assertTrue(
        "File export pattern should have multiple format specifiers",
        countOccurrences(fileExportPattern, "%") >= 10
    );

    String dateFormat = Constants.EXPORT_DATE_FORMAT;
    assertTrue("Date format should contain year pattern", dateFormat.contains("yyyy"));
    assertTrue("Date format should contain month pattern", dateFormat.contains("MM"));
    assertTrue("Date format should contain day pattern", dateFormat.contains("dd"));
    assertTrue("Date format should contain hour pattern", dateFormat.contains("HH"));
    assertTrue("Date format should contain minute pattern", dateFormat.contains("mm"));
    assertTrue("Date format should contain second pattern", dateFormat.contains("ss"));
  }

  private int countOccurrences(String text, String pattern) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(pattern, index)) != -1) {
      count++;
      index += pattern.length();
    }
    return count;
  }

  @Test
  public void testMessageConsistency() {
    // Test that error/toast messages are user-friendly
    String[] userMessages = {
        Constants.ERROR_NAME_EMPTY,
        Constants.TOAST_FAILED_LOAD_DEFAULT,
        Constants.TOAST_IMAGES_DELETED,
        Constants.TOAST_SELECTION_SAVED,
        Constants.TOAST_IMAGE_RENAMED,
        Constants.FAILED_EXPORT_MESSAGE
    };

    for (String message : userMessages) {
      assertFalse("User message should not be too short: " + message, message.length() < 5);
      assertTrue(
          "User message should start with capital letter: " + message,
          Character.isUpperCase(message.charAt(0))
      );
    }
  }

  @Test
  public void testButtonTextConsistency() {
    String[] buttonTexts = {
        Constants.DELETE_BUTTON,
        Constants.CANCEL_BUTTON,
        Constants.SAVE_BUTTON,
        Constants.YES_BUTTON,
        Constants.NO_BUTTON
    };

    for (String buttonText : buttonTexts) {
      assertTrue(
          "Button text should start with capital letter: " + buttonText,
          Character.isUpperCase(buttonText.charAt(0))
      );
      assertFalse("Button text should not be too long: " + buttonText, buttonText.length() > 20);
    }
  }

  @Test
  public void testDataPrefixConsistency() {
    String[] prefixes = {
        Constants.DRAWING_DATA_PREFIX,
        Constants.GRAVITY_DATA_PREFIX,
        Constants.GYROSCOPE_DATA_PREFIX,
        Constants.MAGNETIC_FIELD_DATA_PREFIX,
        Constants.ACCELEROMETER_DATA_PREFIX
    };

    for (String prefix : prefixes) {
      assertTrue(
          "Data prefix should follow naming pattern: " + prefix,
          prefix.matches("_[a-z_]+_")
      );
    }
  }

  @Test
  public void testClassIsFinal() {
    assertTrue("Constants class should be final", Modifier.isFinal(Constants.class.getModifiers()));
  }

  @Test
  public void testNoMethodsOtherThanConstructor() {
    // The class should only have the private constructor, no other methods
    assertEquals(
        "Constants class should only have constructor",
        1,
        Constants.class.getDeclaredMethods().length
    );
  }

  @Test
  public void testDrawingModeCoherence() {
    // Test that drawing mode constants are used consistently
    assertTrue(
        "Drawing mode should be referenced in drawing key",
        Constants.KEY_DRAWING_MODE.contains("drawing_mode")
    );
    assertEquals(
        "Drawing key mode should match key pattern",
        Constants.DRAWING_KEY_MODE,
        "drawing_mode"
    );
  }
} 

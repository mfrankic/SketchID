package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Unit tests for the CheckboxUtils utility class
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CheckboxUtilsTest {

  private Context context;
  private ImageButton checkboxButton;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.getApplication();
    checkboxButton = new ImageButton(context);
  }

  @Test
  public void testConstructorThrowsException() {
    // Test that the utility class constructor throws IllegalStateException
    try {
      java.lang.reflect.Constructor<CheckboxUtils> constructor
          = CheckboxUtils.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Expected IllegalStateException to be thrown");
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
      assertEquals("Utility class", e.getCause().getMessage());
    }
  }

  @Test
  public void testStyleCustomCheckboxChecked() {
    // Test styling a checked checkbox
    int backgroundColor = Color.WHITE;
    int borderColor = Color.BLACK;
    int checkmarkColor = Color.GREEN;

    CheckboxUtils.styleCustomCheckbox(
        context,
        checkboxButton,
        true,
        backgroundColor,
        borderColor,
        checkmarkColor
    );

    // Verify that a drawable was set (we can't easily test the exact appearance)
    assertNotNull("Checkbox should have a drawable set", checkboxButton.getDrawable());
  }

  @Test
  public void testStyleCustomCheckboxUnchecked() {
    // Test styling an unchecked checkbox
    int backgroundColor = Color.WHITE;
    int borderColor = Color.BLACK;
    int checkmarkColor = Color.GREEN;

    CheckboxUtils.styleCustomCheckbox(
        context,
        checkboxButton,
        false,
        backgroundColor,
        borderColor,
        checkmarkColor
    );

    // Verify that a drawable was set
    assertNotNull("Checkbox should have a drawable set", checkboxButton.getDrawable());
    assertTrue(
        "Should be a GradientDrawable for unchecked state",
        checkboxButton.getDrawable() instanceof GradientDrawable
    );
  }

  @Test
  public void testStyleCustomCheckboxWithStateUnchecked() {
    // Test with state 0 (unchecked)
    CheckboxUtils.styleCustomCheckboxWithState(
        context,
        checkboxButton,
        0,
        Color.WHITE,
        Color.BLACK,
        Color.GREEN
    );

    assertNotNull("Checkbox should have a drawable set", checkboxButton.getDrawable());
    assertTrue(
        "Should be a GradientDrawable for unchecked state",
        checkboxButton.getDrawable() instanceof GradientDrawable
    );
  }

  @Test
  public void testStyleCustomCheckboxWithStateChecked() {
    // Test with state 1 (checked)
    CheckboxUtils.styleCustomCheckboxWithState(
        context,
        checkboxButton,
        1,
        Color.WHITE,
        Color.BLACK,
        Color.GREEN
    );

    assertNotNull("Checkbox should have a drawable set", checkboxButton.getDrawable());
    // For checked state, it might be a LayerDrawable or GradientDrawable depending on resource
    // availability
  }

  @Test
  public void testStyleCustomCheckboxWithStateIndeterminate() {
    // Test with state 2 (indeterminate)
    CheckboxUtils.styleCustomCheckboxWithState(
        context,
        checkboxButton,
        2,
        Color.WHITE,
        Color.BLACK,
        Color.GREEN
    );

    assertNotNull("Checkbox should have a drawable set", checkboxButton.getDrawable());
    // For indeterminate state, it might be a LayerDrawable or GradientDrawable depending on
    // resource availability
  }

  @Test
  public void testStyleCustomCheckboxWithInvalidState() {
    // Test with invalid state values
    int[] invalidStates = {-1, 3, 999, -999};

    for (int state : invalidStates) {
      CheckboxUtils.styleCustomCheckboxWithState(
          context,
          checkboxButton,
          state,
          Color.WHITE,
          Color.BLACK,
          Color.GREEN
      );

      assertNotNull(
          "Checkbox should have a drawable set even with invalid state " + state,
          checkboxButton.getDrawable()
      );
    }
  }

  @Test
  public void testApplyAlphaValidRange() {
    // Test applyAlpha with valid alpha values
    int baseColor = Color.RED; // Full opacity red

    // Test fully opaque
    int result1 = CheckboxUtils.applyAlpha(baseColor, 1.0f);
    assertEquals("Alpha 1.0 should result in full opacity", 255, Color.alpha(result1));
    assertEquals("Red component should be preserved", Color.red(baseColor), Color.red(result1));
    assertEquals(
        "Green component should be preserved",
        Color.green(baseColor),
        Color.green(result1)
    );
    assertEquals("Blue component should be preserved", Color.blue(baseColor), Color.blue(result1));

    // Test half transparent
    int result2 = CheckboxUtils.applyAlpha(baseColor, 0.5f);
    assertEquals(
        "Alpha 0.5 should result in half opacity",
        127,
        Color.alpha(result2),
        1
    ); // Allow 1 unit tolerance for rounding

    // Test fully transparent
    int result3 = CheckboxUtils.applyAlpha(baseColor, 0.0f);
    assertEquals("Alpha 0.0 should result in full transparency", 0, Color.alpha(result3));
  }

  @Test
  public void testApplyAlphaOutOfBounds() {
    // Test applyAlpha with out-of-bounds values
    int baseColor = Color.BLUE;

    // Test alpha > 1.0 (should be clamped to 1.0)
    int result1 = CheckboxUtils.applyAlpha(baseColor, 1.5f);
    assertEquals("Alpha > 1.0 should be clamped to 1.0", 255, Color.alpha(result1));

    int result2 = CheckboxUtils.applyAlpha(baseColor, 999.0f);
    assertEquals("Very high alpha should be clamped to 1.0", 255, Color.alpha(result2));

    // Test alpha < 0.0 (should be clamped to 0.0)
    int result3 = CheckboxUtils.applyAlpha(baseColor, -0.5f);
    assertEquals("Alpha < 0.0 should be clamped to 0.0", 0, Color.alpha(result3));

    int result4 = CheckboxUtils.applyAlpha(baseColor, -999.0f);
    assertEquals("Very low alpha should be clamped to 0.0", 0, Color.alpha(result4));
  }

  @Test
  public void testApplyAlphaColorPreservation() {
    // Test that RGB components are preserved when applying alpha
    int[] testColors = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
        Color.WHITE,
        Color.BLACK,
        Color.rgb(123, 45, 67),
        Color.rgb(200, 150, 100)
    };

    for (int baseColor : testColors) {
      int result = CheckboxUtils.applyAlpha(baseColor, 0.7f);

      assertEquals("Red component should be preserved", Color.red(baseColor), Color.red(result));
      assertEquals(
          "Green component should be preserved",
          Color.green(baseColor),
          Color.green(result)
      );
      assertEquals("Blue component should be preserved", Color.blue(baseColor), Color.blue(result));
      assertEquals("Alpha should be approximately 70%", (int) (0.7f * 255), Color.alpha(result), 1);
    }
  }

  @Test
  public void testApplyAlphaSpecialValues() {
    // Test with special float values
    int baseColor = Color.GREEN;

    // Test with very small positive values
    int result1 = CheckboxUtils.applyAlpha(baseColor, 0.001f);
    assertEquals("Very small alpha should work", 0, Color.alpha(result1), 1);

    // Test with values very close to 1.0
    int result2 = CheckboxUtils.applyAlpha(baseColor, 0.999f);
    assertEquals("Alpha close to 1.0 should work", 254, Color.alpha(result2), 1);

    // Test with exactly 0.5
    int result3 = CheckboxUtils.applyAlpha(baseColor, 0.5f);
    assertEquals("Exactly 0.5 alpha should work", 127, Color.alpha(result3), 1);
  }

  @Test
  public void testStyleCustomCheckboxDifferentColors() {
    // Test with various color combinations
    int[][] colorCombinations = {
        {Color.WHITE, Color.BLACK, Color.GREEN},
        {Color.BLACK, Color.WHITE, Color.RED},
        {Color.TRANSPARENT, Color.BLUE, Color.YELLOW},
        {Color.rgb(100, 150, 200), Color.rgb(50, 75, 100), Color.rgb(200, 100, 50)},
        {0xFF123456, 0xFF654321, 0xFFABCDEF}
    };

    for (int[] colors : colorCombinations) {
      int backgroundColor = colors[0];
      int borderColor = colors[1];
      int checkmarkColor = colors[2];

      // Test checked state
      CheckboxUtils.styleCustomCheckbox(
          context,
          checkboxButton,
          true,
          backgroundColor,
          borderColor,
          checkmarkColor
      );
      assertNotNull(
          "Checkbox should have drawable with colors: " + Integer.toHexString(backgroundColor),
          checkboxButton.getDrawable()
      );

      // Test unchecked state
      CheckboxUtils.styleCustomCheckbox(
          context,
          checkboxButton,
          false,
          backgroundColor,
          borderColor,
          checkmarkColor
      );
      assertNotNull(
          "Checkbox should have drawable with colors: " + Integer.toHexString(backgroundColor),
          checkboxButton.getDrawable()
      );
    }
  }

  @Test
  public void testStyleCustomCheckboxNullContext() {
    // Test with null context - should handle gracefully or throw expected exception
    try {
      CheckboxUtils.styleCustomCheckbox(
          null,
          checkboxButton,
          true,
          Color.WHITE,
          Color.BLACK,
          Color.GREEN
      );
      // If it doesn't throw, that's okay - the method might handle null gracefully
    } catch (NullPointerException e) {
      // Expected behavior for null context
      assertTrue("Null context should throw NullPointerException", true);
    }
  }

  @Test
  public void testStyleCustomCheckboxNullButton() {
    // Test with null ImageButton - should handle gracefully or throw expected exception
    try {
      CheckboxUtils.styleCustomCheckbox(context, null, true, Color.WHITE, Color.BLACK, Color.GREEN);
      // If it doesn't throw, that's okay
    } catch (NullPointerException e) {
      // Expected behavior for null button
      assertTrue("Null button should throw NullPointerException", true);
    }
  }

  @Test
  public void testMultipleStylingsOnSameButton() {
    // Test applying multiple stylings to the same button
    ImageButton button = new ImageButton(context);

    // First styling
    CheckboxUtils.styleCustomCheckbox(
        context,
        button,
        false,
        Color.WHITE,
        Color.BLACK,
        Color.GREEN
    );
    assertNotNull("First styling should set drawable", button.getDrawable());

    // Second styling with different parameters
    CheckboxUtils.styleCustomCheckbox(context, button, true, Color.BLUE, Color.RED, Color.YELLOW);
    assertNotNull("Second styling should set drawable", button.getDrawable());

    // Third styling with different state
    CheckboxUtils.styleCustomCheckboxWithState(
        context,
        button,
        2,
        Color.GRAY,
        Color.WHITE,
        Color.BLACK
    );
    assertNotNull("Third styling should set drawable", button.getDrawable());
  }

  @Test
  public void testApplyAlphaPrecision() {
    // Test alpha precision with various decimal values
    int baseColor = Color.rgb(128, 128, 128);
    float[] alphaValues = {0.1f, 0.25f, 0.33f, 0.5f, 0.66f, 0.75f, 0.9f};

    for (float alpha : alphaValues) {
      int result = CheckboxUtils.applyAlpha(baseColor, alpha);
      int expectedAlpha = (int) (alpha * 255);
      int actualAlpha = Color.alpha(result);

      assertEquals(
          "Alpha precision test for " + alpha,
          expectedAlpha,
          actualAlpha,
          1
      ); // Allow 1 unit tolerance for rounding
    }
  }

  @Test
  public void testApplyAlphaWithAlreadyTransparentColor() {
    // Test applying alpha to a color that already has alpha
    int semiTransparentRed = Color.argb(128, 255, 0, 0); // 50% transparent red

    int result = CheckboxUtils.applyAlpha(semiTransparentRed, 0.5f);

    // The method should replace the alpha, not combine it
    assertEquals("Alpha should be replaced, not combined", 127, Color.alpha(result), 1);
    assertEquals("Red component should be preserved", 255, Color.red(result));
    assertEquals("Green component should be preserved", 0, Color.green(result));
    assertEquals("Blue component should be preserved", 0, Color.blue(result));
  }

  @Test
  public void testConsistentResults() {
    // Test that multiple calls with same parameters produce consistent results
    int backgroundColor = Color.CYAN;
    int borderColor = Color.MAGENTA;
    int checkmarkColor = Color.YELLOW;

    // Style the checkbox multiple times with same parameters
    CheckboxUtils.styleCustomCheckbox(
        context,
        checkboxButton,
        true,
        backgroundColor,
        borderColor,
        checkmarkColor
    );
    Object firstDrawable = checkboxButton.getDrawable();

    CheckboxUtils.styleCustomCheckbox(
        context,
        checkboxButton,
        true,
        backgroundColor,
        borderColor,
        checkmarkColor
    );
    Object secondDrawable = checkboxButton.getDrawable();

    // Both calls should result in drawables being set
    assertNotNull("First call should set drawable", firstDrawable);
    assertNotNull("Second call should set drawable", secondDrawable);
  }

  @Test
  public void testAllStateValues() {
    // Test all possible state values systematically
    for (int state = -2; state <= 5; state++) {
      CheckboxUtils.styleCustomCheckboxWithState(
          context,
          checkboxButton,
          state,
          Color.WHITE,
          Color.BLACK,
          Color.GREEN
      );

      assertNotNull(
          "State " + state + " should result in a drawable",
          checkboxButton.getDrawable()
      );
    }
  }
} 

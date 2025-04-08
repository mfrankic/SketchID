package com.mfrankic.sketchid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.widget.ImageButton;

import androidx.core.content.res.ResourcesCompat;

/**
 * Utility class to help with checkbox styling in different contexts
 */
public class CheckboxUtils {

  private CheckboxUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Applies a semi-transparent white background to a checkbox with app theme colors
   *
   * @param context        The application context
   * @param checkboxButton The ImageButton used as a checkbox
   * @param isChecked      Whether the checkbox is checked
   * @param alpha          The alpha value (0.0-1.0) for background transparency
   */
  public static void styleCheckboxForImageOverlay(
      Context context,
      ImageButton checkboxButton,
      boolean isChecked,
      float alpha
  ) {
    // Get colors from the theme
    TypedArray typedArray = context.obtainStyledAttributes(new int[]{
        R.attr.checkboxBorderColor, R.attr.checkboxCheckmarkColor, R.attr.checkboxBackgroundColor
    });

    int borderColor = typedArray.getColor(0, Color.BLUE); // Default to blue if not found
    int checkmarkColor = typedArray.getColor(1, Color.WHITE); // Default to white if not found
    int backgroundColor = typedArray.getColor(2, Color.TRANSPARENT); // Default to transparent

    typedArray.recycle();

    // If background color is transparent, use a semi-transparent white
    if (Color.alpha(backgroundColor) == 0) {
      backgroundColor = Color.BLACK;
      backgroundColor = applyAlpha(backgroundColor, alpha);
    }

    // Apply the styling
    styleCustomCheckbox(
        context,
        checkboxButton,
        isChecked,
        backgroundColor,
        borderColor,
        checkmarkColor
    );
  }

  /**
   * Applies a custom styled checkbox to an ImageButton
   *
   * @param context         The application context
   * @param checkboxButton  The ImageButton to style as a checkbox
   * @param isChecked       Whether the checkbox is checked (true = checked, false = unchecked)
   * @param backgroundColor The background color (can include alpha for transparency)
   * @param borderColor     The color for the checkbox border
   * @param checkmarkColor  The color for the checkmark
   */
  public static void styleCustomCheckbox(
      Context context,
      ImageButton checkboxButton,
      boolean isChecked,
      int backgroundColor,
      int borderColor,
      int checkmarkColor
  ) {
    styleCustomCheckboxWithState(
        context,
        checkboxButton,
        isChecked ? 1 : 0,
        backgroundColor,
        borderColor,
        checkmarkColor
    );
  }

  /**
   * Applies a custom styled checkbox to an ImageButton with support for indeterminate state
   *
   * @param context         The application context
   * @param checkboxButton  The ImageButton to style as a checkbox
   * @param state           The checkbox state: 0 = unchecked, 1 = checked, 2 = indeterminate
   * @param backgroundColor The background color (can include alpha for transparency)
   * @param borderColor     The color for the checkbox border
   * @param checkmarkColor  The color for the checkmark
   */
  public static void styleCustomCheckboxWithState(
      Context context,
      ImageButton checkboxButton,
      int state,
      int backgroundColor,
      int borderColor,
      int checkmarkColor
  ) {

    // Border width in dp
    float borderWidthDp = 2f;
    float borderWidth = context.getResources().getDisplayMetrics().density * borderWidthDp;

    // Corner radius in dp
    float cornerRadiusDp = 2f;
    float cornerRadius = context.getResources().getDisplayMetrics().density * cornerRadiusDp;

    // Create background with border
    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setColor(backgroundColor);
    background.setStroke((int) borderWidth, borderColor);
    background.setCornerRadius(cornerRadius);

    if (state > 0) {
      // Checked or indeterminate state, get the appropriate drawable
      try {
        int drawableRes = (state == 1) ? R.drawable.checkmark : R.drawable.indeterminate_minus;

        Drawable checkmark = ResourcesCompat.getDrawable(
            context.getResources(),
            drawableRes,
            context.getTheme()
        );

        // Try to set the checkmark color if it's a VectorDrawable
        if (checkmark instanceof VectorDrawable) {
          checkmark.setTint(checkmarkColor);
        }

        // Create a layer drawable with the background and the checkmark
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{background, checkmark});

        // Set it to the button
        checkboxButton.setImageDrawable(layerDrawable);
      } catch (Exception e) {
        // Fallback if the checkmark drawable can't be loaded
        checkboxButton.setImageDrawable(background);
      }
    } else {
      // For unchecked state, just use the background
      checkboxButton.setImageDrawable(background);
    }
  }

  /**
   * Creates a semi-transparent color with alpha value in 0-1 range
   *
   * @param baseColor The base color to apply transparency to
   * @param alpha     Alpha value between 0.0 (fully transparent) and 1.0 (fully opaque)
   * @return Color with alpha applied
   */
  public static int applyAlpha(int baseColor, float alpha) {
    // Ensure alpha is between 0 and 1
    alpha = Math.max(0f, Math.min(1f, alpha));

    // Convert to 0-255 range for Color.argb
    int alphaInt = (int) (alpha * 255);

    // Extract color components
    int red = Color.red(baseColor);
    int green = Color.green(baseColor);
    int blue = Color.blue(baseColor);

    // Create new color with specified alpha
    return Color.argb(alphaInt, red, green, blue);
  }

  /**
   * Gets the currently configured checkbox background color from the theme
   */
  public static int getCheckboxBackgroundColor(Context context) {
    TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.checkboxBackgroundColor});
    int color = a.getColor(0, Color.TRANSPARENT);
    a.recycle();
    return color;
  }
} 

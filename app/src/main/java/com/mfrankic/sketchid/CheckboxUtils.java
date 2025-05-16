package com.mfrankic.sketchid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

/**
 * Utility class to help with checkbox styling in different contexts
 */
public class CheckboxUtils {

  private CheckboxUtils() {
    throw new IllegalStateException("Utility class");
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
    GradientDrawable background = getGradientDrawable(context, backgroundColor, borderColor);

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

  @NonNull
  private static GradientDrawable getGradientDrawable(
      Context context,
      int backgroundColor,
      int borderColor
  ) {
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
    return background;
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

}

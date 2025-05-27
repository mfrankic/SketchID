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
 * Utility class for creating and applying custom styles to checkbox-like {@link ImageButton} views.
 * This class provides methods to render checkboxes with different states (checked, unchecked,
 * indeterminate)
 * and customizable colors for background, border, and checkmark.
 * This class is not meant to be instantiated.
 */
public class CheckboxUtils {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private CheckboxUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Applies a custom style to an {@link ImageButton} to make it look like a checkbox.
   *
   * @param context         The application context.
   * @param checkboxButton  The {@link ImageButton} to be styled.
   * @param isChecked       {@code true} if the checkbox should be displayed as checked, {@code
   *                        false} otherwise.
   * @param backgroundColor The background color of the checkbox (can include alpha for
   *                        transparency).
   * @param borderColor     The color of the checkbox border.
   * @param checkmarkColor  The color of the checkmark icon.
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
        context, checkboxButton, isChecked ? 1 : 0, // 1 for checked, 0 for unchecked
        backgroundColor, borderColor, checkmarkColor
    );
  }

  /**
   * Applies a custom style to an {@link ImageButton} to make it look like a checkbox,
   * with support for an indeterminate state.
   *
   * @param context         The application context.
   * @param checkboxButton  The {@link ImageButton} to be styled.
   * @param state           The state of the checkbox:
   *                        0 for unchecked,
   *                        1 for checked,
   *                        2 for indeterminate.
   * @param backgroundColor The background color of the checkbox (can include alpha for
   *                        transparency).
   * @param borderColor     The color of the checkbox border.
   * @param checkmarkColor  The color of the checkmark or indeterminate icon.
   */
  public static void styleCustomCheckboxWithState(
      Context context,
      ImageButton checkboxButton,
      int state,
      // 0 = unchecked, 1 = checked, 2 = indeterminate
      int backgroundColor,
      int borderColor,
      int checkmarkColor
  ) {

    GradientDrawable background = getGradientDrawable(context, backgroundColor, borderColor);

    if (state > 0) { // Checked or indeterminate
      try {
        int drawableRes = (state == 1) ? R.drawable.checkmark : R.drawable.indeterminate_minus;

        Drawable checkmark = ResourcesCompat.getDrawable(
            context.getResources(),
            drawableRes,
            context.getTheme()
        );

        if (checkmark instanceof VectorDrawable) {
          checkmark.setTint(checkmarkColor);
        }

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{background, checkmark});

        checkboxButton.setImageDrawable(layerDrawable);
      } catch (Exception e) {
        // Fallback to just background if checkmark loading fails
        checkboxButton.setImageDrawable(background);
      }
    } else { // Unchecked
      checkboxButton.setImageDrawable(background);
    }
  }

  /**
   * Creates a {@link GradientDrawable} to be used as the background for the custom checkbox.
   *
   * @param context         The application context.
   * @param backgroundColor The background color.
   * @param borderColor     The border color.
   * @return A {@link GradientDrawable} with the specified background and border colors, and
   * rounded corners.
   */
  @NonNull
  private static GradientDrawable getGradientDrawable(
      Context context,
      int backgroundColor,
      int borderColor
  ) {
    float borderWidthDp = 2f;
    float borderWidth = context.getResources().getDisplayMetrics().density * borderWidthDp;

    float cornerRadiusDp = 2f;
    float cornerRadius = context.getResources().getDisplayMetrics().density * cornerRadiusDp;

    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setColor(backgroundColor);
    background.setStroke((int) borderWidth, borderColor);
    background.setCornerRadius(cornerRadius);
    return background;
  }

  /**
   * Applies an alpha transparency value to a base color.
   *
   * @param baseColor The base color (e.g., {@code Color.RED}).
   * @param alpha     The alpha value, ranging from 0.0 (fully transparent) to 1.0 (fully opaque).
   *                  Values outside this range will be clamped.
   * @return The new color integer with the alpha component applied.
   */
  public static int applyAlpha(int baseColor, float alpha) {
    // Clamp alpha to the valid range [0, 1]
    alpha = Math.max(0f, Math.min(1f, alpha));

    int alphaInt = (int) (alpha * 255);

    int red = Color.red(baseColor);
    int green = Color.green(baseColor);
    int blue = Color.blue(baseColor);

    return Color.argb(alphaInt, red, green, blue);
  }

}

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

    GradientDrawable background = getGradientDrawable(context, backgroundColor, borderColor);

    if (state > 0) {
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
        checkboxButton.setImageDrawable(background);
      }
    } else {
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
   * Creates a semi-transparent color with alpha value in 0-1 range
   *
   * @param baseColor The base color to apply transparency to
   * @param alpha     Alpha value between 0.0 (fully transparent) and 1.0 (fully opaque)
   * @return Color with alpha applied
   */
  public static int applyAlpha(int baseColor, float alpha) {

    alpha = Math.max(0f, Math.min(1f, alpha));

    int alphaInt = (int) (alpha * 255);

    int red = Color.red(baseColor);
    int green = Color.green(baseColor);
    int blue = Color.blue(baseColor);

    return Color.argb(alphaInt, red, green, blue);
  }

}

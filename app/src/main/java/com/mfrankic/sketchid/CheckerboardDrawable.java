package com.mfrankic.sketchid;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom drawable that displays a checkerboard pattern, commonly used
 * to indicate transparency in images.
 */
public class CheckerboardDrawable extends Drawable {
  private final Paint lightPaint;
  private final Paint darkPaint;
  private final int cellSize;

  /**
   * Create a new checkerboard drawable
   *
   * @param lightColor The light color to use
   * @param darkColor  The dark color to use
   * @param cellSize   The size of each checkerboard cell in pixels
   */
  public CheckerboardDrawable(int lightColor, int darkColor, int cellSize) {
    this.cellSize = cellSize;
    lightPaint = new Paint();
    lightPaint.setColor(lightColor);
    lightPaint.setStyle(Paint.Style.FILL);

    darkPaint = new Paint();
    darkPaint.setColor(darkColor);
    darkPaint.setStyle(Paint.Style.FILL);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    int width = getBounds().width();
    int height = getBounds().height();

    canvas.drawRect(0, 0, width, height, lightPaint);

    boolean isLightRow = true;
    for (int y = 0; y < height; y += cellSize) {
      boolean isLightCell = isLightRow;
      for (int x = 0; x < width; x += cellSize) {
        if (!isLightCell) {
          canvas.drawRect(x, y, (x + cellSize), (y + cellSize), darkPaint);
        }
        isLightCell = !isLightCell;
      }
      isLightRow = !isLightRow;
    }
  }

  @Override
  public void setAlpha(int alpha) {
    lightPaint.setAlpha(alpha);
    darkPaint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    lightPaint.setColorFilter(colorFilter);
    darkPaint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }
} 

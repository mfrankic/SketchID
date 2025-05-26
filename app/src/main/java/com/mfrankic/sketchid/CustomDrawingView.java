package com.mfrankic.sketchid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class CustomDrawingView extends View {

  private final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
  private Path drawPath;
  private Paint drawPaint;
  private Paint canvasPaint;
  private Bitmap canvasBitmap;
  private Canvas drawCanvas;
  private OnStrokeListener onStrokeListener;

  public CustomDrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setupDrawing();
  }

  private void setupDrawing() {
    drawPath = new Path();
    drawPaint = new Paint();
    drawPaint.setColor(ContextCompat.getColor(getContext(), R.color.onSurface));
    drawPaint.setAntiAlias(true);
    drawPaint.setStyle(Paint.Style.STROKE);
    drawPaint.setStrokeJoin(Paint.Join.ROUND);
    drawPaint.setStrokeCap(Paint.Cap.ROUND);
    drawPaint.setStrokeWidth(5 * displayMetrics.density);
    canvasPaint = new Paint(Paint.DITHER_FLAG);
  }

  public void setOnStrokeListener(OnStrokeListener listener) {
    this.onStrokeListener = listener;
  }

  public interface OnStrokeListener {
    void onStroke(
        float x,
        float y,
        long timestamp,
        String action,
        float size,
        float pressure,
        float orientation
    );
  }  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (onStrokeListener == null) {
      Log.w(
          "CustomDrawingView",
          "onStrokeListener is null; touch events are ignored. Drawing is disabled."
      );
      return false;
    }

    handleMotionEventBatchedData(event);

    final float touchX = event.getX();
    final float touchY = event.getY();
    final long timestamp = event.getEventTime();
    final float size = event.getSize();
    final float pressure = event.getPressure();
    final float orientation = event.getOrientation();

    float normalizedX = touchX / getWidth();
    float normalizedY = touchY / getHeight();

    if (normalizedX < 0.0f || normalizedX > 1.0f || normalizedY < 0.0f || normalizedY > 1.0f) {
      return false;
    }

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        drawPath.moveTo(touchX, touchY);
        onStrokeListener.onStroke(
            normalizedX,
            normalizedY,
            timestamp,
            "ACTION_DOWN",
            size,
            pressure,
            orientation
        );
        break;
      case MotionEvent.ACTION_MOVE:
        drawPath.lineTo(touchX, touchY);
        onStrokeListener.onStroke(
            normalizedX,
            normalizedY,
            timestamp,
            "ACTION_MOVE",
            size,
            pressure,
            orientation
        );
        break;
      case MotionEvent.ACTION_UP:
        drawPath.lineTo(touchX, touchY);
        drawCanvas.drawPath(drawPath, drawPaint);
        onStrokeListener.onStroke(
            normalizedX,
            normalizedY,
            timestamp,
            "ACTION_UP",
            size,
            pressure,
            orientation
        );
        drawPath.reset();
        performClick();
        break;
      default:
        return false;
    }

    invalidate();
    return true;
  }




  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = Math.min(width, height);

    int minSize = (int) (300 * displayMetrics.density);
    int maxSize = (int) (500 * displayMetrics.density);

    size = Math.max(minSize, Math.min(size, maxSize));

    int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    super.onMeasure(finalMeasureSpec, finalMeasureSpec);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);
    canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    drawCanvas = new Canvas(canvasBitmap);
    setupGridBackground();
  }

  private void setupGridBackground() {
    Drawable grid = ResourcesCompat.getDrawable(getResources(), R.drawable.grid, null);
    if (grid != null) {
      grid.setTint(ContextCompat.getColor(getContext(), R.color.onSurface));
      grid.setAlpha(50);
    }
    setBackground(grid);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    canvas.drawPath(drawPath, drawPaint);
  }

  @Override
  public boolean performClick() {
    super.performClick();
    return true;
  }

  private void handleMotionEventBatchedData(MotionEvent event) {
    final int historySize = event.getHistorySize();

    for (int i = 0; i < historySize; i++) {
      final float historicalX = event.getHistoricalX(0, i);
      final float historicalY = event.getHistoricalY(0, i);
      final long historicalTime = event.getHistoricalEventTime(i);
      final float historicalSize = event.getHistoricalSize(0, i);
      final float historicalPressure = event.getHistoricalPressure(0, i);
      final float historicalOrientation = event.getHistoricalOrientation(0, i);

      float normalizedX = historicalX / getWidth();
      float normalizedY = historicalY / getHeight();

      if (normalizedX < 0.0f || normalizedX > 1.0f || normalizedY < 0.0f || normalizedY > 1.0f) {
        continue;
      }

      drawPath.lineTo(historicalX, historicalY);
      onStrokeListener.onStroke(
          normalizedX,
          normalizedY,
          historicalTime,
          "ACTION_MOVE",
          historicalSize,
          historicalPressure,
          historicalOrientation
      );
    }
  }

}

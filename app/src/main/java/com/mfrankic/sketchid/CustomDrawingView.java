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

/**
 * A custom {@link View} for drawing paths (strokes) by touch input.
 * This view captures touch events, draws them onto an internal bitmap, and notifies a listener
 * about each part of the stroke (ACTION_DOWN, ACTION_MOVE, ACTION_UP) along with its properties
 * like coordinates, timestamp, pressure, size, and orientation.
 * <p>
 * The view also supports a grid background and has a fixed size constraint.
 * Batched motion events are processed to ensure smooth drawing.
 */
public class CustomDrawingView extends View {

  private final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
  private Path drawPath;
  private Paint drawPaint;
  private Paint canvasPaint;
  private Bitmap canvasBitmap;
  private Canvas drawCanvas;
  private OnStrokeListener onStrokeListener;

  /**
   * Constructor for {@link CustomDrawingView}.
   *
   * @param context The Context the view is running in, through which it can
   *                access the current theme, resources, etc.
   * @param attrs   The attributes of the XML tag that is inflating the view.
   */
  public CustomDrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setupDrawing();
  }

  /**
   * Initializes the drawing tools and properties like {@link Paint} and {@link Path}.
   */
  private void setupDrawing() {
    drawPath = new Path();
    drawPaint = new Paint();
    drawPaint.setColor(ContextCompat.getColor(getContext(), R.color.onSurface));
    drawPaint.setAntiAlias(true);
    drawPaint.setStyle(Paint.Style.STROKE);
    drawPaint.setStrokeJoin(Paint.Join.ROUND);
    drawPaint.setStrokeCap(Paint.Cap.ROUND);
    // Scale stroke width by display density
    drawPaint.setStrokeWidth(5 * displayMetrics.density);
    canvasPaint = new Paint(Paint.DITHER_FLAG);
  }

  /**
   * Sets the listener to be notified of stroke events.
   *
   * @param listener The {@link OnStrokeListener} to notify.
   */
  public void setOnStrokeListener(OnStrokeListener listener) {
    this.onStrokeListener = listener;
  }

  /**
   * Interface for receiving callbacks when a stroke event occurs.
   */
  public interface OnStrokeListener {
    /**
     * Called when a part of a stroke is made (down, move, or up).
     *
     * @param x           The normalized X-coordinate of the touch event (0.0 to 1.0).
     * @param y           The normalized Y-coordinate of the touch event (0.0 to 1.0).
     * @param timestamp   The time (in ms) when the event originally occurred.
     * @param action      A string representing the motion event action (e.g., "ACTION_DOWN").
     * @param size        A scaled value of the approximate size of the contact area.
     * @param pressure    The pressure of the touch event, typically ranging from 0 (no pressure)
     *                    to 1 (normal pressure), but can be higher.
     * @param orientation The orientation of the touch area, relative to the vertical axis of the
     *                    screen.
     */
    void onStroke(
        float x,
        float y,
        long timestamp,
        String action,
        float size,
        float pressure,
        float orientation
    );
  }

  /**
   * Handles touch screen motion events to draw on the canvas and notify the
   * {@link OnStrokeListener}.
   * Processes both current and historical (batched) motion events.
   * Coordinates are normalized to be between 0.0 and 1.0 relative to the view's width and height.
   * Touch events outside the view bounds are ignored.
   *
   * @param event The motion event.
   * @return True if the event was handled, false otherwise.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (onStrokeListener == null) {
      Log.w(
          "CustomDrawingView",
          "onStrokeListener is null; touch events are ignored. Drawing is disabled."
      );
      return false; // Drawing is disabled if no listener is set
    }

    // Process historical (batched) data first for smoother lines
    handleMotionEventBatchedData(event);

    // Process the current event
    final float touchX = event.getX();
    final float touchY = event.getY();
    final long timestamp = event.getEventTime();
    final float size = event.getSize();
    final float pressure = event.getPressure();
    final float orientation = event.getOrientation();

    // Normalize coordinates to be between 0.0 and 1.0
    float normalizedX = touchX / getWidth();
    float normalizedY = touchY / getHeight();

    // Ignore events outside the view bounds
    if (normalizedX < 0.0f || normalizedX > 1.0f || normalizedY < 0.0f || normalizedY > 1.0f) {
      // If it's an ACTION_UP, still treat it as the end of a stroke if it started inside
      if (event.getAction() == MotionEvent.ACTION_UP && !drawPath.isEmpty()) {
        drawCanvas.drawPath(drawPath, drawPaint);
        onStrokeListener.onStroke(
            Math.max(0f, Math.min(1f, normalizedX)), // Clamp to bounds
            Math.max(0f, Math.min(1f, normalizedY)), // Clamp to bounds
            timestamp, "ACTION_UP", size, pressure, orientation
        );
        drawPath.reset();
        performClick();
        invalidate();
      }
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
        drawPath.lineTo(touchX, touchY); // Ensure the last segment is drawn
        drawCanvas.drawPath(drawPath, drawPaint); // Commit path to bitmap
        onStrokeListener.onStroke(
            normalizedX,
            normalizedY,
            timestamp,
            "ACTION_UP",
            size,
            pressure,
            orientation
        );
        drawPath.reset(); // Reset path for the next stroke
        performClick(); // Accessibility: perform click for ACTION_UP
        break;
      default:
        return false; // Ignore other actions
    }

    invalidate(); // Redraw the view
    return true;
  }


  /**
   * Measures the view and its content to determine the measured width and the measured height.
   * This implementation constrains the view to be a square, with size between a min and max value.
   *
   * @param widthMeasureSpec  Horizontal space requirements as imposed by the parent.
   * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = Math.min(width, height); // Use the smaller dimension for a square view

    // Define min and max sizes in dp, then convert to pixels
    int minSize = (int) (300 * displayMetrics.density);
    int maxSize = (int) (500 * displayMetrics.density);

    // Clamp the size to the defined min/max range
    size = Math.max(minSize, Math.min(size, maxSize));

    // Set the measured dimensions
    int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    super.onMeasure(finalMeasureSpec, finalMeasureSpec);
  }

  /**
   * Called when the size of this view has changed.
   * Recreates the drawing bitmap and canvas with the new dimensions and sets up the grid
   * background.
   *
   * @param w    Current width of this view.
   * @param h    Current height of this view.
   * @param oldW Old width of this view.
   * @param oldH Old height of this view.
   */
  @Override
  protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);
    canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    drawCanvas = new Canvas(canvasBitmap);
    setupGridBackground(); // Re-apply background if size changes
  }

  /**
   * Sets up a semi-transparent grid as the background of the drawing view.
   */
  private void setupGridBackground() {
    Drawable grid = ResourcesCompat.getDrawable(getResources(), R.drawable.grid, null);
    if (grid != null) {
      grid.setTint(ContextCompat.getColor(getContext(), R.color.onSurface));
      grid.setAlpha(50); // Set transparency (0-255)
    }
    setBackground(grid);
  }

  /**
   * Called when the view should render its content.
   * Draws the bitmap (which contains committed strokes) and the current path (live drawing).
   *
   * @param canvas the canvas on which the background will be drawn
   */
  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint); // Draw the committed strokes
    canvas.drawPath(drawPath, drawPaint); // Draw the current, live stroke
  }

  /**
   * Called when a click event is detected.
   * Ensures accessibility guidelines are met.
   *
   * @return True if the click was handled, false otherwise.
   */
  @Override
  public boolean performClick() {
    super.performClick();
    return true;
  }

  /**
   * Processes batched (historical) {@link MotionEvent} data.
   * This helps in creating smoother lines by drawing segments for intermediate points
   * that might have been reported by the system between two {@code onTouchEvent} calls.
   * Each historical point is treated as an ACTION_MOVE event.
   *
   * @param event The {@link MotionEvent} containing historical data.
   */
  private void handleMotionEventBatchedData(MotionEvent event) {
    if (onStrokeListener == null) return; // Guard against null listener

    final int historySize = event.getHistorySize();

    for (int i = 0; i < historySize; i++) {
      final float historicalX = event.getHistoricalX(0, i);
      final float historicalY = event.getHistoricalY(0, i);
      final long historicalTime = event.getHistoricalEventTime(i);
      final float historicalSize = event.getHistoricalSize(0, i);
      final float historicalPressure = event.getHistoricalPressure(0, i);
      final float historicalOrientation = event.getHistoricalOrientation(0, i);

      // Normalize coordinates
      float normalizedX = historicalX / getWidth();
      float normalizedY = historicalY / getHeight();

      // Ignore historical points outside bounds
      if (normalizedX < 0.0f || normalizedX > 1.0f || normalizedY < 0.0f || normalizedY > 1.0f) {
        continue;
      }

      drawPath.lineTo(historicalX, historicalY);
      onStrokeListener.onStroke(
          normalizedX,
          normalizedY,
          historicalTime,
          "ACTION_MOVE",
          // Historical data is always a move
          historicalSize,
          historicalPressure,
          historicalOrientation
      );
    }
  }

}

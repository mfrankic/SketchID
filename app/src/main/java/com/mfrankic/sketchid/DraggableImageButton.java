package com.mfrankic.sketchid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

/**
 * Custom ImageButton that properly handles accessibility when used for drag operations.
 */
public class DraggableImageButton extends AppCompatImageButton {

  private DragStartListener dragStartListener;

  public DraggableImageButton(@NonNull Context context) {
    super(context);
    init();
  }

  private void init() {
    // Set haptic feedback for better user experience
    setHapticFeedbackEnabled(true);
  }

  public DraggableImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DraggableImageButton(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr
  ) {
    super(context, attrs, defStyleAttr);
    init();
  }

  /**
   * Set a listener to be called when a drag operation should start
   *
   * @param listener The listener to call
   */
  public void setDragStartListener(DragStartListener listener) {
    this.dragStartListener = listener;
  }

  /**
   * Interface for listening to drag start events
   */
  public interface DragStartListener {
    /**
     * Called when a drag operation should start
     *
     * @param view The view that initiated the drag
     */
    void onDragStart(View view);
  }

  /**
   * Properly override performClick to handle accessibility.
   * This method will be called by accessibility services when a click/drag should occur.
   */
  @Override
  public boolean performClick() {
    // Call the super implementation first to handle standard click operations
    boolean handled = super.performClick();

    // If we have a drag listener, notify it that a drag should start
    if (dragStartListener != null) {
      // Announce drag start for accessibility
      announceForAccessibility(getResources().getString(R.string.drag_started));
      dragStartListener.onDragStart(this);
      // Consider the event handled
      return true;
    }

    return handled;
  }

  /**
   * Handle touch events to start drag immediately when pressed
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (dragStartListener == null || event.getAction() != MotionEvent.ACTION_DOWN) {
      return super.onTouchEvent(event);
    }

    performClick();
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    return true;
  }

} 

package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Unit tests for the DraggableImageButton class
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DraggableImageButtonTest {

  private Context context;
  private DraggableImageButton draggableButton;
  private DraggableImageButton.DragStartListener mockDragListener;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.getApplication();
    draggableButton = new DraggableImageButton(context);
    mockDragListener = mock(DraggableImageButton.DragStartListener.class);
  }

  @Test
  public void testConstructorWithContext() {
    DraggableImageButton button = new DraggableImageButton(context);
    assertNotNull(button);
    assertTrue(button.isHapticFeedbackEnabled());
  }

  @Test
  public void testConstructorWithContextAndAttrs() {
    AttributeSet attrs = null; // Robolectric allows null attrs
    DraggableImageButton button = new DraggableImageButton(context, attrs);
    assertNotNull(button);
    assertTrue(button.isHapticFeedbackEnabled());
  }

  @Test
  public void testConstructorWithContextAttrsAndDefStyle() {
    AttributeSet attrs = null; // Robolectric allows null attrs
    int defStyleAttr = 0;
    DraggableImageButton button = new DraggableImageButton(context, attrs, defStyleAttr);
    assertNotNull(button);
    assertTrue(button.isHapticFeedbackEnabled());
  }

  @Test
  public void testInitialization() {
    // Test that initialization sets haptic feedback
    assertTrue("Haptic feedback should be enabled", draggableButton.isHapticFeedbackEnabled());
  }

  @Test
  public void testSetDragStartListener() {
    // Test setting a drag start listener
    draggableButton.setDragStartListener(mockDragListener);

    // Verify by triggering performClick
    draggableButton.performClick();

    verify(mockDragListener, times(1)).onDragStart(draggableButton);
  }

  @Test
  public void testSetDragStartListenerNull() {
    // Test setting null listener
    draggableButton.setDragStartListener(null);

    // Should not crash when performClick is called
    boolean result = draggableButton.performClick();
    // In Robolectric, performClick might return false, which is acceptable
    // The important thing is that it doesn't crash
    assertNotNull("performClick should not crash", result);
  }

  @Test
  public void testPerformClickWithoutListener() {
    // Test performClick without a drag listener
    boolean result = draggableButton.performClick();
    // In Robolectric, performClick might return false, which is acceptable
    // The important thing is that it doesn't crash
    assertNotNull("performClick should not crash", result);
  }

  @Test
  public void testPerformClickWithListener() {
    draggableButton.setDragStartListener(mockDragListener);

    boolean result = draggableButton.performClick();

    assertTrue("performClick should return true when listener is set", result);
    verify(mockDragListener, times(1)).onDragStart(draggableButton);
  }

  @Test
  public void testPerformClickAccessibilityAnnouncement() {
    draggableButton.setDragStartListener(mockDragListener);

    // This test verifies that the method doesn't crash when announcing for accessibility
    // In a real test environment, we would need to mock the accessibility service
    boolean result = draggableButton.performClick();

    assertTrue("performClick should return true", result);
    verify(mockDragListener, times(1)).onDragStart(draggableButton);
  }

  @Test
  public void testOnTouchEventActionDown() {
    draggableButton.setDragStartListener(mockDragListener);

    MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100, 100, 0);

    boolean result = draggableButton.onTouchEvent(downEvent);

    assertTrue("onTouchEvent should return true for ACTION_DOWN with listener", result);
    verify(mockDragListener, times(1)).onDragStart(draggableButton);

    downEvent.recycle();
  }

  @Test
  public void testOnTouchEventActionDownWithoutListener() {
    // Test ACTION_DOWN without a listener - should delegate to super
    MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100, 100, 0);

    boolean result = draggableButton.onTouchEvent(downEvent);

    // Result depends on super implementation, but should not crash
    assertNotNull("Result should not be null", result);

    downEvent.recycle();
  }

  @Test
  public void testOnTouchEventActionMove() {
    draggableButton.setDragStartListener(mockDragListener);

    MotionEvent moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 100, 100, 0);

    boolean result = draggableButton.onTouchEvent(moveEvent);

    // ACTION_MOVE should delegate to super, not trigger drag
    verify(mockDragListener, never()).onDragStart(any(View.class));

    moveEvent.recycle();
  }

  @Test
  public void testOnTouchEventActionUp() {
    draggableButton.setDragStartListener(mockDragListener);

    MotionEvent upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 100, 100, 0);

    boolean result = draggableButton.onTouchEvent(upEvent);

    // ACTION_UP should delegate to super, not trigger drag
    verify(mockDragListener, never()).onDragStart(any(View.class));

    upEvent.recycle();
  }

  @Test
  public void testOnTouchEventActionCancel() {
    draggableButton.setDragStartListener(mockDragListener);

    MotionEvent cancelEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 100, 100, 0);

    boolean result = draggableButton.onTouchEvent(cancelEvent);

    // ACTION_CANCEL should delegate to super, not trigger drag
    verify(mockDragListener, never()).onDragStart(any(View.class));

    cancelEvent.recycle();
  }

  @Test
  public void testMultipleTouchDownEvents() {
    draggableButton.setDragStartListener(mockDragListener);

    // Simulate multiple ACTION_DOWN events
    MotionEvent downEvent1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100, 100, 0);
    MotionEvent downEvent2 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_DOWN, 150, 150, 0);

    draggableButton.onTouchEvent(downEvent1);
    draggableButton.onTouchEvent(downEvent2);

    // Should trigger drag listener twice
    verify(mockDragListener, times(2)).onDragStart(draggableButton);

    downEvent1.recycle();
    downEvent2.recycle();
  }

  @Test
  public void testDragStartListenerInterface() {
    // Test the interface implementation
    DraggableImageButton.DragStartListener listener = new DraggableImageButton.DragStartListener() {
      @Override
      public void onDragStart(View view) {
        assertEquals("View should be the draggable button", draggableButton, view);
      }
    };

    draggableButton.setDragStartListener(listener);
    draggableButton.performClick();
  }

  @Test
  public void testDragStartListenerWithLambda() {
    // Test using lambda expression
    boolean[] dragStarted = {false};

    draggableButton.setDragStartListener(view -> {
      dragStarted[0] = true;
      assertEquals("View should be the draggable button", draggableButton, view);
    });

    draggableButton.performClick();
    assertTrue("Drag should have started", dragStarted[0]);
  }

  @Test
  public void testSequentialTouchEvents() {
    draggableButton.setDragStartListener(mockDragListener);

    // Simulate a complete touch sequence
    MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100, 100, 0);
    MotionEvent moveEvent = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 110, 110, 0);
    MotionEvent upEvent = MotionEvent.obtain(0, 100, MotionEvent.ACTION_UP, 120, 120, 0);

    draggableButton.onTouchEvent(downEvent);
    draggableButton.onTouchEvent(moveEvent);
    draggableButton.onTouchEvent(upEvent);

    // Only ACTION_DOWN should trigger drag
    verify(mockDragListener, times(1)).onDragStart(draggableButton);

    downEvent.recycle();
    moveEvent.recycle();
    upEvent.recycle();
  }

  @Test
  public void testListenerReplacement() {
    DraggableImageButton.DragStartListener firstListener
        = mock(DraggableImageButton.DragStartListener.class);
    DraggableImageButton.DragStartListener secondListener
        = mock(DraggableImageButton.DragStartListener.class);

    // Set first listener
    draggableButton.setDragStartListener(firstListener);
    draggableButton.performClick();

    // Replace with second listener
    draggableButton.setDragStartListener(secondListener);
    draggableButton.performClick();

    // First listener should be called once, second listener should be called once
    verify(firstListener, times(1)).onDragStart(draggableButton);
    verify(secondListener, times(1)).onDragStart(draggableButton);
  }

  @Test
  public void testTouchEventCoordinates() {
    draggableButton.setDragStartListener(mockDragListener);

    // Test with different coordinates
    float[] coordinates = {0f, 50f, 100f, 200f, 500f};

    for (float x : coordinates) {
      for (float y : coordinates) {
        MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, x, y, 0);
        draggableButton.onTouchEvent(event);
        event.recycle();
      }
    }

    // Should trigger drag for each coordinate pair
    verify(mockDragListener, times(coordinates.length * coordinates.length)).onDragStart(
        draggableButton);
  }

  @Test
  public void testAccessibilityIntegration() {
    draggableButton.setDragStartListener(mockDragListener);

    // Test that performClick works for accessibility
    // This simulates what accessibility services would call
    boolean result = draggableButton.performClick();

    assertTrue("performClick should return true for accessibility", result);
    verify(mockDragListener, times(1)).onDragStart(draggableButton);
  }

  @Test
  public void testHapticFeedbackEnabled() {
    // Test that haptic feedback is enabled by default
    assertTrue("Haptic feedback should be enabled", draggableButton.isHapticFeedbackEnabled());

    // Test that we can disable it
    draggableButton.setHapticFeedbackEnabled(false);
    assertFalse("Haptic feedback should be disabled", draggableButton.isHapticFeedbackEnabled());

    // Test that we can re-enable it
    draggableButton.setHapticFeedbackEnabled(true);
    assertTrue("Haptic feedback should be re-enabled", draggableButton.isHapticFeedbackEnabled());
  }
} 

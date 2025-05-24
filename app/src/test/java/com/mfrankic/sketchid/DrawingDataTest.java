package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the DrawingData entity class
 */
public class DrawingDataTest {

  @Test
  public void testDefaultConstructor() {
    // Test the default constructor
    DrawingData data = new DrawingData();

    assertEquals(0, data.id);
    assertEquals(0L, data.time);
    assertEquals(0.0f, data.x, 0.001f);
    assertEquals(0.0f, data.y, 0.001f);
    assertNull(data.action);
    assertEquals(0, data.userID);
    assertEquals(0, data.imageID);
    assertNull(data.itemType);
    assertEquals(0, data.attempt);
    assertNull(data.sessionID);
    assertEquals(0.0f, data.size, 0.001f);
    assertEquals(0.0f, data.pressure, 0.001f);
    assertEquals(0.0f, data.orientation, 0.001f);
  }

  @Test
  public void testBuilderPattern() {
    // Test the Builder pattern with all fields
    long currentTime = System.currentTimeMillis();
    DrawingData data = new DrawingData.Builder()
        .time(currentTime)
        .x(100.5f)
        .y(200.3f)
        .action("MOVE")
        .userID(123)
        .imageID(456)
        .itemType(Item.Type.IMAGE)
        .attempt(2)
        .sessionID("session_001")
        .size(10.0f)
        .pressure(0.8f)
        .orientation(45.0f)
        .build();

    assertEquals(0, data.id); // ID should remain 0 (auto-generated)
    assertEquals(currentTime, data.time);
    assertEquals(100.5f, data.x, 0.001f);
    assertEquals(200.3f, data.y, 0.001f);
    assertEquals("MOVE", data.action);
    assertEquals(123, data.userID);
    assertEquals(456, data.imageID);
    assertEquals(Item.Type.IMAGE, data.itemType);
    assertEquals(2, data.attempt);
    assertEquals("session_001", data.sessionID);
    assertEquals(10.0f, data.size, 0.001f);
    assertEquals(0.8f, data.pressure, 0.001f);
    assertEquals(45.0f, data.orientation, 0.001f);
  }

  @Test
  public void testBuilderChaining() {
    // Test that Builder methods return the builder instance for chaining
    DrawingData.Builder builder = new DrawingData.Builder();

    DrawingData.Builder result = builder.time(1000L).x(50.0f).y(75.0f);

    assertSame(builder, result);

    DrawingData data = result.build();
    assertEquals(1000L, data.time);
    assertEquals(50.0f, data.x, 0.001f);
    assertEquals(75.0f, data.y, 0.001f);
  }

  @Test
  public void testBuilderPartialData() {
    // Test builder with only some fields set
    DrawingData data = new DrawingData.Builder().x(10.0f).y(20.0f).action("DOWN").build();

    assertEquals(0L, data.time); // Default value
    assertEquals(10.0f, data.x, 0.001f);
    assertEquals(20.0f, data.y, 0.001f);
    assertEquals("DOWN", data.action);
    assertEquals(0, data.userID); // Default value
    assertNull(data.itemType); // Default value
  }

  @Test
  public void testCopyMethod() {
    // Test the copy method
    DrawingData original = new DrawingData.Builder()
        .time(1234567890L)
        .x(123.45f)
        .y(678.90f)
        .action("DRAW")
        .userID(100)
        .imageID(200)
        .itemType(Item.Type.IMAGE)
        .attempt(3)
        .sessionID("original_session")
        .size(15.0f)
        .pressure(0.9f)
        .orientation(90.0f)
        .build();

    DrawingData copy = original.copy();

    // Verify all fields are copied
    assertEquals(original.time, copy.time);
    assertEquals(original.x, copy.x, 0.001f);
    assertEquals(original.y, copy.y, 0.001f);
    assertEquals(original.action, copy.action);
    assertEquals(original.userID, copy.userID);
    assertEquals(original.imageID, copy.imageID);
    assertEquals(original.itemType, copy.itemType);
    assertEquals(original.attempt, copy.attempt);
    assertEquals(original.sessionID, copy.sessionID);
    assertEquals(original.size, copy.size, 0.001f);
    assertEquals(original.pressure, copy.pressure, 0.001f);
    assertEquals(original.orientation, copy.orientation, 0.001f);

    // Verify it's a different object
    assertNotSame(original, copy);

    // Verify ID is reset to 0 in copy
    assertEquals(0, copy.id);
  }

  @Test
  public void testCopyIndependence() {
    // Test that copy and original are independent
    DrawingData original = new DrawingData.Builder().x(100.0f).y(200.0f).action("ORIGINAL").build();

    DrawingData copy = original.copy();

    // Modify the original
    original.x = 999.0f;
    original.action = "MODIFIED";

    // Verify copy remains unchanged
    assertEquals(100.0f, copy.x, 0.001f);
    assertEquals("ORIGINAL", copy.action);
  }

  @Test
  public void testFieldAssignment() {
    // Test direct field assignment after construction
    DrawingData data = new DrawingData();

    data.id = 999;
    data.time = 1234567890L;
    data.x = 50.5f;
    data.y = 75.3f;
    data.action = "TEST_ACTION";
    data.userID = 42;
    data.imageID = 84;
    data.itemType = Item.Type.IMAGE;
    data.attempt = 5;
    data.sessionID = "test_session";
    data.size = 12.5f;
    data.pressure = 0.7f;
    data.orientation = 180.0f;

    assertEquals(999, data.id);
    assertEquals(1234567890L, data.time);
    assertEquals(50.5f, data.x, 0.001f);
    assertEquals(75.3f, data.y, 0.001f);
    assertEquals("TEST_ACTION", data.action);
    assertEquals(42, data.userID);
    assertEquals(84, data.imageID);
    assertEquals(Item.Type.IMAGE, data.itemType);
    assertEquals(5, data.attempt);
    assertEquals("test_session", data.sessionID);
    assertEquals(12.5f, data.size, 0.001f);
    assertEquals(0.7f, data.pressure, 0.001f);
    assertEquals(180.0f, data.orientation, 0.001f);
  }

  @Test
  public void testWithNullValues() {
    // Test with null values
    DrawingData data = new DrawingData.Builder()
        .action(null)
        .itemType(null)
        .sessionID(null)
        .build();

    assertNull(data.action);
    assertNull(data.itemType);
    assertNull(data.sessionID);
  }

  @Test
  public void testWithEmptyStrings() {
    // Test with empty strings
    DrawingData data = new DrawingData.Builder().action("").sessionID("").build();

    assertEquals("", data.action);
    assertEquals("", data.sessionID);
  }

  @Test
  public void testBoundaryValues() {
    // Test with boundary values
    DrawingData data = new DrawingData.Builder()
        .time(Long.MAX_VALUE)
        .x(Float.MAX_VALUE)
        .y(Float.MIN_VALUE)
        .userID(Integer.MAX_VALUE)
        .imageID(Integer.MIN_VALUE)
        .attempt(-1)
        .size(0.0f)
        .pressure(1.0f)
        .orientation(360.0f)
        .build();

    assertEquals(Long.MAX_VALUE, data.time);
    assertEquals(Float.MAX_VALUE, data.x, 0.001f);
    assertEquals(Float.MIN_VALUE, data.y, 0.001f);
    assertEquals(Integer.MAX_VALUE, data.userID);
    assertEquals(Integer.MIN_VALUE, data.imageID);
    assertEquals(-1, data.attempt);
    assertEquals(0.0f, data.size, 0.001f);
    assertEquals(1.0f, data.pressure, 0.001f);
    assertEquals(360.0f, data.orientation, 0.001f);
  }

  @Test
  public void testSpecialFloatValues() {
    // Test with special float values
    DrawingData data = new DrawingData.Builder()
        .x(Float.POSITIVE_INFINITY)
        .y(Float.NEGATIVE_INFINITY)
        .size(Float.NaN)
        .pressure(0.0f)
        .orientation(-0.0f)
        .build();

    assertEquals(Float.POSITIVE_INFINITY, data.x, 0.001f);
    assertEquals(Float.NEGATIVE_INFINITY, data.y, 0.001f);
    assertTrue(Float.isNaN(data.size));
    assertEquals(0.0f, data.pressure, 0.001f);
    assertEquals(-0.0f, data.orientation, 0.001f);
  }

  @Test
  public void testRealisticDrawingScenarios() {
    // Test with realistic drawing scenarios

    // Scenario 1: Finger down
    DrawingData touchDown = new DrawingData.Builder()
        .time(System.currentTimeMillis())
        .x(150.0f)
        .y(300.0f)
        .action("DOWN")
        .userID(1)
        .imageID(5)
        .itemType(Item.Type.IMAGE)
        .attempt(1)
        .sessionID("drawing_session_001")
        .size(20.0f)
        .pressure(0.5f)
        .orientation(0.0f)
        .build();

    assertEquals("DOWN", touchDown.action);
    assertEquals(0.5f, touchDown.pressure, 0.001f);

    // Scenario 2: Finger move
    DrawingData touchMove = new DrawingData.Builder()
        .time(System.currentTimeMillis() + 100)
        .x(155.0f)
        .y(305.0f)
        .action("MOVE")
        .userID(1)
        .imageID(5)
        .itemType(Item.Type.IMAGE)
        .attempt(1)
        .sessionID("drawing_session_001")
        .size(20.0f)
        .pressure(0.7f)
        .orientation(5.0f)
        .build();

    assertEquals("MOVE", touchMove.action);
    assertEquals(0.7f, touchMove.pressure, 0.001f);

    // Scenario 3: Finger up
    DrawingData touchUp = new DrawingData.Builder()
        .time(System.currentTimeMillis() + 200)
        .x(160.0f)
        .y(310.0f)
        .action("UP")
        .userID(1)
        .imageID(5)
        .itemType(Item.Type.IMAGE)
        .attempt(1)
        .sessionID("drawing_session_001")
        .size(20.0f)
        .pressure(0.0f)
        .orientation(10.0f)
        .build();

    assertEquals("UP", touchUp.action);
    assertEquals(0.0f, touchUp.pressure, 0.001f);
  }

  @Test
  public void testCommonActionConstants() {
    // Test with common action types
    String[] commonActions = {"DOWN", "MOVE", "UP", "CANCEL"};

    for (String action : commonActions) {
      DrawingData data = new DrawingData.Builder().action(action).build();

      assertEquals(action, data.action);
    }
  }

  @Test
  public void testPressureRange() {
    // Test pressure values in typical range (0.0 to 1.0)
    float[] pressureValues = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};

    for (float pressure : pressureValues) {
      DrawingData data = new DrawingData.Builder().pressure(pressure).build();

      assertEquals(pressure, data.pressure, 0.001f);
      assertTrue("Pressure should be in valid range", pressure >= 0.0f && pressure <= 1.0f);
    }
  }

  @Test
  public void testOrientationRange() {
    // Test orientation values (typically 0-360 degrees)
    float[] orientationValues = {0.0f, 90.0f, 180.0f, 270.0f, 360.0f};

    for (float orientation : orientationValues) {
      DrawingData data = new DrawingData.Builder().orientation(orientation).build();

      assertEquals(orientation, data.orientation, 0.001f);
    }
  }

  @Test
  public void testMultipleBuilders() {
    // Test creating multiple builders independently
    DrawingData.Builder builder1 = new DrawingData.Builder().x(10.0f).y(20.0f);
    DrawingData.Builder builder2 = new DrawingData.Builder().x(30.0f).y(40.0f);

    DrawingData data1 = builder1.build();
    DrawingData data2 = builder2.build();

    assertEquals(10.0f, data1.x, 0.001f);
    assertEquals(20.0f, data1.y, 0.001f);
    assertEquals(30.0f, data2.x, 0.001f);
    assertEquals(40.0f, data2.y, 0.001f);
  }

  @Test
  public void testLongSessionId() {
    // Test with very long session ID
    StringBuilder longSessionId = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longSessionId.append("s");
    }
    String sessionId = longSessionId.toString();

    DrawingData data = new DrawingData.Builder().sessionID(sessionId).build();

    assertEquals(sessionId, data.sessionID);
    assertEquals(1000, data.sessionID.length());
  }

  @Test
  public void testSpecialCharactersInStrings() {
    // Test with special characters in string fields
    String specialAction = "ACTION_@#$%^&*()";
    String specialSessionId = "session-123_@#$%^&*()";

    DrawingData data = new DrawingData.Builder()
        .action(specialAction)
        .sessionID(specialSessionId)
        .build();

    assertEquals(specialAction, data.action);
    assertEquals(specialSessionId, data.sessionID);
  }

  @Test
  public void testCopyWithModification() {
    // Test copying and then modifying specific fields
    DrawingData original = new DrawingData.Builder()
        .time(1000L)
        .x(100.0f)
        .y(200.0f)
        .action("ORIGINAL")
        .build();

    DrawingData modified = original.copy();
    modified.action = "MODIFIED";
    modified.x = 999.0f;

    // Verify original is unchanged
    assertEquals("ORIGINAL", original.action);
    assertEquals(100.0f, original.x, 0.001f);

    // Verify modified has changes
    assertEquals("MODIFIED", modified.action);
    assertEquals(999.0f, modified.x, 0.001f);

    // Verify other fields remain the same
    assertEquals(original.time, modified.time);
    assertEquals(original.y, modified.y, 0.001f);
  }
} 

package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for MagneticFieldExportData class.
 * Tests getters, setters, drawing mode behavior, and data integrity.
 */
public class MagneticFieldExportDataTest {

  @Test
  public void testDefaultConstructor() {
    MagneticFieldExportData data = new MagneticFieldExportData();
    assertNotNull("Data object should not be null", data);

    // Test default values
    assertEquals("Default id should be 0", 0, data.getId());
    assertEquals("Default userId should be 0", 0, data.getUserId());
    assertNull("Default userName should be null", data.getUserName());
    assertEquals("Default imageId should be 0", 0, data.getImageId());
    assertNull("Default imageName should be null", data.getImageName());
    assertNull("Default sessionId should be null", data.getSessionId());
    assertEquals("Default attempt should be 0", 0, data.getAttempt());
    assertEquals("Default timestamp should be 0", 0L, data.getTimestamp());
    assertEquals("Default x should be 0.0", 0.0f, data.getX(), 0.001f);
    assertEquals("Default y should be 0.0", 0.0f, data.getY(), 0.001f);
    assertEquals("Default z should be 0.0", 0.0f, data.getZ(), 0.001f);
    assertEquals(
        "Default drawing mode should be normal",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );
  }

  @Test
  public void testIdGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setId(42);
    assertEquals("Id should be set correctly", 42, data.getId());

    data.setId(-1);
    assertEquals("Negative id should be allowed", -1, data.getId());

    data.setId(Integer.MAX_VALUE);
    assertEquals("Max int value should be allowed", Integer.MAX_VALUE, data.getId());

    data.setId(Integer.MIN_VALUE);
    assertEquals("Min int value should be allowed", Integer.MIN_VALUE, data.getId());
  }

  @Test
  public void testUserIdGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setUserId(123);
    assertEquals("UserId should be set correctly", 123, data.getUserId());

    data.setUserId(0);
    assertEquals("Zero userId should be allowed", 0, data.getUserId());

    data.setUserId(Integer.MAX_VALUE);
    assertEquals("Max int value should be allowed", Integer.MAX_VALUE, data.getUserId());
  }

  @Test
  public void testUserNameGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    String userName = "TestUser";
    data.setUserName(userName);
    assertEquals("UserName should be set correctly", userName, data.getUserName());

    data.setUserName(null);
    assertNull("Null userName should be allowed", data.getUserName());

    data.setUserName("");
    assertEquals("Empty userName should be allowed", "", data.getUserName());

    String longName = "A".repeat(1000);
    data.setUserName(longName);
    assertEquals("Long userName should be allowed", longName, data.getUserName());
  }

  @Test
  public void testImageIdGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setImageId(456);
    assertEquals("ImageId should be set correctly", 456, data.getImageId());

    data.setImageId(0);
    assertEquals("Zero imageId should be allowed", 0, data.getImageId());

    data.setImageId(-1);
    assertEquals("Negative imageId should be allowed", -1, data.getImageId());
  }

  @Test
  public void testImageNameGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    String imageName = "TestImage.png";
    data.setImageName(imageName);
    assertEquals("ImageName should be set correctly", imageName, data.getImageName());

    data.setImageName(null);
    assertNull("Null imageName should be allowed", data.getImageName());

    data.setImageName("");
    assertEquals("Empty imageName should be allowed", "", data.getImageName());

    // Test with special characters
    String specialName = "Test@Image#123.png";
    data.setImageName(specialName);
    assertEquals(
        "ImageName with special characters should be allowed",
        specialName,
        data.getImageName()
    );
  }

  @Test
  public void testSessionIdGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    String sessionId = "session_123_abc";
    data.setSessionId(sessionId);
    assertEquals("SessionId should be set correctly", sessionId, data.getSessionId());

    data.setSessionId(null);
    assertNull("Null sessionId should be allowed", data.getSessionId());

    data.setSessionId("");
    assertEquals("Empty sessionId should be allowed", "", data.getSessionId());

    // Test with UUID-like format
    String uuidSession = "550e8400-e29b-41d4-a716-446655440000";
    data.setSessionId(uuidSession);
    assertEquals("UUID-format sessionId should be allowed", uuidSession, data.getSessionId());
  }

  @Test
  public void testAttemptGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setAttempt(5);
    assertEquals("Attempt should be set correctly", 5, data.getAttempt());

    data.setAttempt(0);
    assertEquals("Zero attempt should be allowed", 0, data.getAttempt());

    data.setAttempt(1);
    assertEquals("First attempt should be allowed", 1, data.getAttempt());

    data.setAttempt(100);
    assertEquals("High attempt number should be allowed", 100, data.getAttempt());

    data.setAttempt(-1);
    assertEquals("Negative attempt should be allowed", -1, data.getAttempt());
  }

  @Test
  public void testTimestampGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    long timestamp = System.currentTimeMillis();
    data.setTimestamp(timestamp);
    assertEquals("Timestamp should be set correctly", timestamp, data.getTimestamp());

    data.setTimestamp(0L);
    assertEquals("Zero timestamp should be allowed", 0L, data.getTimestamp());

    data.setTimestamp(Long.MAX_VALUE);
    assertEquals("Max long timestamp should be allowed", Long.MAX_VALUE, data.getTimestamp());

    data.setTimestamp(Long.MIN_VALUE);
    assertEquals("Min long timestamp should be allowed", Long.MIN_VALUE, data.getTimestamp());
  }

  @Test
  public void testXCoordinateGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setX(1.5f);
    assertEquals("X coordinate should be set correctly", 1.5f, data.getX(), 0.001f);

    data.setX(0.0f);
    assertEquals("Zero X should be allowed", 0.0f, data.getX(), 0.001f);

    data.setX(-5.7f);
    assertEquals("Negative X should be allowed", -5.7f, data.getX(), 0.001f);

    data.setX(Float.MAX_VALUE);
    assertEquals("Max float X should be allowed", Float.MAX_VALUE, data.getX(), 0.001f);

    data.setX(Float.MIN_VALUE);
    assertEquals("Min float X should be allowed", Float.MIN_VALUE, data.getX(), 0.001f);
  }

  @Test
  public void testYCoordinateGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setY(2.8f);
    assertEquals("Y coordinate should be set correctly", 2.8f, data.getY(), 0.001f);

    data.setY(0.0f);
    assertEquals("Zero Y should be allowed", 0.0f, data.getY(), 0.001f);

    data.setY(-10.3f);
    assertEquals("Negative Y should be allowed", -10.3f, data.getY(), 0.001f);

    data.setY(Float.POSITIVE_INFINITY);
    assertEquals(
        "Positive infinity Y should be allowed",
        Float.POSITIVE_INFINITY,
        data.getY(),
        0.001f
    );

    data.setY(Float.NEGATIVE_INFINITY);
    assertEquals(
        "Negative infinity Y should be allowed",
        Float.NEGATIVE_INFINITY,
        data.getY(),
        0.001f
    );
  }

  @Test
  public void testZCoordinateGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    data.setZ(3.14f);
    assertEquals("Z coordinate should be set correctly", 3.14f, data.getZ(), 0.001f);

    data.setZ(0.0f);
    assertEquals("Zero Z should be allowed", 0.0f, data.getZ(), 0.001f);

    data.setZ(-7.2f);
    assertEquals("Negative Z should be allowed", -7.2f, data.getZ(), 0.001f);

    // Test NaN
    data.setZ(Float.NaN);
    assertTrue("NaN Z should be allowed", Float.isNaN(data.getZ()));
  }

  @Test
  public void testDrawingModeGetterAndSetter() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test default behavior - should return normal mode when null
    assertEquals(
        "Default drawing mode should be normal",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );

    // Test setting normal mode
    data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
    assertEquals(
        "Normal drawing mode should be set correctly",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );

    // Test setting overlay mode
    data.setDrawingMode(Constants.DRAWING_MODE_OVERLAY);
    assertEquals(
        "Overlay drawing mode should be set correctly",
        Constants.DRAWING_MODE_OVERLAY,
        data.getDrawingMode()
    );

    // Test setting null - should default to normal
    data.setDrawingMode(null);
    assertEquals(
        "Null drawing mode should default to normal",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );

    // Test setting empty string
    data.setDrawingMode("");
    assertEquals("Empty drawing mode should be preserved", "", data.getDrawingMode());

    // Test setting custom value
    String customMode = "custom_mode";
    data.setDrawingMode(customMode);
    assertEquals("Custom drawing mode should be preserved", customMode, data.getDrawingMode());
  }

  @Test
  public void testDrawingModeNullBehavior() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Initially null, should return default
    assertEquals(
        "Initially null should return normal mode",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );

    // Set to non-null, then back to null
    data.setDrawingMode(Constants.DRAWING_MODE_OVERLAY);
    assertEquals("Should be overlay mode", Constants.DRAWING_MODE_OVERLAY, data.getDrawingMode());

    data.setDrawingMode(null);
    assertEquals(
        "Back to null should return normal mode",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );
  }

  @Test
  public void testMultipleFieldsSimultaneously() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Set all fields
    data.setId(100);
    data.setUserId(200);
    data.setUserName("TestUser");
    data.setImageId(300);
    data.setImageName("TestImage.png");
    data.setSessionId("session_123");
    data.setAttempt(3);
    data.setTimestamp(1234567890L);
    data.setX(1.1f);
    data.setY(2.2f);
    data.setZ(3.3f);
    data.setDrawingMode(Constants.DRAWING_MODE_OVERLAY);

    // Verify all fields
    assertEquals("Id should be preserved", 100, data.getId());
    assertEquals("UserId should be preserved", 200, data.getUserId());
    assertEquals("UserName should be preserved", "TestUser", data.getUserName());
    assertEquals("ImageId should be preserved", 300, data.getImageId());
    assertEquals("ImageName should be preserved", "TestImage.png", data.getImageName());
    assertEquals("SessionId should be preserved", "session_123", data.getSessionId());
    assertEquals("Attempt should be preserved", 3, data.getAttempt());
    assertEquals("Timestamp should be preserved", 1234567890L, data.getTimestamp());
    assertEquals("X should be preserved", 1.1f, data.getX(), 0.001f);
    assertEquals("Y should be preserved", 2.2f, data.getY(), 0.001f);
    assertEquals("Z should be preserved", 3.3f, data.getZ(), 0.001f);
    assertEquals(
        "DrawingMode should be preserved",
        Constants.DRAWING_MODE_OVERLAY,
        data.getDrawingMode()
    );
  }

  @Test
  public void testFieldIndependence() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test that setting one field doesn't affect others
    data.setId(1);
    assertEquals("Only id should be set", 1, data.getId());
    assertEquals("UserId should remain default", 0, data.getUserId());
    assertNull("UserName should remain null", data.getUserName());

    data.setX(5.5f);
    assertEquals("X should be set", 5.5f, data.getX(), 0.001f);
    assertEquals("Y should remain default", 0.0f, data.getY(), 0.001f);
    assertEquals("Z should remain default", 0.0f, data.getZ(), 0.001f);
  }

  @Test
  public void testStringFieldsWithUnicode() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test Unicode in userName
    String unicodeUserName = "用户123";
    data.setUserName(unicodeUserName);
    assertEquals("Unicode userName should be preserved", unicodeUserName, data.getUserName());

    // Test Unicode in imageName
    String unicodeImageName = "图片_🎨.png";
    data.setImageName(unicodeImageName);
    assertEquals("Unicode imageName should be preserved", unicodeImageName, data.getImageName());

    // Test Unicode in sessionId
    String unicodeSessionId = "会话_αβγ_123";
    data.setSessionId(unicodeSessionId);
    assertEquals("Unicode sessionId should be preserved", unicodeSessionId, data.getSessionId());
  }

  @Test
  public void testFloatPrecision() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test very small values
    float smallValue = 0.000001f;
    data.setX(smallValue);
    assertEquals("Small X value should be preserved", smallValue, data.getX(), 0.0000001f);

    // Test precise decimal values
    float preciseValue = 1.23456789f;
    data.setY(preciseValue);
    assertEquals("Precise Y value should be preserved", preciseValue, data.getY(), 0.0001f);

    // Test very large values
    float largeValue = 999999.9f;
    data.setZ(largeValue);
    assertEquals("Large Z value should be preserved", largeValue, data.getZ(), 0.1f);
  }

  @Test
  public void testBoundaryValues() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test integer boundaries
    data.setId(0);
    assertEquals("Zero id should work", 0, data.getId());

    data.setUserId(1);
    assertEquals("One userId should work", 1, data.getUserId());

    data.setAttempt(-1);
    assertEquals("Negative attempt should work", -1, data.getAttempt());

    // Test long boundaries
    data.setTimestamp(1L);
    assertEquals("One timestamp should work", 1L, data.getTimestamp());

    // Test float boundaries
    data.setX(1.0f);
    assertEquals("One X should work", 1.0f, data.getX(), 0.001f);

    data.setY(-1.0f);
    assertEquals("Negative one Y should work", -1.0f, data.getY(), 0.001f);
  }

  @Test
  public void testNullStringHandling() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test all string fields with null
    data.setUserName(null);
    data.setImageName(null);
    data.setSessionId(null);
    data.setDrawingMode(null);

    assertNull("UserName should be null", data.getUserName());
    assertNull("ImageName should be null", data.getImageName());
    assertNull("SessionId should be null", data.getSessionId());
    assertEquals(
        "DrawingMode should default to normal when null",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );
  }

  @Test
  public void testEmptyStringHandling() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test all string fields with empty strings
    data.setUserName("");
    data.setImageName("");
    data.setSessionId("");
    data.setDrawingMode("");

    assertEquals("Empty userName should be preserved", "", data.getUserName());
    assertEquals("Empty imageName should be preserved", "", data.getImageName());
    assertEquals("Empty sessionId should be preserved", "", data.getSessionId());
    assertEquals("Empty drawingMode should be preserved", "", data.getDrawingMode());
  }

  @Test
  public void testWhitespaceStringHandling() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test strings with only whitespace
    String whitespace = "   \t\n  ";
    data.setUserName(whitespace);
    data.setImageName(whitespace);
    data.setSessionId(whitespace);
    data.setDrawingMode(whitespace);

    assertEquals("Whitespace userName should be preserved", whitespace, data.getUserName());
    assertEquals("Whitespace imageName should be preserved", whitespace, data.getImageName());
    assertEquals("Whitespace sessionId should be preserved", whitespace, data.getSessionId());
    assertEquals("Whitespace drawingMode should be preserved", whitespace, data.getDrawingMode());
  }

  @Test
  public void testRepeatedSetting() {
    MagneticFieldExportData data = new MagneticFieldExportData();

    // Test setting the same field multiple times
    data.setX(1.0f);
    data.setX(2.0f);
    data.setX(3.0f);
    assertEquals("Final X value should be preserved", 3.0f, data.getX(), 0.001f);

    data.setUserName("First");
    data.setUserName("Second");
    data.setUserName("Third");
    assertEquals("Final userName should be preserved", "Third", data.getUserName());

    data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
    data.setDrawingMode(Constants.DRAWING_MODE_OVERLAY);
    data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
    assertEquals(
        "Final drawingMode should be preserved",
        Constants.DRAWING_MODE_NORMAL,
        data.getDrawingMode()
    );
  }
} 

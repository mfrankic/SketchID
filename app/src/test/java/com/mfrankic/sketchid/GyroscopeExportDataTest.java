package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for GyroscopeExportData.
 * Tests all getters, setters, and default value behavior.
 */
public class GyroscopeExportDataTest {

  private GyroscopeExportData exportData;

  @Before
  public void setup() {
    exportData = new GyroscopeExportData();
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull("Export data should be created", exportData);
  }

  @Test
  public void testIdGetterSetter() {
    int testId = 123;
    exportData.setId(testId);
    assertEquals("ID should match", testId, exportData.getId());
  }

  @Test
  public void testUserIdGetterSetter() {
    int testUserId = 456;
    exportData.setUserId(testUserId);
    assertEquals("User ID should match", testUserId, exportData.getUserId());
  }

  @Test
  public void testUserNameGetterSetter() {
    String testUserName = "TestUser";
    exportData.setUserName(testUserName);
    assertEquals("User name should match", testUserName, exportData.getUserName());
  }

  @Test
  public void testUserNameWithNull() {
    exportData.setUserName(null);
    assertNull("User name should be null", exportData.getUserName());
  }

  @Test
  public void testImageIdGetterSetter() {
    int testImageId = 789;
    exportData.setImageId(testImageId);
    assertEquals("Image ID should match", testImageId, exportData.getImageId());
  }

  @Test
  public void testImageNameGetterSetter() {
    String testImageName = "test_image.png";
    exportData.setImageName(testImageName);
    assertEquals("Image name should match", testImageName, exportData.getImageName());
  }

  @Test
  public void testImageNameWithNull() {
    exportData.setImageName(null);
    assertNull("Image name should be null", exportData.getImageName());
  }

  @Test
  public void testSessionIdGetterSetter() {
    String testSessionId = "session_123";
    exportData.setSessionId(testSessionId);
    assertEquals("Session ID should match", testSessionId, exportData.getSessionId());
  }

  @Test
  public void testSessionIdWithNull() {
    exportData.setSessionId(null);
    assertNull("Session ID should be null", exportData.getSessionId());
  }

  @Test
  public void testAttemptGetterSetter() {
    int testAttempt = 3;
    exportData.setAttempt(testAttempt);
    assertEquals("Attempt should match", testAttempt, exportData.getAttempt());
  }

  @Test
  public void testTimestampGetterSetter() {
    long testTimestamp = System.currentTimeMillis();
    exportData.setTimestamp(testTimestamp);
    assertEquals("Timestamp should match", testTimestamp, exportData.getTimestamp());
  }

  @Test
  public void testXGetterSetter() {
    float testX = 1.23f;
    exportData.setX(testX);
    assertEquals("X value should match", testX, exportData.getX(), 0.001f);
  }

  @Test
  public void testYGetterSetter() {
    float testY = 4.56f;
    exportData.setY(testY);
    assertEquals("Y value should match", testY, exportData.getY(), 0.001f);
  }

  @Test
  public void testZGetterSetter() {
    float testZ = 7.89f;
    exportData.setZ(testZ);
    assertEquals("Z value should match", testZ, exportData.getZ(), 0.001f);
  }

  @Test
  public void testNegativeCoordinates() {
    float negativeX = -1.23f;
    float negativeY = -4.56f;
    float negativeZ = -7.89f;

    exportData.setX(negativeX);
    exportData.setY(negativeY);
    exportData.setZ(negativeZ);

    assertEquals("Negative X should be handled", negativeX, exportData.getX(), 0.001f);
    assertEquals("Negative Y should be handled", negativeY, exportData.getY(), 0.001f);
    assertEquals("Negative Z should be handled", negativeZ, exportData.getZ(), 0.001f);
  }

  @Test
  public void testDrawingModeGetterSetter() {
    String testMode = "test_mode";
    exportData.setDrawingMode(testMode);
    assertEquals("Drawing mode should match", testMode, exportData.getDrawingMode());
  }

  @Test
  public void testDrawingModeDefaultValue() {
    // When drawing mode is null, should return default
    exportData.setDrawingMode(null);
    assertEquals(
        "Should return default drawing mode",
        Constants.DRAWING_MODE_NORMAL,
        exportData.getDrawingMode()
    );
  }

  @Test
  public void testDrawingModeInitialValue() {
    // Initial value should be default when not set
    assertEquals(
        "Initial drawing mode should be default",
        Constants.DRAWING_MODE_NORMAL,
        exportData.getDrawingMode()
    );
  }

  @Test
  public void testAllFieldsTogether() {
    // Test setting all fields at once
    int id = 1;
    int userId = 2;
    String userName = "TestUser";
    int imageId = 3;
    String imageName = "test.png";
    String sessionId = "session_1";
    int attempt = 1;
    long timestamp = 1234567890L;
    float x = 1.0f;
    float y = 2.0f;
    float z = 3.0f;
    String drawingMode = "custom_mode";

    exportData.setId(id);
    exportData.setUserId(userId);
    exportData.setUserName(userName);
    exportData.setImageId(imageId);
    exportData.setImageName(imageName);
    exportData.setSessionId(sessionId);
    exportData.setAttempt(attempt);
    exportData.setTimestamp(timestamp);
    exportData.setX(x);
    exportData.setY(y);
    exportData.setZ(z);
    exportData.setDrawingMode(drawingMode);

    assertEquals("ID should match", id, exportData.getId());
    assertEquals("User ID should match", userId, exportData.getUserId());
    assertEquals("User name should match", userName, exportData.getUserName());
    assertEquals("Image ID should match", imageId, exportData.getImageId());
    assertEquals("Image name should match", imageName, exportData.getImageName());
    assertEquals("Session ID should match", sessionId, exportData.getSessionId());
    assertEquals("Attempt should match", attempt, exportData.getAttempt());
    assertEquals("Timestamp should match", timestamp, exportData.getTimestamp());
    assertEquals("X should match", x, exportData.getX(), 0.001f);
    assertEquals("Y should match", y, exportData.getY(), 0.001f);
    assertEquals("Z should match", z, exportData.getZ(), 0.001f);
    assertEquals("Drawing mode should match", drawingMode, exportData.getDrawingMode());
  }

  @Test
  public void testZeroValues() {
    exportData.setId(0);
    exportData.setUserId(0);
    exportData.setImageId(0);
    exportData.setAttempt(0);
    exportData.setTimestamp(0L);
    exportData.setX(0.0f);
    exportData.setY(0.0f);
    exportData.setZ(0.0f);

    assertEquals("Zero ID should be handled", 0, exportData.getId());
    assertEquals("Zero user ID should be handled", 0, exportData.getUserId());
    assertEquals("Zero image ID should be handled", 0, exportData.getImageId());
    assertEquals("Zero attempt should be handled", 0, exportData.getAttempt());
    assertEquals("Zero timestamp should be handled", 0L, exportData.getTimestamp());
    assertEquals("Zero X should be handled", 0.0f, exportData.getX(), 0.001f);
    assertEquals("Zero Y should be handled", 0.0f, exportData.getY(), 0.001f);
    assertEquals("Zero Z should be handled", 0.0f, exportData.getZ(), 0.001f);
  }

  @Test
  public void testExtremeValues() {
    exportData.setId(Integer.MAX_VALUE);
    exportData.setUserId(Integer.MIN_VALUE);
    exportData.setImageId(Integer.MAX_VALUE);
    exportData.setAttempt(Integer.MIN_VALUE);
    exportData.setTimestamp(Long.MAX_VALUE);
    exportData.setX(Float.MAX_VALUE);
    exportData.setY(Float.MIN_VALUE);
    exportData.setZ(-Float.MAX_VALUE);

    assertEquals("Max int ID should be handled", Integer.MAX_VALUE, exportData.getId());
    assertEquals("Min int user ID should be handled", Integer.MIN_VALUE, exportData.getUserId());
    assertEquals("Max int image ID should be handled", Integer.MAX_VALUE, exportData.getImageId());
    assertEquals("Min int attempt should be handled", Integer.MIN_VALUE, exportData.getAttempt());
    assertEquals("Max long timestamp should be handled", Long.MAX_VALUE, exportData.getTimestamp());
    assertEquals("Max float X should be handled", Float.MAX_VALUE, exportData.getX(), 0.001f);
    assertEquals("Min float Y should be handled", Float.MIN_VALUE, exportData.getY(), 0.001f);
    assertEquals(
        "Negative max float Z should be handled",
        -Float.MAX_VALUE,
        exportData.getZ(),
        0.001f
    );
  }
} 

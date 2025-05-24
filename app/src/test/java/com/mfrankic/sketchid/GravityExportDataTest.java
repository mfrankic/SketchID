package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for GravityExportData.
 * Tests all getters, setters, and default value behavior.
 */
public class GravityExportDataTest {

  private GravityExportData exportData;

  @Before
  public void setup() {
    exportData = new GravityExportData();
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
    float testX = 9.81f; // Earth's gravity
    exportData.setX(testX);
    assertEquals("X value should match", testX, exportData.getX(), 0.001f);
  }

  @Test
  public void testYGetterSetter() {
    float testY = 0.0f;
    exportData.setY(testY);
    assertEquals("Y value should match", testY, exportData.getY(), 0.001f);
  }

  @Test
  public void testZGetterSetter() {
    float testZ = -9.81f;
    exportData.setZ(testZ);
    assertEquals("Z value should match", testZ, exportData.getZ(), 0.001f);
  }

  @Test
  public void testGravityValues() {
    // Test typical gravity values
    float earthGravityX = 0.0f;
    float earthGravityY = 0.0f;
    float earthGravityZ = 9.81f;

    exportData.setX(earthGravityX);
    exportData.setY(earthGravityY);
    exportData.setZ(earthGravityZ);

    assertEquals("Earth gravity X should be handled", earthGravityX, exportData.getX(), 0.001f);
    assertEquals("Earth gravity Y should be handled", earthGravityY, exportData.getY(), 0.001f);
    assertEquals("Earth gravity Z should be handled", earthGravityZ, exportData.getZ(), 0.001f);
  }

  @Test
  public void testDrawingModeGetterSetter() {
    String testMode = "gravity_mode";
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
    String userName = "GravityUser";
    int imageId = 3;
    String imageName = "gravity_test.png";
    String sessionId = "gravity_session_1";
    int attempt = 1;
    long timestamp = 1234567890L;
    float x = 0.1f;
    float y = 0.2f;
    float z = 9.8f;
    String drawingMode = "gravity_mode";

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
  public void testZeroGravity() {
    // Test zero gravity scenario
    exportData.setX(0.0f);
    exportData.setY(0.0f);
    exportData.setZ(0.0f);

    assertEquals("Zero gravity X should be handled", 0.0f, exportData.getX(), 0.001f);
    assertEquals("Zero gravity Y should be handled", 0.0f, exportData.getY(), 0.001f);
    assertEquals("Zero gravity Z should be handled", 0.0f, exportData.getZ(), 0.001f);
  }

  @Test
  public void testExtremeGravityValues() {
    // Test extreme gravity values
    exportData.setX(Float.MAX_VALUE);
    exportData.setY(Float.MIN_VALUE);
    exportData.setZ(-Float.MAX_VALUE);

    assertEquals("Max float X should be handled", Float.MAX_VALUE, exportData.getX(), 0.001f);
    assertEquals("Min float Y should be handled", Float.MIN_VALUE, exportData.getY(), 0.001f);
    assertEquals(
        "Negative max float Z should be handled",
        -Float.MAX_VALUE,
        exportData.getZ(),
        0.001f
    );
  }

  @Test
  public void testNegativeIds() {
    exportData.setId(-1);
    exportData.setUserId(-2);
    exportData.setImageId(-3);
    exportData.setAttempt(-1);

    assertEquals("Negative ID should be handled", -1, exportData.getId());
    assertEquals("Negative user ID should be handled", -2, exportData.getUserId());
    assertEquals("Negative image ID should be handled", -3, exportData.getImageId());
    assertEquals("Negative attempt should be handled", -1, exportData.getAttempt());
  }

  @Test
  public void testEmptyStrings() {
    exportData.setUserName("");
    exportData.setImageName("");
    exportData.setSessionId("");
    exportData.setDrawingMode("");

    assertEquals("Empty user name should be handled", "", exportData.getUserName());
    assertEquals("Empty image name should be handled", "", exportData.getImageName());
    assertEquals("Empty session ID should be handled", "", exportData.getSessionId());
    assertEquals("Empty drawing mode should be handled", "", exportData.getDrawingMode());
  }
} 

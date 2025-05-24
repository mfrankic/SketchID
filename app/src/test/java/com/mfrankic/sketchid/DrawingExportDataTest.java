package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for DrawingExportData.
 * Tests all getters, setters, and default value behavior.
 */
public class DrawingExportDataTest {

  private DrawingExportData exportData;

  @Before
  public void setup() {
    exportData = new DrawingExportData();
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
  public void testTimeGetterSetter() {
    long testTime = System.currentTimeMillis();
    exportData.setTime(testTime);
    assertEquals("Time should match", testTime, exportData.getTime());
  }

  @Test
  public void testXGetterSetter() {
    float testX = 100.5f;
    exportData.setX(testX);
    assertEquals("X value should match", testX, exportData.getX(), 0.001f);
  }

  @Test
  public void testYGetterSetter() {
    float testY = 200.7f;
    exportData.setY(testY);
    assertEquals("Y value should match", testY, exportData.getY(), 0.001f);
  }

  @Test
  public void testActionGetterSetter() {
    String testAction = "MOVE";
    exportData.setAction(testAction);
    assertEquals("Action should match", testAction, exportData.getAction());
  }

  @Test
  public void testActionWithNull() {
    exportData.setAction(null);
    assertNull("Action should be null", exportData.getAction());
  }

  @Test
  public void testUserIDGetterSetter() {
    int testUserID = 456;
    exportData.setUserID(testUserID);
    assertEquals("User ID should match", testUserID, exportData.getUserID());
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
  public void testAttemptGetterSetter() {
    int testAttempt = 3;
    exportData.setAttempt(testAttempt);
    assertEquals("Attempt should match", testAttempt, exportData.getAttempt());
  }

  @Test
  public void testItemTypeGetterSetter() {
    Item.Type testType = Item.Type.IMAGE;
    exportData.setItemType(testType);
    assertEquals("Item type should match", testType, exportData.getItemType());
  }

  @Test
  public void testItemTypeWithNull() {
    exportData.setItemType(null);
    assertNull("Item type should be null", exportData.getItemType());
  }

  @Test
  public void testImageIDGetterSetter() {
    int testImageID = 789;
    exportData.setImageID(testImageID);
    assertEquals("Image ID should match", testImageID, exportData.getImageID());
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
  public void testSessionIDGetterSetter() {
    String testSessionID = "session_123";
    exportData.setSessionID(testSessionID);
    assertEquals("Session ID should match", testSessionID, exportData.getSessionID());
  }

  @Test
  public void testSessionIDWithNull() {
    exportData.setSessionID(null);
    assertNull("Session ID should be null", exportData.getSessionID());
  }

  @Test
  public void testSizeGetterSetter() {
    float testSize = 15.5f;
    exportData.setSize(testSize);
    assertEquals("Size should match", testSize, exportData.getSize(), 0.001f);
  }

  @Test
  public void testPressureGetterSetter() {
    float testPressure = 0.8f;
    exportData.setPressure(testPressure);
    assertEquals("Pressure should match", testPressure, exportData.getPressure(), 0.001f);
  }

  @Test
  public void testOrientationGetterSetter() {
    float testOrientation = 45.0f;
    exportData.setOrientation(testOrientation);
    assertEquals("Orientation should match", testOrientation, exportData.getOrientation(), 0.001f);
  }

  @Test
  public void testDrawingModeGetterSetter() {
    String testMode = "overlay";
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
    long time = 1234567890L;
    float x = 100.0f;
    float y = 200.0f;
    String action = "DOWN";
    int userID = 2;
    String userName = "DrawingUser";
    int attempt = 1;
    Item.Type itemType = Item.Type.IMAGE;
    int imageID = 3;
    String imageName = "drawing_test.png";
    String sessionID = "drawing_session_1";
    float size = 10.0f;
    float pressure = 0.5f;
    float orientation = 90.0f;
    String drawingMode = "overlay";

    exportData.setId(id);
    exportData.setTime(time);
    exportData.setX(x);
    exportData.setY(y);
    exportData.setAction(action);
    exportData.setUserID(userID);
    exportData.setUserName(userName);
    exportData.setAttempt(attempt);
    exportData.setItemType(itemType);
    exportData.setImageID(imageID);
    exportData.setImageName(imageName);
    exportData.setSessionID(sessionID);
    exportData.setSize(size);
    exportData.setPressure(pressure);
    exportData.setOrientation(orientation);
    exportData.setDrawingMode(drawingMode);

    assertEquals("ID should match", id, exportData.getId());
    assertEquals("Time should match", time, exportData.getTime());
    assertEquals("X should match", x, exportData.getX(), 0.001f);
    assertEquals("Y should match", y, exportData.getY(), 0.001f);
    assertEquals("Action should match", action, exportData.getAction());
    assertEquals("User ID should match", userID, exportData.getUserID());
    assertEquals("User name should match", userName, exportData.getUserName());
    assertEquals("Attempt should match", attempt, exportData.getAttempt());
    assertEquals("Item type should match", itemType, exportData.getItemType());
    assertEquals("Image ID should match", imageID, exportData.getImageID());
    assertEquals("Image name should match", imageName, exportData.getImageName());
    assertEquals("Session ID should match", sessionID, exportData.getSessionID());
    assertEquals("Size should match", size, exportData.getSize(), 0.001f);
    assertEquals("Pressure should match", pressure, exportData.getPressure(), 0.001f);
    assertEquals("Orientation should match", orientation, exportData.getOrientation(), 0.001f);
    assertEquals("Drawing mode should match", drawingMode, exportData.getDrawingMode());
  }

  @Test
  public void testZeroValues() {
    exportData.setId(0);
    exportData.setTime(0L);
    exportData.setX(0.0f);
    exportData.setY(0.0f);
    exportData.setUserID(0);
    exportData.setAttempt(0);
    exportData.setImageID(0);
    exportData.setSize(0.0f);
    exportData.setPressure(0.0f);
    exportData.setOrientation(0.0f);

    assertEquals("Zero ID should be handled", 0, exportData.getId());
    assertEquals("Zero time should be handled", 0L, exportData.getTime());
    assertEquals("Zero X should be handled", 0.0f, exportData.getX(), 0.001f);
    assertEquals("Zero Y should be handled", 0.0f, exportData.getY(), 0.001f);
    assertEquals("Zero user ID should be handled", 0, exportData.getUserID());
    assertEquals("Zero attempt should be handled", 0, exportData.getAttempt());
    assertEquals("Zero image ID should be handled", 0, exportData.getImageID());
    assertEquals("Zero size should be handled", 0.0f, exportData.getSize(), 0.001f);
    assertEquals("Zero pressure should be handled", 0.0f, exportData.getPressure(), 0.001f);
    assertEquals("Zero orientation should be handled", 0.0f, exportData.getOrientation(), 0.001f);
  }

  @Test
  public void testNegativeValues() {
    exportData.setId(-1);
    exportData.setTime(-1L);
    exportData.setX(-100.0f);
    exportData.setY(-200.0f);
    exportData.setUserID(-1);
    exportData.setAttempt(-1);
    exportData.setImageID(-1);
    exportData.setSize(-10.0f);
    exportData.setPressure(-0.5f);
    exportData.setOrientation(-90.0f);

    assertEquals("Negative ID should be handled", -1, exportData.getId());
    assertEquals("Negative time should be handled", -1L, exportData.getTime());
    assertEquals("Negative X should be handled", -100.0f, exportData.getX(), 0.001f);
    assertEquals("Negative Y should be handled", -200.0f, exportData.getY(), 0.001f);
    assertEquals("Negative user ID should be handled", -1, exportData.getUserID());
    assertEquals("Negative attempt should be handled", -1, exportData.getAttempt());
    assertEquals("Negative image ID should be handled", -1, exportData.getImageID());
    assertEquals("Negative size should be handled", -10.0f, exportData.getSize(), 0.001f);
    assertEquals("Negative pressure should be handled", -0.5f, exportData.getPressure(), 0.001f);
    assertEquals(
        "Negative orientation should be handled",
        -90.0f,
        exportData.getOrientation(),
        0.001f
    );
  }

  @Test
  public void testActionConstants() {
    // Test common action values
    String[] actions = {"DOWN", "MOVE", "UP", "START", "END"};

    for (String action : actions) {
      exportData.setAction(action);
      assertEquals("Action should match", action, exportData.getAction());
    }
  }

  @Test
  public void testEmptyStrings() {
    exportData.setAction("");
    exportData.setUserName("");
    exportData.setImageName("");
    exportData.setSessionID("");
    exportData.setDrawingMode("");

    assertEquals("Empty action should be handled", "", exportData.getAction());
    assertEquals("Empty user name should be handled", "", exportData.getUserName());
    assertEquals("Empty image name should be handled", "", exportData.getImageName());
    assertEquals("Empty session ID should be handled", "", exportData.getSessionID());
    assertEquals("Empty drawing mode should be handled", "", exportData.getDrawingMode());
  }
} 

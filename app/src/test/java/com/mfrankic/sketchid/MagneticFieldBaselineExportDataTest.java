package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MagneticFieldBaselineExportData.
 * Tests all getters, setters, and default value behavior.
 */
public class MagneticFieldBaselineExportDataTest {

  private MagneticFieldBaselineExportData exportData;

  @Before
  public void setup() {
    exportData = new MagneticFieldBaselineExportData();
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull("Export data should be created", exportData);
  }

  @Test
  public void testIdGetterSetter() {
    long testId = 123L;
    exportData.setId(testId);
    assertEquals("ID should match", testId, exportData.getId());
  }

  @Test
  public void testUserIdGetterSetter() {
    long testUserId = 456L;
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
  public void testAvgXGetterSetter() {
    float testAvgX = 15.5f; // Typical magnetic field value in µT
    exportData.setAvgX(testAvgX);
    assertEquals("Average X should match", testAvgX, exportData.getAvgX(), 0.001f);
  }

  @Test
  public void testAvgYGetterSetter() {
    float testAvgY = -8.2f;
    exportData.setAvgY(testAvgY);
    assertEquals("Average Y should match", testAvgY, exportData.getAvgY(), 0.001f);
  }

  @Test
  public void testAvgZGetterSetter() {
    float testAvgZ = 42.7f;
    exportData.setAvgZ(testAvgZ);
    assertEquals("Average Z should match", testAvgZ, exportData.getAvgZ(), 0.001f);
  }

  @Test
  public void testDurationMsGetterSetter() {
    long testDuration = 5000L; // 5 seconds
    exportData.setDurationMs(testDuration);
    assertEquals("Duration should match", testDuration, exportData.getDurationMs());
  }

  @Test
  public void testSampleCountGetterSetter() {
    int testSampleCount = 150;
    exportData.setSampleCount(testSampleCount);
    assertEquals("Sample count should match", testSampleCount, exportData.getSampleCount());
  }

  @Test
  public void testStdDevXGetterSetter() {
    float testStdDevX = 2.1f;
    exportData.setStdDevX(testStdDevX);
    assertEquals("Standard deviation X should match", testStdDevX, exportData.getStdDevX(), 0.001f);
  }

  @Test
  public void testStdDevYGetterSetter() {
    float testStdDevY = 1.8f;
    exportData.setStdDevY(testStdDevY);
    assertEquals("Standard deviation Y should match", testStdDevY, exportData.getStdDevY(), 0.001f);
  }

  @Test
  public void testStdDevZGetterSetter() {
    float testStdDevZ = 3.4f;
    exportData.setStdDevZ(testStdDevZ);
    assertEquals("Standard deviation Z should match", testStdDevZ, exportData.getStdDevZ(), 0.001f);
  }

  @Test
  public void testDrawingModeGetterSetter() {
    String testMode = "baseline_mode";
    exportData.setDrawingMode(testMode);
    assertEquals("Drawing mode should match", testMode, exportData.getDrawingMode());
  }

  @Test
  public void testDrawingModeDefaultValue() {
    // When drawing mode is set to null, it should be null (no default fallback in getter)
    exportData.setDrawingMode(null);
    assertNull("Drawing mode should be null when set to null", exportData.getDrawingMode());
  }

  @Test
  public void testDrawingModeInitialValue() {
    // Initial value should be default as field is initialized
    assertEquals(
        "Initial drawing mode should be default",
        Constants.DRAWING_MODE_NORMAL,
        exportData.getDrawingMode()
    );
  }

  @Test
  public void testAllFieldsTogether() {
    // Test setting all fields at once
    long id = 1L;
    long userId = 2L;
    String userName = "MagneticUser";
    int imageId = 3;
    String imageName = "magnetic_test.png";
    String sessionId = "magnetic_session_1";
    int attempt = 1;
    long timestamp = 1234567890L;
    float avgX = 15.5f;
    float avgY = -8.2f;
    float avgZ = 42.7f;
    long durationMs = 5000L;
    int sampleCount = 150;
    float stdDevX = 2.1f;
    float stdDevY = 1.8f;
    float stdDevZ = 3.4f;
    String drawingMode = "baseline_mode";

    exportData.setId(id);
    exportData.setUserId(userId);
    exportData.setUserName(userName);
    exportData.setImageId(imageId);
    exportData.setImageName(imageName);
    exportData.setSessionId(sessionId);
    exportData.setAttempt(attempt);
    exportData.setTimestamp(timestamp);
    exportData.setAvgX(avgX);
    exportData.setAvgY(avgY);
    exportData.setAvgZ(avgZ);
    exportData.setDurationMs(durationMs);
    exportData.setSampleCount(sampleCount);
    exportData.setStdDevX(stdDevX);
    exportData.setStdDevY(stdDevY);
    exportData.setStdDevZ(stdDevZ);
    exportData.setDrawingMode(drawingMode);

    assertEquals("ID should match", id, exportData.getId());
    assertEquals("User ID should match", userId, exportData.getUserId());
    assertEquals("User name should match", userName, exportData.getUserName());
    assertEquals("Image ID should match", imageId, exportData.getImageId());
    assertEquals("Image name should match", imageName, exportData.getImageName());
    assertEquals("Session ID should match", sessionId, exportData.getSessionId());
    assertEquals("Attempt should match", attempt, exportData.getAttempt());
    assertEquals("Timestamp should match", timestamp, exportData.getTimestamp());
    assertEquals("Average X should match", avgX, exportData.getAvgX(), 0.001f);
    assertEquals("Average Y should match", avgY, exportData.getAvgY(), 0.001f);
    assertEquals("Average Z should match", avgZ, exportData.getAvgZ(), 0.001f);
    assertEquals("Duration should match", durationMs, exportData.getDurationMs());
    assertEquals("Sample count should match", sampleCount, exportData.getSampleCount());
    assertEquals("Standard deviation X should match", stdDevX, exportData.getStdDevX(), 0.001f);
    assertEquals("Standard deviation Y should match", stdDevY, exportData.getStdDevY(), 0.001f);
    assertEquals("Standard deviation Z should match", stdDevZ, exportData.getStdDevZ(), 0.001f);
    assertEquals("Drawing mode should match", drawingMode, exportData.getDrawingMode());
  }

  @Test
  public void testZeroValues() {
    exportData.setId(0L);
    exportData.setUserId(0L);
    exportData.setImageId(0);
    exportData.setAttempt(0);
    exportData.setTimestamp(0L);
    exportData.setAvgX(0.0f);
    exportData.setAvgY(0.0f);
    exportData.setAvgZ(0.0f);
    exportData.setDurationMs(0L);
    exportData.setSampleCount(0);
    exportData.setStdDevX(0.0f);
    exportData.setStdDevY(0.0f);
    exportData.setStdDevZ(0.0f);

    assertEquals("Zero ID should be handled", 0L, exportData.getId());
    assertEquals("Zero user ID should be handled", 0L, exportData.getUserId());
    assertEquals("Zero image ID should be handled", 0, exportData.getImageId());
    assertEquals("Zero attempt should be handled", 0, exportData.getAttempt());
    assertEquals("Zero timestamp should be handled", 0L, exportData.getTimestamp());
    assertEquals("Zero average X should be handled", 0.0f, exportData.getAvgX(), 0.001f);
    assertEquals("Zero average Y should be handled", 0.0f, exportData.getAvgY(), 0.001f);
    assertEquals("Zero average Z should be handled", 0.0f, exportData.getAvgZ(), 0.001f);
    assertEquals("Zero duration should be handled", 0L, exportData.getDurationMs());
    assertEquals("Zero sample count should be handled", 0, exportData.getSampleCount());
    assertEquals("Zero std dev X should be handled", 0.0f, exportData.getStdDevX(), 0.001f);
    assertEquals("Zero std dev Y should be handled", 0.0f, exportData.getStdDevY(), 0.001f);
    assertEquals("Zero std dev Z should be handled", 0.0f, exportData.getStdDevZ(), 0.001f);
  }

  @Test
  public void testNegativeValues() {
    exportData.setId(-1L);
    exportData.setUserId(-2L);
    exportData.setImageId(-3);
    exportData.setAttempt(-1);
    exportData.setTimestamp(-1L);
    exportData.setAvgX(-15.5f);
    exportData.setAvgY(-8.2f);
    exportData.setAvgZ(-42.7f);
    exportData.setDurationMs(-1000L);
    exportData.setSampleCount(-10);
    exportData.setStdDevX(-2.1f);
    exportData.setStdDevY(-1.8f);
    exportData.setStdDevZ(-3.4f);

    assertEquals("Negative ID should be handled", -1L, exportData.getId());
    assertEquals("Negative user ID should be handled", -2L, exportData.getUserId());
    assertEquals("Negative image ID should be handled", -3, exportData.getImageId());
    assertEquals("Negative attempt should be handled", -1, exportData.getAttempt());
    assertEquals("Negative timestamp should be handled", -1L, exportData.getTimestamp());
    assertEquals("Negative average X should be handled", -15.5f, exportData.getAvgX(), 0.001f);
    assertEquals("Negative average Y should be handled", -8.2f, exportData.getAvgY(), 0.001f);
    assertEquals("Negative average Z should be handled", -42.7f, exportData.getAvgZ(), 0.001f);
    assertEquals("Negative duration should be handled", -1000L, exportData.getDurationMs());
    assertEquals("Negative sample count should be handled", -10, exportData.getSampleCount());
    assertEquals("Negative std dev X should be handled", -2.1f, exportData.getStdDevX(), 0.001f);
    assertEquals("Negative std dev Y should be handled", -1.8f, exportData.getStdDevY(), 0.001f);
    assertEquals("Negative std dev Z should be handled", -3.4f, exportData.getStdDevZ(), 0.001f);
  }

  @Test
  public void testExtremeValues() {
    exportData.setId(Long.MAX_VALUE);
    exportData.setUserId(Long.MIN_VALUE);
    exportData.setImageId(Integer.MAX_VALUE);
    exportData.setAttempt(Integer.MIN_VALUE);
    exportData.setTimestamp(Long.MAX_VALUE);
    exportData.setAvgX(Float.MAX_VALUE);
    exportData.setAvgY(Float.MIN_VALUE);
    exportData.setAvgZ(-Float.MAX_VALUE);
    exportData.setDurationMs(Long.MAX_VALUE);
    exportData.setSampleCount(Integer.MAX_VALUE);
    exportData.setStdDevX(Float.MAX_VALUE);
    exportData.setStdDevY(Float.MIN_VALUE);
    exportData.setStdDevZ(-Float.MAX_VALUE);

    assertEquals("Max long ID should be handled", Long.MAX_VALUE, exportData.getId());
    assertEquals("Min long user ID should be handled", Long.MIN_VALUE, exportData.getUserId());
    assertEquals("Max int image ID should be handled", Integer.MAX_VALUE, exportData.getImageId());
    assertEquals("Min int attempt should be handled", Integer.MIN_VALUE, exportData.getAttempt());
    assertEquals("Max long timestamp should be handled", Long.MAX_VALUE, exportData.getTimestamp());
    assertEquals(
        "Max float average X should be handled",
        Float.MAX_VALUE,
        exportData.getAvgX(),
        0.001f
    );
    assertEquals(
        "Min float average Y should be handled",
        Float.MIN_VALUE,
        exportData.getAvgY(),
        0.001f
    );
    assertEquals(
        "Negative max float average Z should be handled",
        -Float.MAX_VALUE,
        exportData.getAvgZ(),
        0.001f
    );
    assertEquals("Max long duration should be handled", Long.MAX_VALUE, exportData.getDurationMs());
    assertEquals(
        "Max int sample count should be handled",
        Integer.MAX_VALUE,
        exportData.getSampleCount()
    );
    assertEquals(
        "Max float std dev X should be handled",
        Float.MAX_VALUE,
        exportData.getStdDevX(),
        0.001f
    );
    assertEquals(
        "Min float std dev Y should be handled",
        Float.MIN_VALUE,
        exportData.getStdDevY(),
        0.001f
    );
    assertEquals(
        "Negative max float std dev Z should be handled",
        -Float.MAX_VALUE,
        exportData.getStdDevZ(),
        0.001f
    );
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

  @Test
  public void testRealisticMagneticFieldValues() {
    // Test realistic magnetic field baseline values
    float earthMagneticX = 20.0f; // µT
    float earthMagneticY = -5.0f; // µT
    float earthMagneticZ = 45.0f; // µT (typical earth's magnetic field)
    long duration = 3000L; // 3 seconds baseline
    int samples = 120; // 120 samples in 3 seconds
    float lowStdDev = 0.5f; // Low standard deviation for stable baseline

    exportData.setAvgX(earthMagneticX);
    exportData.setAvgY(earthMagneticY);
    exportData.setAvgZ(earthMagneticZ);
    exportData.setDurationMs(duration);
    exportData.setSampleCount(samples);
    exportData.setStdDevX(lowStdDev);
    exportData.setStdDevY(lowStdDev);
    exportData.setStdDevZ(lowStdDev);

    assertEquals(
        "Earth magnetic X should be handled",
        earthMagneticX,
        exportData.getAvgX(),
        0.001f
    );
    assertEquals(
        "Earth magnetic Y should be handled",
        earthMagneticY,
        exportData.getAvgY(),
        0.001f
    );
    assertEquals(
        "Earth magnetic Z should be handled",
        earthMagneticZ,
        exportData.getAvgZ(),
        0.001f
    );
    assertEquals("Baseline duration should be handled", duration, exportData.getDurationMs());
    assertEquals("Sample count should be handled", samples, exportData.getSampleCount());
    assertEquals("Low std dev X should be handled", lowStdDev, exportData.getStdDevX(), 0.001f);
    assertEquals("Low std dev Y should be handled", lowStdDev, exportData.getStdDevY(), 0.001f);
    assertEquals("Low std dev Z should be handled", lowStdDev, exportData.getStdDevZ(), 0.001f);
  }
} 

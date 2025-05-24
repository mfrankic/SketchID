package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MagneticFieldBaselineData.
 * Tests constructor, field access, and data integrity.
 */
public class MagneticFieldBaselineDataTest {

  private MagneticFieldBaselineData baselineData;

  @Before
  public void setup() {
    // Create with typical magnetic field baseline values
    baselineData = new MagneticFieldBaselineData(
        1L,             // userId
        123,            // imageId
        "session_1",    // sessionId
        1,              // attempt
        System.currentTimeMillis(), // timestamp
        15.5f,          // avgX (typical magnetic field in µT)
        -8.2f,          // avgY
        42.7f,          // avgZ
        3000L,          // durationMs (3 seconds)
        120,            // sampleCount
        0.5f,           // stdDevX (low std dev for stable baseline)
        0.7f,           // stdDevY
        0.3f            // stdDevZ
    );
  }

  @Test
  public void testConstructorWithValidParameters() {
    long userId = 2L;
    int imageId = 456;
    String sessionId = "test_session";
    int attempt = 2;
    long timestamp = 1234567890L;
    float avgX = 20.0f;
    float avgY = -10.5f;
    float avgZ = 45.8f;
    long durationMs = 5000L;
    int sampleCount = 200;
    float stdDevX = 1.2f;
    float stdDevY = 0.9f;
    float stdDevZ = 1.5f;

    MagneticFieldBaselineData data = new MagneticFieldBaselineData(
        userId,
        imageId,
        sessionId,
        attempt,
        timestamp,
        avgX,
        avgY,
        avgZ,
        durationMs,
        sampleCount,
        stdDevX,
        stdDevY,
        stdDevZ
    );

    assertEquals("User ID should match", userId, data.userId);
    assertEquals("Image ID should match", imageId, data.imageId);
    assertEquals("Session ID should match", sessionId, data.sessionId);
    assertEquals("Attempt should match", attempt, data.attempt);
    assertEquals("Timestamp should match", timestamp, data.timestamp);
    assertEquals("Average X should match", avgX, data.avgX, 0.001f);
    assertEquals("Average Y should match", avgY, data.avgY, 0.001f);
    assertEquals("Average Z should match", avgZ, data.avgZ, 0.001f);
    assertEquals("Duration should match", durationMs, data.durationMs);
    assertEquals("Sample count should match", sampleCount, data.sampleCount);
    assertEquals("Standard deviation X should match", stdDevX, data.stdDevX, 0.001f);
    assertEquals("Standard deviation Y should match", stdDevY, data.stdDevY, 0.001f);
    assertEquals("Standard deviation Z should match", stdDevZ, data.stdDevZ, 0.001f);
  }

  @Test
  public void testFieldAccess() {
    // Test that all public fields are accessible
    assertEquals("User ID should be accessible", 1L, baselineData.userId);
    assertEquals("Image ID should be accessible", 123, baselineData.imageId);
    assertEquals("Session ID should be accessible", "session_1", baselineData.sessionId);
    assertEquals("Attempt should be accessible", 1, baselineData.attempt);
    assertTrue("Timestamp should be positive", baselineData.timestamp > 0);
    assertEquals("Average X should be accessible", 15.5f, baselineData.avgX, 0.001f);
    assertEquals("Average Y should be accessible", -8.2f, baselineData.avgY, 0.001f);
    assertEquals("Average Z should be accessible", 42.7f, baselineData.avgZ, 0.001f);
    assertEquals("Duration should be accessible", 3000L, baselineData.durationMs);
    assertEquals("Sample count should be accessible", 120, baselineData.sampleCount);
    assertEquals("Standard deviation X should be accessible", 0.5f, baselineData.stdDevX, 0.001f);
    assertEquals("Standard deviation Y should be accessible", 0.7f, baselineData.stdDevY, 0.001f);
    assertEquals("Standard deviation Z should be accessible", 0.3f, baselineData.stdDevZ, 0.001f);
  }

  @Test
  public void testFieldModification() {
    // Test that public fields can be modified
    baselineData.userId = 999L;
    baselineData.imageId = 888;
    baselineData.sessionId = "modified_session";
    baselineData.attempt = 5;
    baselineData.timestamp = 9876543210L;
    baselineData.avgX = 100.0f;
    baselineData.avgY = -50.0f;
    baselineData.avgZ = 75.5f;
    baselineData.durationMs = 10000L;
    baselineData.sampleCount = 500;
    baselineData.stdDevX = 5.0f;
    baselineData.stdDevY = 3.2f;
    baselineData.stdDevZ = 4.1f;

    assertEquals("Modified user ID should persist", 999L, baselineData.userId);
    assertEquals("Modified image ID should persist", 888, baselineData.imageId);
    assertEquals("Modified session ID should persist", "modified_session", baselineData.sessionId);
    assertEquals("Modified attempt should persist", 5, baselineData.attempt);
    assertEquals("Modified timestamp should persist", 9876543210L, baselineData.timestamp);
    assertEquals("Modified average X should persist", 100.0f, baselineData.avgX, 0.001f);
    assertEquals("Modified average Y should persist", -50.0f, baselineData.avgY, 0.001f);
    assertEquals("Modified average Z should persist", 75.5f, baselineData.avgZ, 0.001f);
    assertEquals("Modified duration should persist", 10000L, baselineData.durationMs);
    assertEquals("Modified sample count should persist", 500, baselineData.sampleCount);
    assertEquals("Modified std dev X should persist", 5.0f, baselineData.stdDevX, 0.001f);
    assertEquals("Modified std dev Y should persist", 3.2f, baselineData.stdDevY, 0.001f);
    assertEquals("Modified std dev Z should persist", 4.1f, baselineData.stdDevZ, 0.001f);
  }

  @Test
  public void testZeroValues() {
    MagneticFieldBaselineData zeroData = new MagneticFieldBaselineData(
        0L,
                                                                       0,
                                                                       null,
                                                                       0,
                                                                       0L,
                                                                       0.0f,
                                                                       0.0f,
                                                                       0.0f,
                                                                       0L,
                                                                       0,
                                                                       0.0f,
                                                                       0.0f,
                                                                       0.0f
    );

    assertEquals("Zero user ID should be handled", 0L, zeroData.userId);
    assertEquals("Zero image ID should be handled", 0, zeroData.imageId);
    assertNull("Null session ID should be handled", zeroData.sessionId);
    assertEquals("Zero attempt should be handled", 0, zeroData.attempt);
    assertEquals("Zero timestamp should be handled", 0L, zeroData.timestamp);
    assertEquals("Zero average X should be handled", 0.0f, zeroData.avgX, 0.001f);
    assertEquals("Zero average Y should be handled", 0.0f, zeroData.avgY, 0.001f);
    assertEquals("Zero average Z should be handled", 0.0f, zeroData.avgZ, 0.001f);
    assertEquals("Zero duration should be handled", 0L, zeroData.durationMs);
    assertEquals("Zero sample count should be handled", 0, zeroData.sampleCount);
    assertEquals("Zero std dev X should be handled", 0.0f, zeroData.stdDevX, 0.001f);
    assertEquals("Zero std dev Y should be handled", 0.0f, zeroData.stdDevY, 0.001f);
    assertEquals("Zero std dev Z should be handled", 0.0f, zeroData.stdDevZ, 0.001f);
  }

  @Test
  public void testNegativeValues() {
    MagneticFieldBaselineData negativeData = new MagneticFieldBaselineData(
        -1L,
                                                                           -2,
                                                                           "negative_test",
                                                                           -1,
                                                                           -1L,
                                                                           -25.5f,
                                                                           -15.2f,
                                                                           -35.7f,
                                                                           -1000L,
                                                                           -50,
                                                                           -2.5f,
                                                                           -1.8f,
                                                                           -3.2f
    );

    assertEquals("Negative user ID should be handled", -1L, negativeData.userId);
    assertEquals("Negative image ID should be handled", -2, negativeData.imageId);
    assertEquals("Negative session ID should be handled", "negative_test", negativeData.sessionId);
    assertEquals("Negative attempt should be handled", -1, negativeData.attempt);
    assertEquals("Negative timestamp should be handled", -1L, negativeData.timestamp);
    assertEquals("Negative average X should be handled", -25.5f, negativeData.avgX, 0.001f);
    assertEquals("Negative average Y should be handled", -15.2f, negativeData.avgY, 0.001f);
    assertEquals("Negative average Z should be handled", -35.7f, negativeData.avgZ, 0.001f);
    assertEquals("Negative duration should be handled", -1000L, negativeData.durationMs);
    assertEquals("Negative sample count should be handled", -50, negativeData.sampleCount);
    assertEquals("Negative std dev X should be handled", -2.5f, negativeData.stdDevX, 0.001f);
    assertEquals("Negative std dev Y should be handled", -1.8f, negativeData.stdDevY, 0.001f);
    assertEquals("Negative std dev Z should be handled", -3.2f, negativeData.stdDevZ, 0.001f);
  }

  @Test
  public void testExtremeValues() {
    MagneticFieldBaselineData extremeData = new MagneticFieldBaselineData(
        Long.MAX_VALUE,
        Integer.MAX_VALUE,
        "extreme_test",
        Integer.MAX_VALUE,
        Long.MAX_VALUE,
        Float.MAX_VALUE,
        Float.MIN_VALUE,
        -Float.MAX_VALUE,
        Long.MAX_VALUE,
        Integer.MAX_VALUE,
        Float.MAX_VALUE,
        Float.MIN_VALUE,
        -Float.MAX_VALUE
    );

    assertEquals("Max long user ID should be handled", Long.MAX_VALUE, extremeData.userId);
    assertEquals("Max int image ID should be handled", Integer.MAX_VALUE, extremeData.imageId);
    assertEquals("Extreme session ID should be handled", "extreme_test", extremeData.sessionId);
    assertEquals("Max int attempt should be handled", Integer.MAX_VALUE, extremeData.attempt);
    assertEquals("Max long timestamp should be handled", Long.MAX_VALUE, extremeData.timestamp);
    assertEquals(
        "Max float average X should be handled",
        Float.MAX_VALUE,
        extremeData.avgX,
        0.001f
    );
    assertEquals(
        "Min float average Y should be handled",
        Float.MIN_VALUE,
        extremeData.avgY,
        0.001f
    );
    assertEquals(
        "Negative max float average Z should be handled",
        -Float.MAX_VALUE,
        extremeData.avgZ,
        0.001f
    );
    assertEquals("Max long duration should be handled", Long.MAX_VALUE, extremeData.durationMs);
    assertEquals(
        "Max int sample count should be handled",
        Integer.MAX_VALUE,
        extremeData.sampleCount
    );
    assertEquals(
        "Max float std dev X should be handled",
        Float.MAX_VALUE,
        extremeData.stdDevX,
        0.001f
    );
    assertEquals(
        "Min float std dev Y should be handled",
        Float.MIN_VALUE,
        extremeData.stdDevY,
        0.001f
    );
    assertEquals(
        "Negative max float std dev Z should be handled",
        -Float.MAX_VALUE,
        extremeData.stdDevZ,
        0.001f
    );
  }

  @Test
  public void testRealisticMagneticFieldScenarios() {
    // Test with realistic earth's magnetic field values
    MagneticFieldBaselineData earthMagneticData = new MagneticFieldBaselineData(
        10L, 50, "earth_field_test", 1, System.currentTimeMillis(), 22.0f,
        // µT - typical horizontal component
        -5.5f,
        // µT - declination component
        48.0f,
        // µT - typical total intensity
        5000L,
        // 5 second baseline
        250,
        // 50Hz sampling for 5 seconds
        0.8f,
        // µT - realistic noise level
        0.6f,
        // µT
        0.9f
        // µT
    );

    assertTrue("Realistic user ID should be positive", earthMagneticData.userId > 0);
    assertTrue("Realistic image ID should be positive", earthMagneticData.imageId > 0);
    assertNotNull("Session ID should not be null", earthMagneticData.sessionId);
    assertTrue("Attempt should be positive", earthMagneticData.attempt > 0);
    assertTrue("Timestamp should be recent", earthMagneticData.timestamp > 0);
    assertTrue("Earth magnetic X should be reasonable", Math.abs(earthMagneticData.avgX) < 100.0f);
    assertTrue("Earth magnetic Y should be reasonable", Math.abs(earthMagneticData.avgY) < 100.0f);
    assertTrue("Earth magnetic Z should be reasonable", Math.abs(earthMagneticData.avgZ) < 100.0f);
    assertTrue(
        "Baseline duration should be reasonable",
        earthMagneticData.durationMs > 0 && earthMagneticData.durationMs < 60000
    );
    assertTrue(
        "Sample count should be reasonable",
        earthMagneticData.sampleCount > 0 && earthMagneticData.sampleCount < 10000
    );
    assertTrue(
        "Standard deviations should be small for stable baseline",
        earthMagneticData.stdDevX < 5.0f
        && earthMagneticData.stdDevY < 5.0f
        && earthMagneticData.stdDevZ < 5.0f
    );
  }

  @Test
  public void testEmptyStringSessionId() {
    MagneticFieldBaselineData emptyStringData = new MagneticFieldBaselineData(
        1L,
                                                                              1,
                                                                              "",
                                                                              1,
                                                                              1L,
                                                                              1.0f,
                                                                              1.0f,
                                                                              1.0f,
                                                                              1L,
                                                                              1,
                                                                              1.0f,
                                                                              1.0f,
                                                                              1.0f
    );

    assertEquals("Empty string session ID should be handled", "", emptyStringData.sessionId);
  }

  @Test
  public void testIdFieldInitialization() {
    // ID field should start at 0 (auto-generated by Room)
    assertEquals("ID field should be initialized to 0", 0L, baselineData.id);

    // Test that id can be set
    baselineData.id = 12345L;
    assertEquals("ID field should be modifiable", 12345L, baselineData.id);
  }

  @Test
  public void testLongStringSessionId() {
    String longSessionId
        =
        "very_long_session_id_that_contains_many_characters_and_might_test_string_handling_limitations";
    MagneticFieldBaselineData longStringData = new MagneticFieldBaselineData(
        1L,
                                                                             1,
                                                                             longSessionId,
                                                                             1,
                                                                             1L,
                                                                             1.0f,
                                                                             1.0f,
                                                                             1.0f,
                                                                             1L,
                                                                             1,
                                                                             1.0f,
                                                                             1.0f,
                                                                             1.0f
    );

    assertEquals(
        "Long string session ID should be handled",
        longSessionId,
        longStringData.sessionId
    );
  }
} 

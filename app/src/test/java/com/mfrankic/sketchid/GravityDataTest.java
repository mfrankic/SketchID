package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for GravityData Room entity class.
 * Tests constructors, field assignments, and data integrity for gravity sensor data.
 */
public class GravityDataTest {

  @Test
  public void testDefaultConstructor() {
    GravityData data = new GravityData();
    assertNotNull("Data object should not be null", data);

    // Test default values for all fields
    assertEquals("Default id should be 0", 0, data.id);
    assertEquals("Default userId should be 0", 0, data.userId);
    assertEquals("Default imageId should be 0", 0, data.imageId);
    assertNull("Default sessionId should be null", data.sessionId);
    assertEquals("Default attempt should be 0", 0, data.attempt);
    assertEquals("Default timestamp should be 0", 0L, data.timestamp);
    assertEquals("Default x should be 0.0", 0.0f, data.x, 0.001f);
    assertEquals("Default y should be 0.0", 0.0f, data.y, 0.001f);
    assertEquals("Default z should be 0.0", 0.0f, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructor() {
    int userId = 789;
    int imageId = 101;
    String sessionId = "gravity_session_456";
    int attempt = 2;
    long timestamp = System.currentTimeMillis();
    float x = 0.5f;   // X-axis gravity component
    float y = 2.3f;   // Y-axis gravity component
    float z = 9.3f;   // Z-axis gravity component (close to 9.8 m/s²)

    GravityData data = new GravityData(userId, imageId, sessionId, attempt, timestamp, x, y, z);

    assertNotNull("Data object should not be null", data);

    // ID should remain 0 as it's auto-generated
    assertEquals("ID should be default (auto-generated)", 0, data.id);

    // All other fields should be set correctly
    assertEquals("UserId should be set", userId, data.userId);
    assertEquals("ImageId should be set", imageId, data.imageId);
    assertEquals("SessionId should be set", sessionId, data.sessionId);
    assertEquals("Attempt should be set", attempt, data.attempt);
    assertEquals("Timestamp should be set", timestamp, data.timestamp);
    assertEquals("X should be set", x, data.x, 0.001f);
    assertEquals("Y should be set", y, data.y, 0.001f);
    assertEquals("Z should be set", z, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithRealisticGravityValues() {
    // Test with typical gravity sensor values (Earth's gravity ~ 9.8 m/s²)
    GravityData earthGravity = new GravityData(
        1, 1, "earth_gravity", 1, System.currentTimeMillis(), 0.2f,
        // Small X component (device slightly tilted)
        1.5f,
        // Y component (device tilted more in Y)
        9.6f
        // Dominant Z component (close to Earth's gravity)
    );

    assertEquals("Earth gravity X", 0.2f, earthGravity.x, 0.001f);
    assertEquals("Earth gravity Y", 1.5f, earthGravity.y, 0.001f);
    assertEquals("Earth gravity Z", 9.6f, earthGravity.z, 0.001f);

    // Calculate total magnitude (should be close to 9.8 m/s²)
    float magnitude = (float) Math.sqrt(earthGravity.x * earthGravity.x
                                        + earthGravity.y * earthGravity.y
                                        + earthGravity.z * earthGravity.z);
    assertTrue(
        "Gravity magnitude should be close to Earth's gravity (9.8 m/s²)",
        magnitude >= 9.0f && magnitude <= 10.5f
    );
  }

  @Test
  public void testParameterizedConstructorWithBoundaryValues() {
    // Test with minimum values
    GravityData data1 = new GravityData(0, 0, "", 0, 0L, 0.0f, 0.0f, 0.0f);
    assertEquals("Min userId should work", 0, data1.userId);
    assertEquals("Min imageId should work", 0, data1.imageId);
    assertEquals("Empty sessionId should work", "", data1.sessionId);
    assertEquals("Min attempt should work", 0, data1.attempt);
    assertEquals("Min timestamp should work", 0L, data1.timestamp);
    assertEquals("Zero X should work", 0.0f, data1.x, 0.001f);
    assertEquals("Zero Y should work", 0.0f, data1.y, 0.001f);
    assertEquals("Zero Z should work", 0.0f, data1.z, 0.001f);

    // Test with maximum/large values
    GravityData data2 = new GravityData(
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        "extremely_long_gravity_session_id_with_many_characters_12345",
        Integer.MAX_VALUE,
        Long.MAX_VALUE,
        Float.MAX_VALUE,
        Float.MAX_VALUE,
        Float.MAX_VALUE
    );
    assertEquals("Max userId should work", Integer.MAX_VALUE, data2.userId);
    assertEquals("Max imageId should work", Integer.MAX_VALUE, data2.imageId);
    assertEquals(
        "Long sessionId should work",
        "extremely_long_gravity_session_id_with_many_characters_12345",
        data2.sessionId
    );
    assertEquals("Max attempt should work", Integer.MAX_VALUE, data2.attempt);
    assertEquals("Max timestamp should work", Long.MAX_VALUE, data2.timestamp);
    assertEquals("Max X should work", Float.MAX_VALUE, data2.x, 0.1f);
    assertEquals("Max Y should work", Float.MAX_VALUE, data2.y, 0.1f);
    assertEquals("Max Z should work", Float.MAX_VALUE, data2.z, 0.1f);
  }

  @Test
  public void testParameterizedConstructorWithNegativeValues() {
    // Gravity can have negative components depending on device orientation
    GravityData data = new GravityData(-1, -2, "inverted_gravity", -3, -4L, -2.5f, -1.2f, -9.8f);

    assertEquals("Negative userId should be allowed", -1, data.userId);
    assertEquals("Negative imageId should be allowed", -2, data.imageId);
    assertEquals("Negative attempt should be allowed", -3, data.attempt);
    assertEquals("Negative timestamp should be allowed", -4L, data.timestamp);
    assertEquals("Negative X should be allowed", -2.5f, data.x, 0.001f);
    assertEquals("Negative Y should be allowed", -1.2f, data.y, 0.001f);
    assertEquals("Negative Z should be allowed", -9.8f, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithNullSessionId() {
    GravityData data = new GravityData(1, 2, null, 3, 4L, 5.0f, 6.0f, 7.0f);

    assertNull("Null sessionId should be allowed", data.sessionId);
    assertEquals("Other fields should be preserved with null sessionId", 1, data.userId);
  }

  @Test
  public void testParameterizedConstructorWithSpecialFloatValues() {
    // Test with NaN values
    GravityData data1 = new GravityData(
        1,
                                        2,
                                        "gravity_nan",
                                        3,
                                        4L,
                                        Float.NaN,
                                        Float.NaN,
                                        Float.NaN
    );
    assertTrue("NaN X should be preserved", Float.isNaN(data1.x));
    assertTrue("NaN Y should be preserved", Float.isNaN(data1.y));
    assertTrue("NaN Z should be preserved", Float.isNaN(data1.z));

    // Test with infinity values
    GravityData data2 = new GravityData(
        1,
                                        2,
                                        "gravity_infinity",
                                        3,
                                        4L,
                                        Float.POSITIVE_INFINITY,
                                        Float.NEGATIVE_INFINITY,
                                        Float.POSITIVE_INFINITY
    );
    assertEquals(
        "Positive infinity X should be preserved",
        Float.POSITIVE_INFINITY,
        data2.x,
        0.001f
    );
    assertEquals(
        "Negative infinity Y should be preserved",
        Float.NEGATIVE_INFINITY,
        data2.y,
        0.001f
    );
    assertEquals(
        "Positive infinity Z should be preserved",
        Float.POSITIVE_INFINITY,
        data2.z,
        0.001f
    );
  }

  @Test
  public void testFieldAssignments() {
    GravityData data = new GravityData();

    // Test setting ID (even though it's typically auto-generated)
    data.id = 500;
    assertEquals("ID should be assignable", 500, data.id);

    // Test setting userId
    data.userId = 600;
    assertEquals("UserId should be assignable", 600, data.userId);

    // Test setting imageId
    data.imageId = 700;
    assertEquals("ImageId should be assignable", 700, data.imageId);

    // Test setting sessionId
    data.sessionId = "new_gravity_session";
    assertEquals("SessionId should be assignable", "new_gravity_session", data.sessionId);

    // Test setting attempt
    data.attempt = 8;
    assertEquals("Attempt should be assignable", 8, data.attempt);

    // Test setting timestamp
    long newTimestamp = System.currentTimeMillis();
    data.timestamp = newTimestamp;
    assertEquals("Timestamp should be assignable", newTimestamp, data.timestamp);

    // Test setting gravity components
    data.x = 1.5f;
    data.y = 3.2f;
    data.z = 9.1f;
    assertEquals("X should be assignable", 1.5f, data.x, 0.001f);
    assertEquals("Y should be assignable", 3.2f, data.y, 0.001f);
    assertEquals("Z should be assignable", 9.1f, data.z, 0.001f);
  }

  @Test
  public void testFieldIndependence() {
    GravityData data = new GravityData();

    // Setting one field should not affect others
    data.userId = 1;
    assertEquals("Setting userId should not affect other fields", 0, data.imageId);
    assertEquals("Setting userId should not affect other fields", 0, data.attempt);
    assertNull("Setting userId should not affect other fields", data.sessionId);

    data.x = 2.5f;
    assertEquals("Setting x should not affect y", 0.0f, data.y, 0.001f);
    assertEquals("Setting x should not affect z", 0.0f, data.z, 0.001f);
    assertEquals("Setting x should not affect userId", 1, data.userId);
  }

  @Test
  public void testGravityOrientationScenarios() {
    // Test different device orientations and their gravity effects

    // Portrait mode (device upright)
    GravityData portrait = new GravityData(1, 1, "portrait", 1, 1000L, 0.1f, 0.2f, 9.8f);
    assertEquals("Portrait X (minimal)", 0.1f, portrait.x, 0.001f);
    assertEquals("Portrait Y (minimal)", 0.2f, portrait.y, 0.001f);
    assertEquals("Portrait Z (dominant)", 9.8f, portrait.z, 0.001f);

    // Landscape mode (device rotated 90°)
    GravityData landscape = new GravityData(1, 2, "landscape", 1, 2000L, 9.7f, 0.3f, 0.5f);
    assertEquals("Landscape X (dominant)", 9.7f, landscape.x, 0.001f);
    assertEquals("Landscape Y (minimal)", 0.3f, landscape.y, 0.001f);
    assertEquals("Landscape Z (minimal)", 0.5f, landscape.z, 0.001f);

    // Inverted (device upside down)
    GravityData inverted = new GravityData(1, 3, "inverted", 1, 3000L, -0.2f, 0.1f, -9.7f);
    assertEquals("Inverted X (negative minimal)", -0.2f, inverted.x, 0.001f);
    assertEquals("Inverted Y (minimal)", 0.1f, inverted.y, 0.001f);
    assertEquals("Inverted Z (negative dominant)", -9.7f, inverted.z, 0.001f);
  }

  @Test
  public void testGravityMagnitudeConsistency() {
    // Test that gravity magnitude remains consistent across orientations
    GravityData[] orientations = {
        new GravityData(1, 1, "test1", 1, 1000L, 0.0f, 0.0f, 9.8f),
        new GravityData(1, 2, "test2", 1, 2000L, 9.8f, 0.0f, 0.0f),
        new GravityData(1, 3, "test3", 1, 3000L, 0.0f, 9.8f, 0.0f),
        new GravityData(1, 4, "test4", 1, 4000L, 6.9f, 6.9f, 0.0f)
    };

    for (GravityData data : orientations) {
      float magnitude = (float) Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
      assertTrue(
          "Gravity magnitude should be close to 9.8 m/s²: " + magnitude,
          magnitude >= 9.0f && magnitude <= 10.5f
      );
    }
  }

  @Test
  public void testMultipleGravityReadings() {
    // Test creating multiple gravity readings as would happen during sensor collection
    GravityData[] readings = new GravityData[5];
    long baseTimestamp = System.currentTimeMillis();

    // Simulate device gradually tilting (gravity shifts from Z to X axis)
    for (int i = 0; i < 5; i++) {
      float tiltAngle = i * 22.5f; // Degrees: 0, 22.5, 45, 67.5, 90
      float radians = (float) Math.toRadians(tiltAngle);

      readings[i] = new GravityData(
          1,                                          // same user
          1,                                          // same image
          "tilt_session",                             // same session
          1,                                          // same attempt
          baseTimestamp + (i * 50),                   // 50ms intervals
          (float) (9.8 * Math.sin(radians)),         // X increases with tilt
          0.1f,                                       // Y stays minimal
          (float) (9.8 * Math.cos(radians))          // Z decreases with tilt
      );
    }

    // Verify each reading
    for (int i = 0; i < 5; i++) {
      assertEquals("Reading " + i + " userId", 1, readings[i].userId);
      assertEquals("Reading " + i + " imageId", 1, readings[i].imageId);
      assertEquals("Reading " + i + " sessionId", "tilt_session", readings[i].sessionId);
      assertEquals("Reading " + i + " attempt", 1, readings[i].attempt);
      assertEquals("Reading " + i + " timestamp", baseTimestamp + (i * 50), readings[i].timestamp);

      // Verify magnitude is consistent (should be close to 9.8)
      float magnitude = (float) Math.sqrt(readings[i].x * readings[i].x
                                          + readings[i].y * readings[i].y
                                          + readings[i].z * readings[i].z);
      assertTrue(
          "Reading " + i + " magnitude should be close to 9.8",
          Math.abs(magnitude - 9.8f) < 0.5f
      );
    }
  }

  @Test
  public void testSessionIdVariants() {
    // Test different gravity session ID formats
    String[] sessionIds = {
        "gravity_simple",
        "gravity_session_123",
        "GRAVITY_SESSION_ABC_456",
        "gravity-uuid-550e8400-e29b-41d4-a716-446655440000",
        "Mixed_Gravity_Session_123_ABC",
        "gravity-with-dashes-and_underscores",
        "gravity123456789",
        "",
        null
    };

    for (int i = 0; i < sessionIds.length; i++) {
      GravityData data = new GravityData(
          i,
                                         i,
                                         sessionIds[i],
                                         i,
                                         i * 1000L,
                                         i * 0.5f,
                                         i * 0.3f,
                                         9.8f - (i * 0.1f)
      );

      if (sessionIds[i] == null) {
        assertNull("Null sessionId should be preserved", data.sessionId);
      } else {
        assertEquals(
            "SessionId variant " + i + " should be preserved",
            sessionIds[i],
            data.sessionId
        );
      }
    }
  }

  @Test
  public void testTimestampProgression() {
    // Test realistic gravity sensor sampling progression
    long baseTime = System.currentTimeMillis();
    int samplingRate = 100; // Hz (10ms intervals, typical for gravity sensor)
    int intervalMs = 1000 / samplingRate;

    GravityData[] samples = new GravityData[10];
    for (int i = 0; i < 10; i++) {
      samples[i] = new GravityData(
          1, 1, "gravity_sampling", 1, baseTime + (i * intervalMs),  // 10ms intervals
          0.2f, 1.1f, 9.6f              // Stable gravity reading
      );
    }

    // Verify timestamp progression
    for (int i = 1; i < samples.length; i++) {
      long expectedTimestamp = baseTime + (i * intervalMs);
      assertEquals("Timestamp progression", expectedTimestamp, samples[i].timestamp);

      long timeDiff = samples[i].timestamp - samples[i - 1].timestamp;
      assertEquals("Time interval should be consistent", intervalMs, timeDiff);
    }
  }

  @Test
  public void testDataMutability() {
    GravityData data = new GravityData(1, 2, "initial_gravity", 3, 4L, 1.0f, 2.0f, 9.8f);

    // Verify initial values
    assertEquals("Initial userId", 1, data.userId);
    assertEquals("Initial sessionId", "initial_gravity", data.sessionId);
    assertEquals("Initial X", 1.0f, data.x, 0.001f);

    // Modify values
    data.userId = 10;
    data.sessionId = "modified_gravity";
    data.x = 5.0f;

    // Verify changes
    assertEquals("Modified userId", 10, data.userId);
    assertEquals("Modified sessionId", "modified_gravity", data.sessionId);
    assertEquals("Modified X", 5.0f, data.x, 0.001f);
  }

  @Test
  public void testFloatPrecision() {
    GravityData data = new GravityData();

    // Test very small gravity values
    data.x = 0.000001f;
    data.y = -0.000001f;
    data.z = 9.800001f;

    assertEquals("Very small X", 0.000001f, data.x, 0.0000001f);
    assertEquals("Very small Y", -0.000001f, data.y, 0.0000001f);
    assertEquals("Precise Z", 9.800001f, data.z, 0.0000001f);

    // Test precise decimal values common in gravity readings
    data.x = 1.23456789f;
    data.y = 2.34567891f;
    data.z = 9.80665f;    // Standard gravity value

    assertEquals("Precise X", 1.23456789f, data.x, 0.0001f);
    assertEquals("Precise Y", 2.34567891f, data.y, 0.0001f);
    assertEquals("Standard gravity Z", 9.80665f, data.z, 0.0001f);
  }

  @Test
  public void testIntegerBoundaries() {
    GravityData data = new GravityData();

    // Test integer field boundaries
    data.id = Integer.MIN_VALUE;
    data.userId = Integer.MAX_VALUE;
    data.imageId = 0;
    data.attempt = -1;

    assertEquals("Min int ID", Integer.MIN_VALUE, data.id);
    assertEquals("Max int userId", Integer.MAX_VALUE, data.userId);
    assertEquals("Zero imageId", 0, data.imageId);
    assertEquals("Negative attempt", -1, data.attempt);

    // Test long boundary
    data.timestamp = Long.MIN_VALUE;
    assertEquals("Min long timestamp", Long.MIN_VALUE, data.timestamp);

    data.timestamp = Long.MAX_VALUE;
    assertEquals("Max long timestamp", Long.MAX_VALUE, data.timestamp);
  }

  @Test
  public void testUnicodeSessionId() {
    GravityData data = new GravityData(1, 2, "重力_gravity_αβγ_🌍", 3, 4L, 1.0f, 2.0f, 9.8f);

    assertEquals("Unicode sessionId should be preserved", "重力_gravity_αβγ_🌍", data.sessionId);
  }

  @Test
  public void testZeroGravityScenario() {
    // Test with zero gravity (theoretical free-fall scenario)
    GravityData zeroG = new GravityData(
        1,
                                        1,
                                        "zero_gravity",
                                        1,
                                        System.currentTimeMillis(),
                                        0.0f,
                                        0.0f,
                                        0.0f
    );

    assertEquals("Zero gravity X", 0.0f, zeroG.x, 0.001f);
    assertEquals("Zero gravity Y", 0.0f, zeroG.y, 0.001f);
    assertEquals("Zero gravity Z", 0.0f, zeroG.z, 0.001f);

    float magnitude = (float) Math.sqrt(zeroG.x * zeroG.x + zeroG.y * zeroG.y + zeroG.z * zeroG.z);
    assertEquals("Zero gravity magnitude", 0.0f, magnitude, 0.001f);
  }

  @Test
  public void testMicroGravityScenario() {
    // Test with micro-gravity (space station scenario)
    GravityData microG = new GravityData(
        1,
                                         1,
                                         "micro_gravity",
                                         1,
                                         System.currentTimeMillis(),
                                         0.001f,
                                         0.002f,
                                         0.003f
                                         // Very small values
    );

    assertEquals("Micro gravity X", 0.001f, microG.x, 0.0001f);
    assertEquals("Micro gravity Y", 0.002f, microG.y, 0.0001f);
    assertEquals("Micro gravity Z", 0.003f, microG.z, 0.0001f);

    float magnitude = (float) Math.sqrt(microG.x * microG.x
                                        + microG.y * microG.y
                                        + microG.z * microG.z);
    assertTrue("Micro gravity magnitude should be very small", magnitude < 0.01f);
  }

  @Test
  public void testExtremeGravityScenario() {
    // Test with extreme gravity values (acceleration/deceleration scenario)
    GravityData extremeG = new GravityData(
        1,
                                           1,
                                           "extreme_gravity",
                                           1,
                                           System.currentTimeMillis(),
                                           5.0f,
                                           8.0f,
                                           20.0f
                                           // Much higher than Earth's gravity
    );

    assertEquals("Extreme gravity X", 5.0f, extremeG.x, 0.001f);
    assertEquals("Extreme gravity Y", 8.0f, extremeG.y, 0.001f);
    assertEquals("Extreme gravity Z", 20.0f, extremeG.z, 0.001f);

    float magnitude = (float) Math.sqrt(extremeG.x * extremeG.x
                                        + extremeG.y * extremeG.y
                                        + extremeG.z * extremeG.z);
    assertTrue("Extreme gravity magnitude should be much higher than Earth's", magnitude > 15.0f);
  }

  @Test
  public void testStandardGravityConstant() {
    // Test with standard Earth gravity constant (9.80665 m/s²)
    GravityData standardG = new GravityData(
        1,
                                            1,
                                            "standard_gravity",
                                            1,
                                            System.currentTimeMillis(),
                                            0.0f,
                                            0.0f,
                                            9.80665f
    );

    assertEquals("Standard gravity value", 9.80665f, standardG.z, 0.0001f);

    float magnitude = (float) Math.sqrt(standardG.x * standardG.x
                                        + standardG.y * standardG.y
                                        + standardG.z * standardG.z);
    assertEquals("Standard gravity magnitude", 9.80665f, magnitude, 0.0001f);
  }
} 

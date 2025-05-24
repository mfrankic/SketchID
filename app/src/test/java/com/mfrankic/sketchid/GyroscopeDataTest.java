package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for GyroscopeData Room entity class.
 * Tests constructors, field assignments, and data integrity for gyroscope sensor data.
 */
public class GyroscopeDataTest {

  @Test
  public void testDefaultConstructor() {
    GyroscopeData data = new GyroscopeData();
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
    int userId = 321;
    int imageId = 654;
    String sessionId = "gyroscope_session_987";
    int attempt = 3;
    long timestamp = System.currentTimeMillis();
    float x = 0.5f;   // X-axis angular velocity (rad/s)
    float y = -1.2f;  // Y-axis angular velocity (rad/s)
    float z = 2.1f;   // Z-axis angular velocity (rad/s)

    GyroscopeData data = new GyroscopeData(userId, imageId, sessionId, attempt, timestamp, x, y, z);

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
  public void testParameterizedConstructorWithRealisticGyroscopeValues() {
    // Test with typical gyroscope sensor values (angular velocity in rad/s)
    GyroscopeData slowRotation = new GyroscopeData(
        1, 1, "slow_rotation", 1, System.currentTimeMillis(), 0.1f,
        // Slow rotation around X-axis
        0.05f,
        // Very slow rotation around Y-axis
        -0.2f
        // Slow counter-rotation around Z-axis
    );

    assertEquals("Slow rotation X", 0.1f, slowRotation.x, 0.001f);
    assertEquals("Slow rotation Y", 0.05f, slowRotation.y, 0.001f);
    assertEquals("Slow rotation Z", -0.2f, slowRotation.z, 0.001f);

    // Test with device being rapidly rotated
    GyroscopeData fastRotation = new GyroscopeData(
        2, 2, "fast_rotation", 1, System.currentTimeMillis(), 5.2f,
        // Fast rotation around X-axis (pitch)
        -3.8f,
        // Fast counter-rotation around Y-axis (roll)
        7.1f
        // Very fast rotation around Z-axis (yaw)
    );

    assertEquals("Fast rotation X", 5.2f, fastRotation.x, 0.001f);
    assertEquals("Fast rotation Y", -3.8f, fastRotation.y, 0.001f);
    assertEquals("Fast rotation Z", 7.1f, fastRotation.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithBoundaryValues() {
    // Test with minimum values
    GyroscopeData data1 = new GyroscopeData(0, 0, "", 0, 0L, 0.0f, 0.0f, 0.0f);
    assertEquals("Min userId should work", 0, data1.userId);
    assertEquals("Min imageId should work", 0, data1.imageId);
    assertEquals("Empty sessionId should work", "", data1.sessionId);
    assertEquals("Min attempt should work", 0, data1.attempt);
    assertEquals("Min timestamp should work", 0L, data1.timestamp);
    assertEquals("Zero X should work", 0.0f, data1.x, 0.001f);
    assertEquals("Zero Y should work", 0.0f, data1.y, 0.001f);
    assertEquals("Zero Z should work", 0.0f, data1.z, 0.001f);

    // Test with maximum/large values
    GyroscopeData data2 = new GyroscopeData(
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        "extremely_long_gyroscope_session_id_with_many_characters_12345",
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
        "extremely_long_gyroscope_session_id_with_many_characters_12345",
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
    // Gyroscope can have negative values for counter-clockwise rotation
    GyroscopeData data = new GyroscopeData(
        -1,
                                           -2,
                                           "counter_rotation",
                                           -3,
                                           -4L,
                                           -2.5f,
                                           -1.8f,
                                           -4.3f
    );

    assertEquals("Negative userId should be allowed", -1, data.userId);
    assertEquals("Negative imageId should be allowed", -2, data.imageId);
    assertEquals("Negative attempt should be allowed", -3, data.attempt);
    assertEquals("Negative timestamp should be allowed", -4L, data.timestamp);
    assertEquals("Negative X should be allowed", -2.5f, data.x, 0.001f);
    assertEquals("Negative Y should be allowed", -1.8f, data.y, 0.001f);
    assertEquals("Negative Z should be allowed", -4.3f, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithNullSessionId() {
    GyroscopeData data = new GyroscopeData(1, 2, null, 3, 4L, 5.0f, 6.0f, 7.0f);

    assertNull("Null sessionId should be allowed", data.sessionId);
    assertEquals("Other fields should be preserved with null sessionId", 1, data.userId);
  }

  @Test
  public void testParameterizedConstructorWithSpecialFloatValues() {
    // Test with NaN values
    GyroscopeData data1 = new GyroscopeData(
        1,
                                            2,
                                            "gyroscope_nan",
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
    GyroscopeData data2 = new GyroscopeData(
        1,
                                            2,
                                            "gyroscope_infinity",
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
    GyroscopeData data = new GyroscopeData();

    // Test setting ID (even though it's typically auto-generated)
    data.id = 555;
    assertEquals("ID should be assignable", 555, data.id);

    // Test setting userId
    data.userId = 666;
    assertEquals("UserId should be assignable", 666, data.userId);

    // Test setting imageId
    data.imageId = 777;
    assertEquals("ImageId should be assignable", 777, data.imageId);

    // Test setting sessionId
    data.sessionId = "new_gyroscope_session";
    assertEquals("SessionId should be assignable", "new_gyroscope_session", data.sessionId);

    // Test setting attempt
    data.attempt = 9;
    assertEquals("Attempt should be assignable", 9, data.attempt);

    // Test setting timestamp
    long newTimestamp = System.currentTimeMillis();
    data.timestamp = newTimestamp;
    assertEquals("Timestamp should be assignable", newTimestamp, data.timestamp);

    // Test setting angular velocity components
    data.x = 3.5f;
    data.y = -2.1f;
    data.z = 1.8f;
    assertEquals("X should be assignable", 3.5f, data.x, 0.001f);
    assertEquals("Y should be assignable", -2.1f, data.y, 0.001f);
    assertEquals("Z should be assignable", 1.8f, data.z, 0.001f);
  }

  @Test
  public void testFieldIndependence() {
    GyroscopeData data = new GyroscopeData();

    // Setting one field should not affect others
    data.userId = 1;
    assertEquals("Setting userId should not affect other fields", 0, data.imageId);
    assertEquals("Setting userId should not affect other fields", 0, data.attempt);
    assertNull("Setting userId should not affect other fields", data.sessionId);

    data.x = 4.5f;
    assertEquals("Setting x should not affect y", 0.0f, data.y, 0.001f);
    assertEquals("Setting x should not affect z", 0.0f, data.z, 0.001f);
    assertEquals("Setting x should not affect userId", 1, data.userId);
  }

  @Test
  public void testGyroscopeRotationScenarios() {
    // Test different device rotation scenarios

    // Pitch rotation (X-axis) - device tilting forward/backward
    GyroscopeData pitchRotation = new GyroscopeData(
        1,
                                                    1,
                                                    "pitch_rotation",
                                                    1,
                                                    1000L,
                                                    2.5f,
                                                    0.1f,
                                                    0.0f
    );
    assertEquals("Pitch rotation X (dominant)", 2.5f, pitchRotation.x, 0.001f);
    assertEquals("Pitch rotation Y (minimal)", 0.1f, pitchRotation.y, 0.001f);
    assertEquals("Pitch rotation Z (zero)", 0.0f, pitchRotation.z, 0.001f);

    // Roll rotation (Y-axis) - device tilting left/right
    GyroscopeData rollRotation = new GyroscopeData(
        1,
                                                   2,
                                                   "roll_rotation",
                                                   1,
                                                   2000L,
                                                   0.0f,
                                                   3.2f,
                                                   0.1f
    );
    assertEquals("Roll rotation X (zero)", 0.0f, rollRotation.x, 0.001f);
    assertEquals("Roll rotation Y (dominant)", 3.2f, rollRotation.y, 0.001f);
    assertEquals("Roll rotation Z (minimal)", 0.1f, rollRotation.z, 0.001f);

    // Yaw rotation (Z-axis) - device rotating like a compass
    GyroscopeData yawRotation = new GyroscopeData(
        1,
                                                  3,
                                                  "yaw_rotation",
                                                  1,
                                                  3000L,
                                                  0.05f,
                                                  0.02f,
                                                  4.1f
    );
    assertEquals("Yaw rotation X (minimal)", 0.05f, yawRotation.x, 0.001f);
    assertEquals("Yaw rotation Y (minimal)", 0.02f, yawRotation.y, 0.001f);
    assertEquals("Yaw rotation Z (dominant)", 4.1f, yawRotation.z, 0.001f);
  }

  @Test
  public void testAngularVelocityMagnitudeCalculations() {
    // Test various angular velocity readings and their magnitudes
    GyroscopeData[] readings = {
        new GyroscopeData(1, 1, "test1", 1, 1000L, 1.0f, 0.0f, 0.0f),      // Single axis rotation
        new GyroscopeData(1, 2, "test2", 1, 2000L, 0.0f, 2.0f, 0.0f),      // Single axis rotation
        new GyroscopeData(1, 3, "test3", 1, 3000L, 3.0f, 4.0f, 0.0f),      // Two axis rotation
        new GyroscopeData(1, 4, "test4", 1, 4000L, 1.0f, 2.0f, 2.0f)       // Three axis rotation
    };

    float[] expectedMagnitudes = {1.0f, 2.0f, 5.0f, 3.0f};

    for (int i = 0; i < readings.length; i++) {
      GyroscopeData data = readings[i];
      float magnitude = (float) Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
      assertEquals(
          "Reading " + i + " magnitude should be close to expected",
          expectedMagnitudes[i],
          magnitude,
          0.1f
      );
    }
  }

  @Test
  public void testGyroscopeSamplingSequence() {
    // Test creating multiple gyroscope readings during rotation sequence
    GyroscopeData[] samples = new GyroscopeData[8];
    long baseTimestamp = System.currentTimeMillis();

    // Simulate device starting rotation and then stopping
    for (int i = 0; i < 8; i++) {
      float rotationSpeed;
      if (i < 3) {
        rotationSpeed = i * 1.0f;  // Accelerating rotation
      } else if (i < 5) {
        rotationSpeed = 3.0f;      // Constant rotation
      } else {
        rotationSpeed = 3.0f - ((i - 4) * 0.8f);  // Decelerating rotation
      }

      samples[i] = new GyroscopeData(
          1,                                          // same user
          1,                                          // same image
          "rotation_sequence",                        // same session
          1,                                          // same attempt
          baseTimestamp + (i * 20),                   // 20ms intervals (50Hz sampling)
          rotationSpeed,                              // Variable X angular velocity
          0.1f,                                       // Minimal Y angular velocity
          rotationSpeed * 0.5f                        // Proportional Z angular velocity
      );
    }

    // Verify each sample
    for (int i = 0; i < 8; i++) {
      assertEquals("Sample " + i + " userId", 1, samples[i].userId);
      assertEquals("Sample " + i + " imageId", 1, samples[i].imageId);
      assertEquals("Sample " + i + " sessionId", "rotation_sequence", samples[i].sessionId);
      assertEquals("Sample " + i + " attempt", 1, samples[i].attempt);
      assertEquals("Sample " + i + " timestamp", baseTimestamp + (i * 20), samples[i].timestamp);
      assertEquals("Sample " + i + " Y angular velocity", 0.1f, samples[i].y, 0.001f);
    }

    // Verify rotation pattern: accelerate -> constant -> decelerate
    assertTrue("Initial rotation should be slower than peak", samples[0].x < samples[3].x);
    assertEquals("Peak rotation should be constant", samples[3].x, samples[4].x, 0.001f);
    assertTrue("Final rotation should be slower than peak", samples[7].x < samples[4].x);
  }

  @Test
  public void testSessionIdVariants() {
    // Test different gyroscope session ID formats
    String[] sessionIds = {
        "gyroscope_simple",
        "gyro_session_123",
        "GYROSCOPE_SESSION_ABC_456",
        "gyro-uuid-550e8400-e29b-41d4-a716-446655440000",
        "Mixed_Gyroscope_Session_123_ABC",
        "gyro-with-dashes-and_underscores",
        "gyroscope123456789",
        "",
        null
    };

    for (int i = 0; i < sessionIds.length; i++) {
      GyroscopeData data = new GyroscopeData(
          i,
                                             i,
                                             sessionIds[i],
                                             i,
                                             i * 1000L,
                                             i * 0.5f,
                                             i * -0.2f,
                                             i * 0.8f
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
  public void testHighFrequencyTimestampProgression() {
    // Test realistic gyroscope sensor sampling progression (typically high frequency)
    long baseTime = System.currentTimeMillis();
    int samplingRate = 100; // Hz (10ms intervals, typical for gyroscope)
    int intervalMs = 1000 / samplingRate;

    GyroscopeData[] samples = new GyroscopeData[12];
    for (int i = 0; i < 12; i++) {
      samples[i] = new GyroscopeData(
          1, 1, "high_freq_gyro_sampling", 1, baseTime + (i * intervalMs),  // 10ms intervals
          1.5f, -0.8f, 2.3f             // Consistent angular velocity reading
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
    GyroscopeData data = new GyroscopeData(1, 2, "initial_gyro", 3, 4L, 1.5f, 2.5f, 3.5f);

    // Verify initial values
    assertEquals("Initial userId", 1, data.userId);
    assertEquals("Initial sessionId", "initial_gyro", data.sessionId);
    assertEquals("Initial X", 1.5f, data.x, 0.001f);

    // Modify values
    data.userId = 30;
    data.sessionId = "modified_gyro";
    data.x = 9.5f;

    // Verify changes
    assertEquals("Modified userId", 30, data.userId);
    assertEquals("Modified sessionId", "modified_gyro", data.sessionId);
    assertEquals("Modified X", 9.5f, data.x, 0.001f);
  }

  @Test
  public void testFloatPrecision() {
    GyroscopeData data = new GyroscopeData();

    // Test very small angular velocity values
    data.x = 0.000001f;
    data.y = -0.000001f;
    data.z = 0.0000005f;

    assertEquals("Very small X", 0.000001f, data.x, 0.0000001f);
    assertEquals("Very small Y", -0.000001f, data.y, 0.0000001f);
    assertEquals("Very small Z", 0.0000005f, data.z, 0.0000001f);

    // Test precise decimal values common in gyroscope readings
    data.x = 3.14159265f;  // Pi radians/second
    data.y = -1.57079633f; // -Pi/2 radians/second
    data.z = 6.28318531f;  // 2*Pi radians/second

    assertEquals("Precise X (Pi rad/s)", 3.14159265f, data.x, 0.0001f);
    assertEquals("Precise Y (-Pi/2 rad/s)", -1.57079633f, data.y, 0.0001f);
    assertEquals("Precise Z (2*Pi rad/s)", 6.28318531f, data.z, 0.0001f);
  }

  @Test
  public void testIntegerBoundaries() {
    GyroscopeData data = new GyroscopeData();

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
    GyroscopeData data = new GyroscopeData(1, 2, "陀螺仪_gyroscope_αβγ_🌀", 3, 4L, 1.0f, 2.0f, 3.0f);

    assertEquals("Unicode sessionId should be preserved", "陀螺仪_gyroscope_αβγ_🌀", data.sessionId);
  }

  @Test
  public void testStationaryDevice() {
    // Test with stationary device (no rotation - all values near zero)
    GyroscopeData stationary = new GyroscopeData(
        1,
                                                 1,
                                                 "stationary",
                                                 1,
                                                 System.currentTimeMillis(),
                                                 0.01f,
                                                 -0.005f,
                                                 0.002f
                                                 // Very small noise values when device is still
    );

    assertEquals("Stationary X", 0.01f, stationary.x, 0.001f);
    assertEquals("Stationary Y", -0.005f, stationary.y, 0.001f);
    assertEquals("Stationary Z", 0.002f, stationary.z, 0.001f);

    float magnitude = (float) Math.sqrt(stationary.x * stationary.x
                                        + stationary.y * stationary.y
                                        + stationary.z * stationary.z);
    assertTrue("Stationary magnitude should be very small", magnitude < 0.02f);
  }

  @Test
  public void testRapidRotationScenario() {
    // Test with rapid rotation scenario (spinning device)
    GyroscopeData rapidRotation = new GyroscopeData(
        1,
                                                    1,
                                                    "rapid_rotation",
                                                    1,
                                                    System.currentTimeMillis(),
                                                    10.0f,
                                                    -15.0f,
                                                    20.0f
                                                    // High angular velocities
    );

    assertEquals("Rapid rotation X", 10.0f, rapidRotation.x, 0.001f);
    assertEquals("Rapid rotation Y", -15.0f, rapidRotation.y, 0.001f);
    assertEquals("Rapid rotation Z", 20.0f, rapidRotation.z, 0.001f);

    float magnitude = (float) Math.sqrt(rapidRotation.x * rapidRotation.x
                                        + rapidRotation.y * rapidRotation.y
                                        + rapidRotation.z * rapidRotation.z);
    assertTrue("Rapid rotation magnitude should be significant", magnitude > 20.0f);
  }

  @Test
  public void testTypicalSmartphoneGyroscope() {
    // Test with typical smartphone gyroscope range and precision
    GyroscopeData typical = new GyroscopeData(
        1, 1, "smartphone_typical", 1, System.currentTimeMillis(), 2.5f,
        // Typical angular velocity (rad/s)
        -1.8f,
        // Typical angular velocity (rad/s)
        3.2f
        // Typical angular velocity (rad/s)
    );

    assertEquals("Typical smartphone X", 2.5f, typical.x, 0.001f);
    assertEquals("Typical smartphone Y", -1.8f, typical.y, 0.001f);
    assertEquals("Typical smartphone Z", 3.2f, typical.z, 0.001f);

    // Check that values are within typical smartphone gyroscope range (±35 rad/s)
    assertTrue("X should be within typical range", Math.abs(typical.x) <= 35.0f);
    assertTrue("Y should be within typical range", Math.abs(typical.y) <= 35.0f);
    assertTrue("Z should be within typical range", Math.abs(typical.z) <= 35.0f);
  }

  @Test
  public void testAngularVelocityConversions() {
    // Test understanding of angular velocity units and conversions
    GyroscopeData data = new GyroscopeData(
        1, 1, "angular_velocity", 1, System.currentTimeMillis(), 3.14159f,
        // Pi rad/s = 180 degrees/s = 0.5 rotations/s
        6.28318f,
        // 2*Pi rad/s = 360 degrees/s = 1 rotation/s
        1.57080f
        // Pi/2 rad/s = 90 degrees/s = 0.25 rotations/s
    );

    // Verify the values are stored correctly
    assertEquals("Pi rad/s X component", 3.14159f, data.x, 0.001f);
    assertEquals("2*Pi rad/s Y component", 6.28318f, data.y, 0.001f);
    assertEquals("Pi/2 rad/s Z component", 1.57080f, data.z, 0.001f);

    // Verify magnitude calculation: sqrt(π² + (2π)² + (π/2)²) = sqrt(9.87 + 39.48 + 2.47) = sqrt
    // (51.82) ≈ 7.20
    float magnitude = (float) Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
    assertEquals("Angular velocity magnitude", 7.20f, magnitude, 0.1f);
  }

  @Test
  public void testMultiAxisRotation() {
    // Test device rotating around multiple axes simultaneously
    GyroscopeData multiAxis = new GyroscopeData(
        1, 1, "multi_axis_rotation", 1, System.currentTimeMillis(), 2.0f,    // Pitch rotation
        1.5f,    // Roll rotation
        3.0f     // Yaw rotation
    );

    assertEquals("Multi-axis pitch", 2.0f, multiAxis.x, 0.001f);
    assertEquals("Multi-axis roll", 1.5f, multiAxis.y, 0.001f);
    assertEquals("Multi-axis yaw", 3.0f, multiAxis.z, 0.001f);

    // Calculate total angular velocity: sqrt(2² + 1.5² + 3²) = sqrt(4 + 2.25 + 9) = sqrt(15.25)
    // ≈ 3.90
    float totalAngularVelocity = (float) Math.sqrt(multiAxis.x * multiAxis.x
                                                   + multiAxis.y * multiAxis.y
                                                   + multiAxis.z * multiAxis.z);
    assertEquals(
        "Total angular velocity should be calculated correctly",
        3.90f,
        totalAngularVelocity,
        0.1f
    );
  }

  @Test
  public void testGyroscopeDriftCompensation() {
    // Test representing gyroscope bias/drift (small constant offsets)
    GyroscopeData withDrift = new GyroscopeData(
        1, 1, "gyro_drift", 1, System.currentTimeMillis(), 0.02f,   // Small drift in X
        -0.01f,  // Small drift in Y
        0.005f   // Small drift in Z
    );

    assertEquals("Drift X component", 0.02f, withDrift.x, 0.001f);
    assertEquals("Drift Y component", -0.01f, withDrift.y, 0.001f);
    assertEquals("Drift Z component", 0.005f, withDrift.z, 0.001f);

    // Verify drift magnitude is small
    float driftMagnitude = (float) Math.sqrt(withDrift.x * withDrift.x
                                             + withDrift.y * withDrift.y
                                             + withDrift.z * withDrift.z);
    assertTrue("Drift magnitude should be very small", driftMagnitude < 0.05f);
  }

  @Test
  public void testRotationSequenceTracking() {
    // Test tracking a complete rotation sequence (start -> accelerate -> peak -> decelerate ->
    // stop)
    GyroscopeData[] rotationSequence = {
        new GyroscopeData(1, 1, "rotation_seq", 1, 1000L, 0.0f, 0.0f, 0.0f),
        // Start (stationary)
        new GyroscopeData(1, 1, "rotation_seq", 1, 1100L, 0.5f, 0.0f, 1.0f),
        // Begin rotation
        new GyroscopeData(1, 1, "rotation_seq", 1, 1200L, 1.5f, 0.0f, 3.0f),
        // Accelerating
        new GyroscopeData(1, 1, "rotation_seq", 1, 1300L, 2.5f, 0.0f, 5.0f),
        // Peak rotation
        new GyroscopeData(1, 1, "rotation_seq", 1, 1400L, 1.8f, 0.0f, 3.5f),
        // Decelerating
        new GyroscopeData(1, 1, "rotation_seq", 1, 1500L, 0.8f, 0.0f, 1.5f),
        // Slowing down
        new GyroscopeData(1, 1, "rotation_seq", 1, 1600L, 0.1f, 0.0f, 0.2f)
        // Nearly stopped
    };

    // Verify the sequence shows expected pattern
    assertEquals("Start should be stationary", 0.0f, rotationSequence[0].z, 0.001f);
    assertTrue("Should accelerate initially", rotationSequence[1].z > rotationSequence[0].z);
    assertTrue("Should reach peak", rotationSequence[3].z > rotationSequence[2].z);
    assertTrue("Should decelerate", rotationSequence[4].z < rotationSequence[3].z);
    assertTrue("Should approach stationary", rotationSequence[6].z < rotationSequence[1].z);

    // Verify peak is the maximum value
    float maxRotation = 0.0f;
    for (GyroscopeData sample : rotationSequence) {
      if (sample.z > maxRotation) {
        maxRotation = sample.z;
      }
    }
    assertEquals("Peak rotation should be maximum", rotationSequence[3].z, maxRotation, 0.001f);
  }
} 

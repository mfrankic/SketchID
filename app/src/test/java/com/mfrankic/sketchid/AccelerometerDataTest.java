package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for AccelerometerData Room entity class.
 * Tests constructors, field assignments, and data integrity for accelerometer sensor data.
 */
public class AccelerometerDataTest {

  @Test
  public void testDefaultConstructor() {
    AccelerometerData data = new AccelerometerData();
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
    int userId = 456;
    int imageId = 789;
    String sessionId = "accelerometer_session_123";
    int attempt = 2;
    long timestamp = System.currentTimeMillis();
    float x = 1.2f;   // X-axis acceleration component
    float y = -0.8f;  // Y-axis acceleration component
    float z = 9.9f;   // Z-axis acceleration component (includes gravity + device acceleration)

    AccelerometerData data = new AccelerometerData(
        userId,
                                                   imageId,
                                                   sessionId,
                                                   attempt,
                                                   timestamp,
                                                   x,
                                                   y,
                                                   z
    );

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
  public void testParameterizedConstructorWithRealisticAccelerometerValues() {
    // Test with typical accelerometer sensor values (includes gravity + linear acceleration)
    AccelerometerData staticDevice = new AccelerometerData(
        1, 1, "static_accelerometer", 1, System.currentTimeMillis(), 0.3f,
        // Small X component (slight device tilt)
        1.2f,
        // Y component (device tilted in Y direction)
        9.9f
        // Z component (gravity + small linear acceleration)
    );

    assertEquals("Static accelerometer X", 0.3f, staticDevice.x, 0.001f);
    assertEquals("Static accelerometer Y", 1.2f, staticDevice.y, 0.001f);
    assertEquals("Static accelerometer Z", 9.9f, staticDevice.z, 0.001f);

    // Test with device being moved (higher acceleration values)
    AccelerometerData movingDevice = new AccelerometerData(
        2, 2, "moving_accelerometer", 1, System.currentTimeMillis(), 3.5f,
        // Significant X acceleration (device being moved)
        -2.1f,
        // Negative Y acceleration (opposite direction)
        12.4f
        // Higher Z component (gravity + upward acceleration)
    );

    assertEquals("Moving accelerometer X", 3.5f, movingDevice.x, 0.001f);
    assertEquals("Moving accelerometer Y", -2.1f, movingDevice.y, 0.001f);
    assertEquals("Moving accelerometer Z", 12.4f, movingDevice.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithBoundaryValues() {
    // Test with minimum values
    AccelerometerData data1 = new AccelerometerData(0, 0, "", 0, 0L, 0.0f, 0.0f, 0.0f);
    assertEquals("Min userId should work", 0, data1.userId);
    assertEquals("Min imageId should work", 0, data1.imageId);
    assertEquals("Empty sessionId should work", "", data1.sessionId);
    assertEquals("Min attempt should work", 0, data1.attempt);
    assertEquals("Min timestamp should work", 0L, data1.timestamp);
    assertEquals("Zero X should work", 0.0f, data1.x, 0.001f);
    assertEquals("Zero Y should work", 0.0f, data1.y, 0.001f);
    assertEquals("Zero Z should work", 0.0f, data1.z, 0.001f);

    // Test with maximum/large values
    AccelerometerData data2 = new AccelerometerData(
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        "extremely_long_accelerometer_session_id_with_many_characters_12345",
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
        "extremely_long_accelerometer_session_id_with_many_characters_12345",
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
    // Accelerometer can have negative components depending on acceleration direction
    AccelerometerData data = new AccelerometerData(
        -1,
                                                   -2,
                                                   "negative_acceleration",
                                                   -3,
                                                   -4L,
                                                   -5.5f,
                                                   -3.2f,
                                                   -1.1f
    );

    assertEquals("Negative userId should be allowed", -1, data.userId);
    assertEquals("Negative imageId should be allowed", -2, data.imageId);
    assertEquals("Negative attempt should be allowed", -3, data.attempt);
    assertEquals("Negative timestamp should be allowed", -4L, data.timestamp);
    assertEquals("Negative X should be allowed", -5.5f, data.x, 0.001f);
    assertEquals("Negative Y should be allowed", -3.2f, data.y, 0.001f);
    assertEquals("Negative Z should be allowed", -1.1f, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithNullSessionId() {
    AccelerometerData data = new AccelerometerData(1, 2, null, 3, 4L, 5.0f, 6.0f, 7.0f);

    assertNull("Null sessionId should be allowed", data.sessionId);
    assertEquals("Other fields should be preserved with null sessionId", 1, data.userId);
  }

  @Test
  public void testParameterizedConstructorWithSpecialFloatValues() {
    // Test with NaN values
    AccelerometerData data1 = new AccelerometerData(
        1,
                                                    2,
                                                    "accelerometer_nan",
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
    AccelerometerData data2 = new AccelerometerData(
        1,
        2,
        "accelerometer_infinity",
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
    AccelerometerData data = new AccelerometerData();

    // Test setting ID (even though it's typically auto-generated)
    data.id = 123;
    assertEquals("ID should be assignable", 123, data.id);

    // Test setting userId
    data.userId = 456;
    assertEquals("UserId should be assignable", 456, data.userId);

    // Test setting imageId
    data.imageId = 789;
    assertEquals("ImageId should be assignable", 789, data.imageId);

    // Test setting sessionId
    data.sessionId = "new_accelerometer_session";
    assertEquals("SessionId should be assignable", "new_accelerometer_session", data.sessionId);

    // Test setting attempt
    data.attempt = 7;
    assertEquals("Attempt should be assignable", 7, data.attempt);

    // Test setting timestamp
    long newTimestamp = System.currentTimeMillis();
    data.timestamp = newTimestamp;
    assertEquals("Timestamp should be assignable", newTimestamp, data.timestamp);

    // Test setting acceleration components
    data.x = 2.5f;
    data.y = -1.8f;
    data.z = 11.2f;
    assertEquals("X should be assignable", 2.5f, data.x, 0.001f);
    assertEquals("Y should be assignable", -1.8f, data.y, 0.001f);
    assertEquals("Z should be assignable", 11.2f, data.z, 0.001f);
  }

  @Test
  public void testFieldIndependence() {
    AccelerometerData data = new AccelerometerData();

    // Setting one field should not affect others
    data.userId = 1;
    assertEquals("Setting userId should not affect other fields", 0, data.imageId);
    assertEquals("Setting userId should not affect other fields", 0, data.attempt);
    assertNull("Setting userId should not affect other fields", data.sessionId);

    data.x = 3.5f;
    assertEquals("Setting x should not affect y", 0.0f, data.y, 0.001f);
    assertEquals("Setting x should not affect z", 0.0f, data.z, 0.001f);
    assertEquals("Setting x should not affect userId", 1, data.userId);
  }

  @Test
  public void testAccelerometerDeviceOrientationScenarios() {
    // Test different device orientations and their accelerometer effects

    // Portrait mode (device upright, at rest)
    AccelerometerData portrait = new AccelerometerData(
        1,
                                                       1,
                                                       "portrait_rest",
                                                       1,
                                                       1000L,
                                                       0.2f,
                                                       0.5f,
                                                       9.8f
    );
    assertEquals("Portrait X (minimal)", 0.2f, portrait.x, 0.001f);
    assertEquals("Portrait Y (minimal)", 0.5f, portrait.y, 0.001f);
    assertEquals("Portrait Z (gravity dominant)", 9.8f, portrait.z, 0.001f);

    // Landscape mode (device rotated 90°, at rest)
    AccelerometerData landscape = new AccelerometerData(
        1,
                                                        2,
                                                        "landscape_rest",
                                                        1,
                                                        2000L,
                                                        9.7f,
                                                        0.4f,
                                                        0.6f
    );
    assertEquals("Landscape X (gravity dominant)", 9.7f, landscape.x, 0.001f);
    assertEquals("Landscape Y (minimal)", 0.4f, landscape.y, 0.001f);
    assertEquals("Landscape Z (minimal)", 0.6f, landscape.z, 0.001f);

    // Device being shaken (high acceleration in all directions)
    AccelerometerData shaking = new AccelerometerData(
        1,
                                                      3,
                                                      "shaking",
                                                      1,
                                                      3000L,
                                                      15.2f,
                                                      -12.8f,
                                                      18.5f
    );
    assertEquals("Shaking X (high acceleration)", 15.2f, shaking.x, 0.001f);
    assertEquals("Shaking Y (high negative acceleration)", -12.8f, shaking.y, 0.001f);
    assertEquals("Shaking Z (high acceleration)", 18.5f, shaking.z, 0.001f);
  }

  @Test
  public void testAccelerometerMagnitudeCalculations() {
    // Test various accelerometer readings and their magnitudes
    AccelerometerData[] readings = {
        new AccelerometerData(1, 1, "test1", 1, 1000L, 0.0f, 0.0f, 9.8f),    // At rest, upright
        new AccelerometerData(1, 2, "test2", 1, 2000L, 9.8f, 0.0f, 0.0f),    // At rest, sideways
        new AccelerometerData(1, 3, "test3", 1, 3000L, 5.0f, 5.0f, 12.0f),   // Moving device
        new AccelerometerData(1, 4, "test4", 1, 4000L, -3.0f, 8.0f, 15.0f)   // Different motion
    };

    float[] expectedMagnitudes = {9.8f, 9.8f, 14.18f, 17.29f};

    for (int i = 0; i < readings.length; i++) {
      AccelerometerData data = readings[i];
      float magnitude = (float) Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
      assertEquals(
          "Reading " + i + " magnitude should be close to expected",
          expectedMagnitudes[i],
          magnitude,
          0.5f
      );
    }
  }

  @Test
  public void testAccelerometerSamplingSequence() {
    // Test creating multiple accelerometer readings as would happen during high-frequency sampling
    AccelerometerData[] samples = new AccelerometerData[10];
    long baseTimestamp = System.currentTimeMillis();

    // Simulate device gradually accelerating (linear acceleration increases)
    for (int i = 0; i < 10; i++) {
      float acceleration = i * 0.5f; // Gradually increasing acceleration

      samples[i] = new AccelerometerData(
          1,                                          // same user
          1,                                          // same image
          "acceleration_sequence",                    // same session
          1,                                          // same attempt
          baseTimestamp + (i * 10),                   // 10ms intervals (100Hz sampling)
          acceleration,                               // Increasing X acceleration
          0.2f,                                       // Minimal Y acceleration
          9.8f + (acceleration * 0.5f)              // Z includes gravity + some linear acceleration
      );
    }

    // Verify each sample
    for (int i = 0; i < 10; i++) {
      assertEquals("Sample " + i + " userId", 1, samples[i].userId);
      assertEquals("Sample " + i + " imageId", 1, samples[i].imageId);
      assertEquals("Sample " + i + " sessionId", "acceleration_sequence", samples[i].sessionId);
      assertEquals("Sample " + i + " attempt", 1, samples[i].attempt);
      assertEquals("Sample " + i + " timestamp", baseTimestamp + (i * 10), samples[i].timestamp);
      assertEquals("Sample " + i + " X acceleration", i * 0.5f, samples[i].x, 0.001f);
      assertEquals("Sample " + i + " Y acceleration", 0.2f, samples[i].y, 0.001f);
      assertEquals("Sample " + i + " Z acceleration", 9.8f + (i * 0.25f), samples[i].z, 0.001f);
    }

    // Verify acceleration trend (should be increasing)
    for (int i = 1; i < samples.length; i++) {
      assertTrue("X acceleration should be increasing", samples[i].x >= samples[i - 1].x);
      assertTrue("Z acceleration should be increasing", samples[i].z >= samples[i - 1].z);
    }
  }

  @Test
  public void testSessionIdVariants() {
    // Test different accelerometer session ID formats
    String[] sessionIds = {
        "accelerometer_simple",
        "accel_session_123",
        "ACCELEROMETER_SESSION_ABC_456",
        "accel-uuid-550e8400-e29b-41d4-a716-446655440000",
        "Mixed_Accelerometer_Session_123_ABC",
        "accel-with-dashes-and_underscores",
        "accelerometer123456789",
        "",
        null
    };

    for (int i = 0; i < sessionIds.length; i++) {
      AccelerometerData data = new AccelerometerData(
          i,
                                                     i,
                                                     sessionIds[i],
                                                     i,
                                                     i * 1000L,
                                                     i * 0.8f,
                                                     i * -0.3f,
                                                     9.8f + (i * 0.2f)
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
    // Test realistic accelerometer sensor sampling progression (typically higher frequency)
    long baseTime = System.currentTimeMillis();
    int samplingRate = 200; // Hz (5ms intervals, high frequency for accelerometer)
    int intervalMs = 1000 / samplingRate;

    AccelerometerData[] samples = new AccelerometerData[15];
    for (int i = 0; i < 15; i++) {
      samples[i] = new AccelerometerData(
          1, 1, "high_freq_sampling", 1, baseTime + (i * intervalMs),  // 5ms intervals
          1.2f, -0.8f, 10.1f            // Consistent acceleration reading
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
    AccelerometerData data = new AccelerometerData(1, 2, "initial_accel", 3, 4L, 1.5f, 2.5f, 10.8f);

    // Verify initial values
    assertEquals("Initial userId", 1, data.userId);
    assertEquals("Initial sessionId", "initial_accel", data.sessionId);
    assertEquals("Initial X", 1.5f, data.x, 0.001f);

    // Modify values
    data.userId = 20;
    data.sessionId = "modified_accel";
    data.x = 8.5f;

    // Verify changes
    assertEquals("Modified userId", 20, data.userId);
    assertEquals("Modified sessionId", "modified_accel", data.sessionId);
    assertEquals("Modified X", 8.5f, data.x, 0.001f);
  }

  @Test
  public void testFloatPrecision() {
    AccelerometerData data = new AccelerometerData();

    // Test very small acceleration values
    data.x = 0.000001f;
    data.y = -0.000001f;
    data.z = 9.800001f;

    assertEquals("Very small X", 0.000001f, data.x, 0.0000001f);
    assertEquals("Very small Y", -0.000001f, data.y, 0.0000001f);
    assertEquals("Precise Z", 9.800001f, data.z, 0.0000001f);

    // Test precise decimal values common in accelerometer readings
    data.x = 2.98765432f;
    data.y = -1.23456789f;
    data.z = 9.80665f;    // Standard gravity acceleration

    assertEquals("Precise X", 2.98765432f, data.x, 0.0001f);
    assertEquals("Precise Y", -1.23456789f, data.y, 0.0001f);
    assertEquals("Standard gravity Z", 9.80665f, data.z, 0.0001f);
  }

  @Test
  public void testIntegerBoundaries() {
    AccelerometerData data = new AccelerometerData();

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
    AccelerometerData data = new AccelerometerData(
        1,
                                                   2,
                                                   "加速度_accelerometer_αβγ_🚀",
                                                   3,
                                                   4L,
                                                   1.0f,
                                                   2.0f,
                                                   9.8f
    );

    assertEquals(
        "Unicode sessionId should be preserved",
        "加速度_accelerometer_αβγ_🚀",
        data.sessionId
    );
  }

  @Test
  public void testFreefall() {
    // Test with free-fall scenario (all accelerations near zero)
    AccelerometerData freefall = new AccelerometerData(
        1,
                                                       1,
                                                       "freefall",
                                                       1,
                                                       System.currentTimeMillis(),
                                                       0.1f,
                                                       -0.05f,
                                                       0.02f
                                                       // Very small values during free fall
    );

    assertEquals("Freefall X", 0.1f, freefall.x, 0.001f);
    assertEquals("Freefall Y", -0.05f, freefall.y, 0.001f);
    assertEquals("Freefall Z", 0.02f, freefall.z, 0.001f);

    float magnitude = (float) Math.sqrt(freefall.x * freefall.x
                                        + freefall.y * freefall.y
                                        + freefall.z * freefall.z);
    assertTrue("Freefall magnitude should be very small", magnitude < 0.2f);
  }

  @Test
  public void testHighAccelerationScenario() {
    // Test with high acceleration scenario (rapid movement/impact)
    AccelerometerData highAccel = new AccelerometerData(
        1,
                                                        1,
                                                        "high_acceleration",
                                                        1,
                                                        System.currentTimeMillis(),
                                                        25.0f,
                                                        -30.0f,
                                                        45.0f
                                                        // Very high values during rapid motion
    );

    assertEquals("High acceleration X", 25.0f, highAccel.x, 0.001f);
    assertEquals("High acceleration Y", -30.0f, highAccel.y, 0.001f);
    assertEquals("High acceleration Z", 45.0f, highAccel.z, 0.001f);

    float magnitude = (float) Math.sqrt(highAccel.x * highAccel.x
                                        + highAccel.y * highAccel.y
                                        + highAccel.z * highAccel.z);
    assertTrue("High acceleration magnitude should be significant", magnitude > 50.0f);
  }

  @Test
  public void testTypicalSmartphoneAccelerometer() {
    // Test with typical smartphone accelerometer range and precision
    AccelerometerData typical = new AccelerometerData(
        1, 1, "smartphone_typical", 1, System.currentTimeMillis(), 2.5f,
        // Typical linear acceleration
        -1.8f,
        // Typical linear acceleration
        11.3f
        // Gravity + linear acceleration
    );

    assertEquals("Typical smartphone X", 2.5f, typical.x, 0.001f);
    assertEquals("Typical smartphone Y", -1.8f, typical.y, 0.001f);
    assertEquals("Typical smartphone Z", 11.3f, typical.z, 0.001f);

    // Check that values are within typical smartphone accelerometer range (±2g to ±16g)
    assertTrue("X should be within typical range", Math.abs(typical.x) <= 20.0f);
    assertTrue("Y should be within typical range", Math.abs(typical.y) <= 20.0f);
    assertTrue("Z should be within typical range", Math.abs(typical.z) <= 20.0f);
  }

  @Test
  public void testGravityPlusLinearAcceleration() {
    // Test understanding that accelerometer = gravity + linear acceleration
    AccelerometerData restUpright = new AccelerometerData(
        1,
                                                          1,
                                                          "rest_upright",
                                                          1,
                                                          1000L,
                                                          0.0f,
                                                          0.0f,
                                                          9.8f
    );

    AccelerometerData restSideways = new AccelerometerData(
        1,
                                                           2,
                                                           "rest_sideways",
                                                           1,
                                                           2000L,
                                                           9.8f,
                                                           0.0f,
                                                           0.0f
    );

    AccelerometerData movingUpward = new AccelerometerData(
        1,
                                                           3,
                                                           "moving_upward",
                                                           1,
                                                           3000L,
                                                           0.0f,
                                                           0.0f,
                                                           12.8f
                                                           // 9.8 (gravity) + 3.0 (upward
        // acceleration)
    );

    // At rest, only gravity is measured
    assertEquals("Rest upright should show only gravity in Z", 9.8f, restUpright.z, 0.1f);
    assertEquals("Rest sideways should show only gravity in X", 9.8f, restSideways.x, 0.1f);

    // Moving adds linear acceleration to gravity
    assertTrue("Moving upward should show gravity + linear acceleration", movingUpward.z > 10.0f);

    // Calculate linear acceleration component (total - gravity)
    float linearAcceleration = movingUpward.z - 9.8f;
    assertEquals("Linear acceleration component should be ~3.0", 3.0f, linearAcceleration, 0.2f);
  }

  @Test
  public void testMultiAxisAcceleration() {
    // Test device experiencing acceleration in multiple directions simultaneously
    AccelerometerData multiAxis = new AccelerometerData(
        1, 1, "multi_axis", 1, System.currentTimeMillis(), 5.0f,
        // X acceleration (device moving sideways)
        -3.0f,
        // Y acceleration (device moving backward)
        12.0f
        // Z acceleration (gravity + upward motion)
    );

    assertEquals("Multi-axis X", 5.0f, multiAxis.x, 0.001f);
    assertEquals("Multi-axis Y", -3.0f, multiAxis.y, 0.001f);
    assertEquals("Multi-axis Z", 12.0f, multiAxis.z, 0.001f);

    // Calculate resultant acceleration: sqrt(5² + (-3)² + 12²) = sqrt(25 + 9 + 144) = sqrt(178)
    // ≈ 13.34
    float resultant = (float) Math.sqrt(multiAxis.x * multiAxis.x
                                        + multiAxis.y * multiAxis.y
                                        + multiAxis.z * multiAxis.z);
    assertEquals("Resultant acceleration should be calculated correctly", 13.34f, resultant, 0.1f);
  }

  @Test
  public void testAccelerometerVsGravityComparison() {
    // Test that accelerometer readings are different from pure gravity readings
    // (accelerometer includes both gravity and linear acceleration)

    // Device at rest (accelerometer should show only gravity)
    AccelerometerData atRest = new AccelerometerData(1, 1, "at_rest", 1, 1000L, 0.1f, 0.2f, 9.8f);

    // Device accelerating (accelerometer should show gravity + linear acceleration)
    AccelerometerData accelerating = new AccelerometerData(
        1,
                                                           2,
                                                           "accelerating",
                                                           1,
                                                           2000L,
                                                           2.0f,
                                                           1.5f,
                                                           12.5f
    );

    // At rest, magnitude should be close to gravity
    float restMagnitude = (float) Math.sqrt(atRest.x * atRest.x
                                            + atRest.y * atRest.y
                                            + atRest.z * atRest.z);
    assertTrue(
        "At rest magnitude should be close to gravity",
        Math.abs(restMagnitude - 9.8f) < 0.5f
    );

    // Accelerating, magnitude should be higher than gravity
    float accelMagnitude = (float) Math.sqrt(accelerating.x * accelerating.x
                                             + accelerating.y * accelerating.y
                                             + accelerating.z * accelerating.z);
    assertTrue("Accelerating magnitude should be higher than gravity", accelMagnitude > 10.0f);
  }
} 

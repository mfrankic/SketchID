package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for MagneticFieldData Room entity class.
 * Tests constructors, field assignments, and data integrity.
 */
public class MagneticFieldDataTest {

  @Test
  public void testDefaultConstructor() {
    MagneticFieldData data = new MagneticFieldData();
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
    int userId = 123;
    int imageId = 456;
    String sessionId = "session_789";
    int attempt = 3;
    long timestamp = System.currentTimeMillis();
    float x = 1.5f;
    float y = 2.5f;
    float z = 3.5f;

    MagneticFieldData data = new MagneticFieldData(
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
  public void testParameterizedConstructorWithBoundaryValues() {
    // Test with minimum values
    MagneticFieldData data1 = new MagneticFieldData(0, 0, "", 0, 0L, 0.0f, 0.0f, 0.0f);
    assertEquals("Min userId should work", 0, data1.userId);
    assertEquals("Min imageId should work", 0, data1.imageId);
    assertEquals("Empty sessionId should work", "", data1.sessionId);
    assertEquals("Min attempt should work", 0, data1.attempt);
    assertEquals("Min timestamp should work", 0L, data1.timestamp);
    assertEquals("Zero X should work", 0.0f, data1.x, 0.001f);
    assertEquals("Zero Y should work", 0.0f, data1.y, 0.001f);
    assertEquals("Zero Z should work", 0.0f, data1.z, 0.001f);

    // Test with maximum/large values
    MagneticFieldData data2 = new MagneticFieldData(
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        "very_long_session_id_with_many_characters_12345",
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
        "very_long_session_id_with_many_characters_12345",
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
    MagneticFieldData data = new MagneticFieldData(-1, -2, "session", -3, -4L, -1.5f, -2.5f, -3.5f);

    assertEquals("Negative userId should be allowed", -1, data.userId);
    assertEquals("Negative imageId should be allowed", -2, data.imageId);
    assertEquals("Negative attempt should be allowed", -3, data.attempt);
    assertEquals("Negative timestamp should be allowed", -4L, data.timestamp);
    assertEquals("Negative X should be allowed", -1.5f, data.x, 0.001f);
    assertEquals("Negative Y should be allowed", -2.5f, data.y, 0.001f);
    assertEquals("Negative Z should be allowed", -3.5f, data.z, 0.001f);
  }

  @Test
  public void testParameterizedConstructorWithNullSessionId() {
    MagneticFieldData data = new MagneticFieldData(1, 2, null, 3, 4L, 5.0f, 6.0f, 7.0f);

    assertNull("Null sessionId should be allowed", data.sessionId);
    assertEquals("Other fields should be preserved with null sessionId", 1, data.userId);
  }

  @Test
  public void testParameterizedConstructorWithSpecialFloatValues() {
    // Test with NaN values
    MagneticFieldData data1 = new MagneticFieldData(
        1,
                                                    2,
                                                    "session",
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
    MagneticFieldData data2 = new MagneticFieldData(
        1,
                                                    2,
                                                    "session",
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
    MagneticFieldData data = new MagneticFieldData();

    // Test setting ID (even though it's typically auto-generated)
    data.id = 100;
    assertEquals("ID should be assignable", 100, data.id);

    // Test setting userId
    data.userId = 200;
    assertEquals("UserId should be assignable", 200, data.userId);

    // Test setting imageId
    data.imageId = 300;
    assertEquals("ImageId should be assignable", 300, data.imageId);

    // Test setting sessionId
    data.sessionId = "new_session";
    assertEquals("SessionId should be assignable", "new_session", data.sessionId);

    // Test setting attempt
    data.attempt = 5;
    assertEquals("Attempt should be assignable", 5, data.attempt);

    // Test setting timestamp
    long newTimestamp = System.currentTimeMillis();
    data.timestamp = newTimestamp;
    assertEquals("Timestamp should be assignable", newTimestamp, data.timestamp);

    // Test setting coordinates
    data.x = 10.5f;
    data.y = 20.5f;
    data.z = 30.5f;
    assertEquals("X should be assignable", 10.5f, data.x, 0.001f);
    assertEquals("Y should be assignable", 20.5f, data.y, 0.001f);
    assertEquals("Z should be assignable", 30.5f, data.z, 0.001f);
  }

  @Test
  public void testFieldIndependence() {
    MagneticFieldData data = new MagneticFieldData();

    // Setting one field should not affect others
    data.userId = 1;
    assertEquals("Setting userId should not affect other fields", 0, data.imageId);
    assertEquals("Setting userId should not affect other fields", 0, data.attempt);
    assertNull("Setting userId should not affect other fields", data.sessionId);

    data.x = 5.0f;
    assertEquals("Setting x should not affect y", 0.0f, data.y, 0.001f);
    assertEquals("Setting x should not affect z", 0.0f, data.z, 0.001f);
    assertEquals("Setting x should not affect userId", 1, data.userId);
  }

  @Test
  public void testRealisticMagneticFieldData() {
    // Test with realistic magnetic field sensor data
    MagneticFieldData data = new MagneticFieldData(
        123,                          // userId
        456,                          // imageId
        "session_abc123",             // sessionId
        1,                            // attempt (first attempt)
        System.currentTimeMillis(),   // current timestamp
        20.5f,                        // X component (North, μT)
        -15.2f,                       // Y component (East, μT)
        45.8f                         // Z component (Down, μT)
    );

    assertEquals("Realistic userId", 123, data.userId);
    assertEquals("Realistic imageId", 456, data.imageId);
    assertEquals("Realistic sessionId", "session_abc123", data.sessionId);
    assertEquals("Realistic attempt", 1, data.attempt);
    assertTrue("Realistic timestamp should be positive", data.timestamp > 0);
    assertEquals("Realistic X (North component)", 20.5f, data.x, 0.001f);
    assertEquals("Realistic Y (East component)", -15.2f, data.y, 0.001f);
    assertEquals("Realistic Z (Down component)", 45.8f, data.z, 0.001f);

    // Calculate total magnetic field strength (should be realistic for Earth)
    float magnitude = (float) Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
    assertTrue(
        "Magnetic field magnitude should be realistic for Earth (25-65 μT)",
        magnitude >= 25.0f && magnitude <= 65.0f
    );
  }

  @Test
  public void testMagneticFieldVariations() {
    // Test with different magnetic field scenarios

    // Weak magnetic field (near equator)
    MagneticFieldData weakField = new MagneticFieldData(
        1,
                                                        1,
                                                        "session1",
                                                        1,
                                                        1000L,
                                                        10.0f,
                                                        8.0f,
                                                        25.0f
    );
    assertEquals("Weak field X", 10.0f, weakField.x, 0.001f);
    assertEquals("Weak field Y", 8.0f, weakField.y, 0.001f);
    assertEquals("Weak field Z", 25.0f, weakField.z, 0.001f);

    // Strong magnetic field (near poles)
    MagneticFieldData strongField = new MagneticFieldData(
        2,
                                                          2,
                                                          "session2",
                                                          1,
                                                          2000L,
                                                          5.0f,
                                                          -3.0f,
                                                          60.0f
    );
    assertEquals("Strong field X", 5.0f, strongField.x, 0.001f);
    assertEquals("Strong field Y", -3.0f, strongField.y, 0.001f);
    assertEquals("Strong field Z", 60.0f, strongField.z, 0.001f);

    // Disturbed magnetic field (interference)
    MagneticFieldData disturbedField = new MagneticFieldData(
        3,
                                                             3,
                                                             "session3",
                                                             1,
                                                             3000L,
                                                             100.0f,
                                                             -80.0f,
                                                             150.0f
    );
    assertEquals("Disturbed field X", 100.0f, disturbedField.x, 0.001f);
    assertEquals("Disturbed field Y", -80.0f, disturbedField.y, 0.001f);
    assertEquals("Disturbed field Z", 150.0f, disturbedField.z, 0.001f);
  }

  @Test
  public void testMultipleDataPoints() {
    // Test creating multiple data points as would happen during sensor collection
    MagneticFieldData[] dataPoints = new MagneticFieldData[5];
    long baseTimestamp = System.currentTimeMillis();

    for (int i = 0; i < 5; i++) {
      dataPoints[i] = new MagneticFieldData(
          1,                              // same user
          1,                              // same image
          "session_test",                 // same session
          1,                              // same attempt
          baseTimestamp + (i * 100),      // incrementing timestamp
          20.0f + (i * 0.1f),            // slightly varying X
          -15.0f + (i * 0.05f),          // slightly varying Y
          45.0f + (i * 0.02f)            // slightly varying Z
      );
    }

    // Verify each data point
    for (int i = 0; i < 5; i++) {
      assertEquals("Data point " + i + " userId", 1, dataPoints[i].userId);
      assertEquals("Data point " + i + " imageId", 1, dataPoints[i].imageId);
      assertEquals("Data point " + i + " sessionId", "session_test", dataPoints[i].sessionId);
      assertEquals("Data point " + i + " attempt", 1, dataPoints[i].attempt);
      assertEquals(
          "Data point " + i + " timestamp",
          baseTimestamp + (i * 100),
          dataPoints[i].timestamp
      );
      assertEquals("Data point " + i + " X", 20.0f + (i * 0.1f), dataPoints[i].x, 0.001f);
      assertEquals("Data point " + i + " Y", -15.0f + (i * 0.05f), dataPoints[i].y, 0.001f);
      assertEquals("Data point " + i + " Z", 45.0f + (i * 0.02f), dataPoints[i].z, 0.001f);
    }
  }

  @Test
  public void testSessionIdVariants() {
    // Test different session ID formats
    String[] sessionIds = {
        "simple",
        "session_123",
        "SESSION_ABC_456",
        "uuid-like-550e8400-e29b-41d4-a716-446655440000",
        "mixed_Case_123_ABC",
        "with-dashes-and_underscores",
        "numbers123456789",
        "",
        null
    };

    for (int i = 0; i < sessionIds.length; i++) {
      MagneticFieldData data = new MagneticFieldData(
          i,
                                                     i,
                                                     sessionIds[i],
                                                     i,
                                                     i * 1000L,
                                                     i * 1.0f,
                                                     i * 2.0f,
                                                     i * 3.0f
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
    // Test that timestamps can represent a realistic progression
    long baseTime = System.currentTimeMillis();
    int samplingRate = 50; // Hz (20ms intervals)
    int intervalMs = 1000 / samplingRate;

    MagneticFieldData[] samples = new MagneticFieldData[10];
    for (int i = 0; i < 10; i++) {
      samples[i] = new MagneticFieldData(
          1, 1, "session", 1, baseTime + (i * intervalMs),  // 20ms intervals
          20.0f, -15.0f, 45.0f
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
    MagneticFieldData data = new MagneticFieldData(1, 2, "initial", 3, 4L, 5.0f, 6.0f, 7.0f);

    // Verify initial values
    assertEquals("Initial userId", 1, data.userId);
    assertEquals("Initial sessionId", "initial", data.sessionId);
    assertEquals("Initial X", 5.0f, data.x, 0.001f);

    // Modify values
    data.userId = 10;
    data.sessionId = "modified";
    data.x = 50.0f;

    // Verify changes
    assertEquals("Modified userId", 10, data.userId);
    assertEquals("Modified sessionId", "modified", data.sessionId);
    assertEquals("Modified X", 50.0f, data.x, 0.001f);
  }

  @Test
  public void testFloatPrecision() {
    MagneticFieldData data = new MagneticFieldData();

    // Test very small values
    data.x = 0.000001f;
    data.y = -0.000001f;
    data.z = 0.0000005f;

    assertEquals("Very small X", 0.000001f, data.x, 0.0000001f);
    assertEquals("Very small Y", -0.000001f, data.y, 0.0000001f);
    assertEquals("Very small Z", 0.0000005f, data.z, 0.0000001f);

    // Test precise decimal values
    data.x = 1.23456789f;
    data.y = 2.34567891f;
    data.z = 3.45678912f;

    assertEquals("Precise X", 1.23456789f, data.x, 0.0001f);
    assertEquals("Precise Y", 2.34567891f, data.y, 0.0001f);
    assertEquals("Precise Z", 3.45678912f, data.z, 0.0001f);
  }

  @Test
  public void testIntegerBoundaries() {
    MagneticFieldData data = new MagneticFieldData();

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
    MagneticFieldData data = new MagneticFieldData(
        1,
                                                   2,
                                                   "会话_αβγ_session_🔬",
                                                   3,
                                                   4L,
                                                   5.0f,
                                                   6.0f,
                                                   7.0f
    );

    assertEquals("Unicode sessionId should be preserved", "会话_αβγ_session_🔬", data.sessionId);
  }

  @Test
  public void testEarthMagneticFieldRange() {
    // Test with values within Earth's magnetic field range
    MagneticFieldData earthField = new MagneticFieldData(
        1,
                                                         1,
                                                         "earth_field",
                                                         1,
                                                         System.currentTimeMillis(),
                                                         25.0f,
                                                         -20.0f,
                                                         40.0f
                                                         // Typical Earth field components
    );

    float magnitude = (float) Math.sqrt(earthField.x * earthField.x
                                        + earthField.y * earthField.y
                                        + earthField.z * earthField.z);

    assertTrue(
        "Earth magnetic field magnitude should be realistic",
        magnitude >= 25.0f && magnitude <= 65.0f
    );
    assertEquals("Earth field X component", 25.0f, earthField.x, 0.001f);
    assertEquals("Earth field Y component", -20.0f, earthField.y, 0.001f);
    assertEquals("Earth field Z component", 40.0f, earthField.z, 0.001f);
  }
} 

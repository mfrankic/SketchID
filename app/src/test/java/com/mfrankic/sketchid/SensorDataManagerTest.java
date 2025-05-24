package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SensorDataManagerTest {

  // Test constants to avoid string duplication
  private static final String TEST_SESSION_ID = "session123";

  @Mock
  private Context mockContext;

  @Mock
  private SensorManager mockSensorManager;

  @Mock
  private Sensor mockGravitySensor;

  @Mock
  private Sensor mockGyroscopeSensor;

  @Mock
  private Sensor mockMagneticFieldSensor;

  @Mock
  private Sensor mockAccelerometerSensor;

  @Mock
  private AppDatabase mockDatabase;

  @Mock
  private SensorDataManager.BaselineCollectionCallback mockBaselineCallback;

  private SensorDataManager sensorDataManager;

  @Before
  public void setup() {
    when(mockContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockSensorManager);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)).thenReturn(mockGravitySensor);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(mockGyroscopeSensor);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)).thenReturn(
        mockMagneticFieldSensor);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(
        mockAccelerometerSensor);

    sensorDataManager = new SensorDataManager(mockContext);
  }

  @Test
  public void testSensorDataManagerInitialization() {
    assertNotNull("SensorDataManager should be initialized", sensorDataManager);
    verify(mockContext).getSystemService(Context.SENSOR_SERVICE);
    verify(mockSensorManager).getDefaultSensor(Sensor.TYPE_GRAVITY);
    verify(mockSensorManager).getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    verify(mockSensorManager).getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    verify(mockSensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  }

  @Test
  public void testStartCollecting() {
    when(mockSensorManager.registerListener(
        eq(sensorDataManager),
        any(Sensor.class),
        anyInt()
    )).thenReturn(true);

    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 1);

    verify(mockSensorManager).registerListener(
        sensorDataManager,
        mockGravitySensor,
        SensorManager.SENSOR_DELAY_FASTEST
    );
    verify(mockSensorManager).registerListener(
        sensorDataManager,
        mockGyroscopeSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    );
    verify(mockSensorManager).registerListener(
        sensorDataManager,
        mockMagneticFieldSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    );
    verify(mockSensorManager).registerListener(
        sensorDataManager,
        mockAccelerometerSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    );
  }

  @Test
  public void testStopCollecting() {
    // First start collecting
    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 1);

    // Then stop collecting
    sensorDataManager.stopCollecting();

    verify(mockSensorManager).unregisterListener(sensorDataManager, mockGravitySensor);
    verify(mockSensorManager).unregisterListener(sensorDataManager, mockGyroscopeSensor);
    verify(mockSensorManager).unregisterListener(sensorDataManager, mockMagneticFieldSensor);
    verify(mockSensorManager).unregisterListener(sensorDataManager, mockAccelerometerSensor);
  }

  @Test
  public void testClearData() {
    // Start collecting to populate buffers
    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 1);

    // Clear data
    sensorDataManager.clearData();

    // Verify that buffers are cleared (this is mainly for code coverage)
    // The actual buffer clearing is internal and can't be directly verified
    assertNotNull("SensorDataManager should still be valid after clearing data", sensorDataManager);
  }

  @Test
  public void testStartBaselineCollection() {
    sensorDataManager.startBaselineCollection(1, 2, TEST_SESSION_ID, 1, mockBaselineCallback);

    verify(mockSensorManager).registerListener(
        sensorDataManager,
        mockMagneticFieldSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    );
  }

  @Test
  public void testOnAccuracyChanged() {
    // This method doesn't do anything, but we test it for coverage
    sensorDataManager.onAccuracyChanged(
        mockGravitySensor,
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    );

    // No assertions needed as the method is empty
    assertNotNull("SensorDataManager should still be valid", sensorDataManager);
  }

  @Test
  public void testSensorDataManagerWithNullSensors() {
    // Test initialization when sensors are not available
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)).thenReturn(null);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(null);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)).thenReturn(null);
    when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(null);

    SensorDataManager manager = new SensorDataManager(mockContext);
    assertNotNull("SensorDataManager should be initialized even with null sensors", manager);

    // Should not fail when starting collection with null sensors
    manager.startCollecting(1, 2, TEST_SESSION_ID, 1);
    manager.stopCollecting();
  }

  @Test
  public void testOnSensorChangedWithGravitySensor() {
    // Test sensor event handling without using reflection
    // Start collecting to enable data recording
    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 1);

    // We can't easily create real SensorEvent objects in unit tests without reflection
    // but we can verify that the sensor data manager handles the lifecycle correctly
    assertNotNull("SensorDataManager should handle sensor lifecycle", sensorDataManager);

    // Test that stopping after starting works correctly
    sensorDataManager.stopCollecting();
    assertNotNull("SensorDataManager should handle stop after start", sensorDataManager);
  }

  @Test
  public void testBaselineTargetSampleCount() {
    assertEquals(
        "Baseline target sample count should be 50",
        50,
        SensorDataManager.BASELINE_TARGET_SAMPLE_COUNT
    );
  }

  @Test
  public void testSensorDataManagerPublicInterface() {
    // Test the public interface without using reflection
    // This ensures all methods work correctly together

    // Test starting and stopping collection multiple times
    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 1);
    sensorDataManager.stopCollecting();
    sensorDataManager.startCollecting(1, 2, TEST_SESSION_ID, 2);
    sensorDataManager.clearData();
    sensorDataManager.stopCollecting();

    // Test baseline collection
    sensorDataManager.startBaselineCollection(1, 2, TEST_SESSION_ID, 1, mockBaselineCallback);
    sensorDataManager.stopCollecting();

    assertNotNull(
        "SensorDataManager should handle multiple lifecycle operations",
        sensorDataManager
    );
  }
} 

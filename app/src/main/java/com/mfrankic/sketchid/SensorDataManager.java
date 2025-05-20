package com.mfrankic.sketchid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SensorDataManager implements SensorEventListener {
  private static final String TAG = "SensorDataManager";

  private final SensorManager sensorManager;
  private final Sensor gravitySensor;
  private final Sensor gyroscopeSensor;
  private final Sensor magneticFieldSensor;
  private final Sensor accelerometerSensor;
  private final Context context;
  private final List<GravityData> gravityDataBuffer;
  private final List<GyroscopeData> gyroscopeDataBuffer;
  private final List<MagneticFieldData> magneticFieldDataBuffer;
  private final List<AccelerometerData> accelerometerDataBuffer;

  private boolean isCollecting = false;
  private int userId;
  private int imageId;
  private String sessionId;
  private int attempt;

  public SensorDataManager(Context context) {
    this.context = context;
    this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    this.gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    this.gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    this.magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    this.gravityDataBuffer = new ArrayList<>(10000);
    this.gyroscopeDataBuffer = new ArrayList<>(20000);
    this.magneticFieldDataBuffer = new ArrayList<>(10000);
    this.accelerometerDataBuffer = new ArrayList<>(20000);

    if (gravitySensor == null) {
      Log.w(TAG, "Gravity sensor not available on this device");
    }

    if (gyroscopeSensor == null) {
      Log.w(TAG, "Gyroscope sensor not available on this device");
    }

    if (magneticFieldSensor == null) {
      Log.w(TAG, "Magnetic field sensor not available on this device");
    }

    if (accelerometerSensor == null) {
      Log.w(TAG, "Accelerometer sensor not available on this device");
    }
  }

  public void startCollecting(int userId, int imageId, String sessionId, int attempt) {
    this.userId = userId;
    this.imageId = imageId;
    this.sessionId = sessionId;
    this.attempt = attempt;

    gravityDataBuffer.clear();
    gyroscopeDataBuffer.clear();
    magneticFieldDataBuffer.clear();
    accelerometerDataBuffer.clear();

    if (gravitySensor != null) {
      sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
      Log.d(TAG, "Started collecting gravity sensor data");
    } else {
      Log.w(TAG, "Cannot collect gravity data: sensor not available");
    }

    if (gyroscopeSensor != null) {
      sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
      Log.d(TAG, "Started collecting gyroscope sensor data");
    } else {
      Log.w(TAG, "Cannot collect gyroscope data: sensor not available");
    }

    if (magneticFieldSensor != null) {
      sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
      Log.d(TAG, "Started collecting magnetic field sensor data");
    } else {
      Log.w(TAG, "Cannot collect magnetic field data: sensor not available");
    }

    if (accelerometerSensor != null) {
      sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
      Log.d(TAG, "Started collecting accelerometer sensor data");
    } else {
      Log.w(TAG, "Cannot collect accelerometer data: sensor not available");
    }

    isCollecting = true;
  }

  public void stopCollecting() {
    if (isCollecting) {
      if (gravitySensor != null) {
        sensorManager.unregisterListener(this, gravitySensor);
        Log.d(
            TAG,
            "Stopped collecting gravity sensor data. Buffer size: " + gravityDataBuffer.size()
        );
      }

      if (gyroscopeSensor != null) {
        sensorManager.unregisterListener(this, gyroscopeSensor);
        Log.d(
            TAG,
            "Stopped collecting gyroscope sensor data. Buffer size: " + gyroscopeDataBuffer.size()
        );
      }

      if (magneticFieldSensor != null) {
        sensorManager.unregisterListener(this, magneticFieldSensor);
        Log.d(
            TAG,
            "Stopped collecting magnetic field sensor data. Buffer size: "
            + magneticFieldDataBuffer.size()
        );
      }

      if (accelerometerSensor != null) {
        sensorManager.unregisterListener(this, accelerometerSensor);
        Log.d(
            TAG,
            "Stopped collecting accelerometer sensor data. Buffer size: "
            + accelerometerDataBuffer.size()
        );
      }

      isCollecting = false;
    }
  }

  public void clearData() {
    gravityDataBuffer.clear();
    gyroscopeDataBuffer.clear();
    magneticFieldDataBuffer.clear();
    accelerometerDataBuffer.clear();
    Log.d(TAG, "Cleared all sensor data buffers");
  }

  public void saveData() {
    AppDatabase db = AppDatabase.getInstance(context);

    if (!gravityDataBuffer.isEmpty()) {
      Log.d(TAG, "Saving " + gravityDataBuffer.size() + " gravity data points to database");

      new Thread(() -> {
        try {
          db.gravityDataDao().insertAll(new ArrayList<>(gravityDataBuffer));
          Log.d(TAG, "Successfully saved gravity sensor data to database");
        } catch (Exception e) {
          Log.e(TAG, "Error saving gravity sensor data", e);
        }
        gravityDataBuffer.clear();
      }).start();
    } else {
      Log.d(TAG, "No gravity data to save");
    }

    if (!gyroscopeDataBuffer.isEmpty()) {
      Log.d(TAG, "Saving " + gyroscopeDataBuffer.size() + " gyroscope data points to database");

      new Thread(() -> {
        try {
          db.gyroscopeDataDao().insertAll(new ArrayList<>(gyroscopeDataBuffer));
          Log.d(TAG, "Successfully saved gyroscope sensor data to database");
        } catch (Exception e) {
          Log.e(TAG, "Error saving gyroscope sensor data", e);
        }
        gyroscopeDataBuffer.clear();
      }).start();
    } else {
      Log.d(TAG, "No gyroscope data to save");
    }

    if (!magneticFieldDataBuffer.isEmpty()) {
      Log.d(
          TAG,
          "Saving " + magneticFieldDataBuffer.size() + " magnetic field data points to database"
      );

      new Thread(() -> {
        try {
          db.magneticFieldDataDao().insertAll(new ArrayList<>(magneticFieldDataBuffer));
          Log.d(TAG, "Successfully saved magnetic field sensor data to database");
        } catch (Exception e) {
          Log.e(TAG, "Error saving magnetic field sensor data", e);
        }
        magneticFieldDataBuffer.clear();
      }).start();
    } else {
      Log.d(TAG, "No magnetic field data to save");
    }

    if (!accelerometerDataBuffer.isEmpty()) {
      Log.d(
          TAG,
          "Saving " + accelerometerDataBuffer.size() + " accelerometer data points to database"
      );

      new Thread(() -> {
        try {
          db.accelerometerDataDao().insertAll(new ArrayList<>(accelerometerDataBuffer));
          Log.d(TAG, "Successfully saved accelerometer sensor data to database");
        } catch (Exception e) {
          Log.e(TAG, "Error saving accelerometer sensor data", e);
        }
        accelerometerDataBuffer.clear();
      }).start();
    } else {
      Log.d(TAG, "No accelerometer data to save");
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (!isCollecting) {
      return;
    }

    long timestamp = SystemClock.uptimeMillis();

    switch (event.sensor.getType()) {
      case Sensor.TYPE_GRAVITY:
        GravityData gravityData = new GravityData(
            userId,
            imageId,
            sessionId,
            attempt,
            timestamp,
            event.values[0],
            event.values[1],
            event.values[2]
        );
        gravityDataBuffer.add(gravityData);
        break;

      case Sensor.TYPE_GYROSCOPE:
        GyroscopeData gyroscopeData = new GyroscopeData(
            userId,
            imageId,
            sessionId,
            attempt,
            timestamp,
            event.values[0],
            event.values[1],
            event.values[2]
        );
        gyroscopeDataBuffer.add(gyroscopeData);
        break;

      case Sensor.TYPE_MAGNETIC_FIELD:
        MagneticFieldData magneticFieldData = new MagneticFieldData(
            userId,
            imageId,
            sessionId,
            attempt,
            timestamp,
            event.values[0],
            event.values[1],
            event.values[2]
        );
        magneticFieldDataBuffer.add(magneticFieldData);
        break;

      case Sensor.TYPE_ACCELEROMETER:
        AccelerometerData accelerometerData = new AccelerometerData(
            userId,
            imageId,
            sessionId,
            attempt,
            timestamp,
            event.values[0],
            event.values[1],
            event.values[2]
        );
        accelerometerDataBuffer.add(accelerometerData);
        break;
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Not handling accuracy changes
  }
} 
 
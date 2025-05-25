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
  public static final int BASELINE_TARGET_SAMPLE_COUNT = 50;
  private static final String TAG = "SensorDataManager";
  private static final String LOG_SAVING = "Saving ";
  private static final String LOG_SUCCESSFULLY_SAVED = "Successfully saved ";
  private static final String LOG_ERROR_SAVING = "Error saving ";
  private static final String LOG_NO_DATA_TO_SAVE = "No ";
  private static final String LOG_DATA_TO_SAVE_SUFFIX = " data to save";
  private static final String LOG_DATA_POINTS_SUFFIX = " data points to database";
  private static final String LOG_SENSOR_DATA_TO_DB_SUFFIX = " sensor data to database";
  private static final String SENSOR_GRAVITY = "gravity";
  private static final String SENSOR_GYROSCOPE = "gyroscope";
  private static final String SENSOR_MAGNETIC_FIELD = "magnetic field";
  private static final String SENSOR_ACCELEROMETER = "accelerometer";
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
  private final List<Float> baselineMagneticFieldX;
  private final List<Float> baselineMagneticFieldY;
  private final List<Float> baselineMagneticFieldZ;
  private final List<Long> baselineTimestamps;
  private boolean isCollecting = false;
  private boolean isCollectingBaseline = false;
  private int userId;
  private int imageId;
  private String sessionId;
  private int attempt;

  private BaselineCollectionCallback baselineCallback;

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

    this.baselineMagneticFieldX = new ArrayList<>(50);
    this.baselineMagneticFieldY = new ArrayList<>(50);
    this.baselineMagneticFieldZ = new ArrayList<>(50);
    this.baselineTimestamps = new ArrayList<>(50);

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

    if (isCollectingBaseline) {
      isCollectingBaseline = false;
      if (magneticFieldSensor != null) {
        sensorManager.unregisterListener(this, magneticFieldSensor);
      }
      if (baselineCallback != null) {
        baselineCallback.onBaselineCollectionComplete(false);
        baselineCallback = null;
      }
      Log.d(TAG, "Stopped baseline collection");
    }
  }

  public void clearData() {
    gravityDataBuffer.clear();
    gyroscopeDataBuffer.clear();
    magneticFieldDataBuffer.clear();
    accelerometerDataBuffer.clear();
    clearBaselineData();

    if (baselineCallback != null) {
      baselineCallback = null;
    }

    Log.d(TAG, "Cleared all sensor data buffers");
  }

  private void clearBaselineData() {
    baselineMagneticFieldX.clear();
    baselineMagneticFieldY.clear();
    baselineMagneticFieldZ.clear();
    baselineTimestamps.clear();
  }

  /**
   * Start collecting baseline magnetic field data before drawing attempt
   *
   * @param userId    User ID
   * @param imageId   Image ID
   * @param sessionId Session ID
   * @param attempt   Attempt number
   * @param callback  Callback when baseline collection is complete
   */
  public void startBaselineCollection(
      int userId,
      int imageId,
      String sessionId,
      int attempt,
      BaselineCollectionCallback callback
  ) {
    if (magneticFieldSensor == null) {
      Log.w(TAG, "Cannot collect baseline: magnetic field sensor not available");
      callback.onBaselineCollectionComplete(false);
      return;
    }

    this.userId = userId;
    this.imageId = imageId;
    this.sessionId = sessionId;
    this.attempt = attempt;

    clearBaselineData();

    isCollectingBaseline = true;

    sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
    Log.d(
        TAG,
        "Started collecting baseline magnetic field data for "
        + BASELINE_TARGET_SAMPLE_COUNT
        + " samples"
    );

    this.baselineCallback = callback;
  }

  public void saveData() {
    AppDatabase db = AppDatabase.getInstance(context);

    saveGravityData(db);
    saveGyroscopeData(db);
    saveMagneticFieldData(db);
    saveAccelerometerData(db);
  }

  private void saveGravityData(AppDatabase db) {
    if (!gravityDataBuffer.isEmpty()) {
      logSavingData(gravityDataBuffer.size());

      new Thread(() -> {
        try {
          db.gravityDataDao().insertAll(new ArrayList<>(gravityDataBuffer));
          logSuccessfullySaved(SENSOR_GRAVITY);
        } catch (Exception e) {
          logErrorSaving(SENSOR_GRAVITY, e);
        }
        gravityDataBuffer.clear();
      }).start();
    } else {
      logNoDataToSave(SENSOR_GRAVITY);
    }
  }

  private void logSavingData(int size) {
    Log.d(TAG, LOG_SAVING + size + LOG_DATA_POINTS_SUFFIX + LOG_SENSOR_DATA_TO_DB_SUFFIX);
  }

  private void logSuccessfullySaved(String sensorType) {
    Log.d(TAG, LOG_SUCCESSFULLY_SAVED + sensorType + LOG_SENSOR_DATA_TO_DB_SUFFIX);
  }

  private void logErrorSaving(String sensorType, Exception e) {
    Log.e(TAG, LOG_ERROR_SAVING + sensorType + " sensor data", e);
  }

  private void logNoDataToSave(String sensorType) {
    Log.d(TAG, LOG_NO_DATA_TO_SAVE + sensorType + LOG_DATA_TO_SAVE_SUFFIX);
  }

  private void saveGyroscopeData(AppDatabase db) {
    if (!gyroscopeDataBuffer.isEmpty()) {
      logSavingData(gyroscopeDataBuffer.size());

      new Thread(() -> {
        try {
          db.gyroscopeDataDao().insertAll(new ArrayList<>(gyroscopeDataBuffer));
          logSuccessfullySaved(SENSOR_GYROSCOPE);
        } catch (Exception e) {
          logErrorSaving(SENSOR_GYROSCOPE, e);
        }
        gyroscopeDataBuffer.clear();
      }).start();
    } else {
      logNoDataToSave(SENSOR_GYROSCOPE);
    }
  }

  private void saveMagneticFieldData(AppDatabase db) {
    if (!magneticFieldDataBuffer.isEmpty()) {
      logSavingData(magneticFieldDataBuffer.size());

      new Thread(() -> {
        try {
          db.magneticFieldDataDao().insertAll(new ArrayList<>(magneticFieldDataBuffer));
          logSuccessfullySaved(SENSOR_MAGNETIC_FIELD);
        } catch (Exception e) {
          logErrorSaving(SENSOR_MAGNETIC_FIELD, e);
        }
        magneticFieldDataBuffer.clear();
      }).start();
    } else {
      logNoDataToSave(SENSOR_MAGNETIC_FIELD);
    }
  }

  private void saveAccelerometerData(AppDatabase db) {
    if (!accelerometerDataBuffer.isEmpty()) {
      logSavingData(accelerometerDataBuffer.size());

      new Thread(() -> {
        try {
          db.accelerometerDataDao().insertAll(new ArrayList<>(accelerometerDataBuffer));
          logSuccessfullySaved(SENSOR_ACCELEROMETER);
        } catch (Exception e) {
          logErrorSaving(SENSOR_ACCELEROMETER, e);
        }
        accelerometerDataBuffer.clear();
      }).start();
    } else {
      logNoDataToSave(SENSOR_ACCELEROMETER);
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    long timestamp = SystemClock.uptimeMillis();

    if (isCollectingBaseline && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      baselineMagneticFieldX.add(event.values[0]);
      baselineMagneticFieldY.add(event.values[1]);
      baselineMagneticFieldZ.add(event.values[2]);
      baselineTimestamps.add(timestamp);

      // Check if we've reached the target sample count
      if (baselineMagneticFieldX.size() >= BASELINE_TARGET_SAMPLE_COUNT) {
        Log.d(TAG, "Baseline sample target reached: " + baselineMagneticFieldX.size() + " samples");
        finishBaselineCollection();
      }
      return;
    }

    if (!isCollecting) {
      return;
    }

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

      default:
    }
  }

  private void finishBaselineCollection() {
    if (!isCollectingBaseline || baselineCallback == null) {
      return;
    }

    isCollectingBaseline = false;
    sensorManager.unregisterListener(this, magneticFieldSensor);

    boolean success = processAndSaveBaseline();
    Log.d(
        TAG,
        "Finished baseline collection. Samples collected: "
        + baselineMagneticFieldX.size()
        + ", Success: "
        + success
    );

    BaselineCollectionCallback callback = baselineCallback;
    baselineCallback = null;
    callback.onBaselineCollectionComplete(success);
  }

  private boolean processAndSaveBaseline() {
    if (baselineMagneticFieldX.isEmpty()) {
      Log.w(TAG, "No baseline data collected");
      return false;
    }

    int sampleCount = baselineMagneticFieldX.size();
    long startTime = baselineTimestamps.get(0);
    long endTime = baselineTimestamps.get(sampleCount - 1);
    long duration = endTime - startTime;

    float avgX = (float) baselineMagneticFieldX
        .stream()
        .mapToDouble(Float::doubleValue)
        .average()
        .orElse(0.0);
    float avgY = (float) baselineMagneticFieldY
        .stream()
        .mapToDouble(Float::doubleValue)
        .average()
        .orElse(0.0);
    float avgZ = (float) baselineMagneticFieldZ
        .stream()
        .mapToDouble(Float::doubleValue)
        .average()
        .orElse(0.0);

    float stdDevX = calculateStandardDeviation(baselineMagneticFieldX, avgX);
    float stdDevY = calculateStandardDeviation(baselineMagneticFieldY, avgY);
    float stdDevZ = calculateStandardDeviation(baselineMagneticFieldZ, avgZ);

    MagneticFieldBaselineData baselineData = new MagneticFieldBaselineData(
        userId,
        imageId,
        sessionId,
        attempt,
        startTime,
        avgX,
        avgY,
        avgZ,
        duration,
        sampleCount,
        stdDevX,
        stdDevY,
        stdDevZ
    );

    AppDatabase db = AppDatabase.getInstance(context);
    new Thread(() -> {
      try {
        db.magneticFieldBaselineDataDao().insertBaseline(baselineData);
        Log.d(TAG, "Successfully saved baseline data");
      } catch (Exception e) {
        Log.e(TAG, "Error saving baseline data", e);
      }
    }).start();

    return true;
  }

  private float calculateStandardDeviation(List<Float> values, float mean) {
    double sum = 0.0;
    for (float value : values) {
      sum += Math.pow(value - mean, 2);
    }
    return (float) Math.sqrt(sum / values.size());
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Not used
  }

  /**
   * Interface for baseline collection completion callback
   */
  public interface BaselineCollectionCallback {
    void onBaselineCollectionComplete(boolean success);
  }
} 
 
package com.mfrankic.sketchid;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The main Room database for the SketchID application.
 * This class defines the database configuration and serves as the main access point
 * to the persisted data. It follows a singleton pattern to ensure only one instance
 * of the database is created and used throughout the application.
 * <p>
 * The database includes entities for drawing data, users, images, and various sensor data.
 * It also handles initial data population for images if the database is empty.
 */
@Database(
    entities = {
        DrawingData.class,
        User.class,
        Image.class,
        GravityData.class,
        GyroscopeData.class,
        MagneticFieldData.class,
        MagneticFieldBaselineData.class,
        AccelerometerData.class
    }, version = 20, exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
  private static AppDatabase instance;

  /**
   * Returns the singleton instance of the {@link AppDatabase}.
   * If an instance does not exist, it creates a new one.
   * This method is synchronized to ensure thread-safe access.
   * <p>
   * The database is built with destructive migration, meaning that if the schema version changes,
   * the old database will be destroyed and a new one created. This is generally suitable for
   * development
   * but should be replaced with proper migration strategies for production applications.
   * <p>
   * If the image table is empty upon first creation, it populates it with initial data from
   * {@link InitialData#getImages()} on a background thread.
   *
   * @param context The application context.
   * @return The singleton {@link AppDatabase} instance.
   */
  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room
          .databaseBuilder(context.getApplicationContext(),
                           AppDatabase.class,
                           "drawing_database.db"
          )
          .fallbackToDestructiveMigration()
          .build();

      // Populate initial image data if the database is new and empty
      new Thread(() -> {
        if (instance.imageDao().getAllImages().isEmpty()) {
          instance.imageDao().insertAll(InitialData.getImages());
        }
      }).start();
    }
    return instance;
  }

  /**
   * Returns the Data Access Object for {@link Image} entities.
   *
   * @return The {@link ImageDao}.
   */
  public abstract ImageDao imageDao();

  /**
   * Returns the Data Access Object for {@link DrawingData} entities.
   *
   * @return The {@link DrawingDataDao}.
   */
  public abstract DrawingDataDao drawingDataDao();

  /**
   * Returns the Data Access Object for {@link User} entities.
   *
   * @return The {@link UserDao}.
   */
  public abstract UserDao userDao();

  /**
   * Returns the Data Access Object for {@link GravityData} entities.
   *
   * @return The {@link GravityDataDao}.
   */
  public abstract GravityDataDao gravityDataDao();

  /**
   * Returns the Data Access Object for {@link GyroscopeData} entities.
   *
   * @return The {@link GyroscopeDataDao}.
   */
  public abstract GyroscopeDataDao gyroscopeDataDao();

  /**
   * Returns the Data Access Object for {@link MagneticFieldData} entities.
   *
   * @return The {@link MagneticFieldDataDao}.
   */
  public abstract MagneticFieldDataDao magneticFieldDataDao();

  /**
   * Returns the Data Access Object for {@link MagneticFieldBaselineData} entities.
   *
   * @return The {@link MagneticFieldBaselineDataDao}.
   */
  public abstract MagneticFieldBaselineDataDao magneticFieldBaselineDataDao();

  /**
   * Returns the Data Access Object for {@link AccelerometerData} entities.
   *
   * @return The {@link AccelerometerDataDao}.
   */
  public abstract AccelerometerDataDao accelerometerDataDao();
}

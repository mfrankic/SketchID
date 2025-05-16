package com.mfrankic.sketchid;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Room database for the application
 * <p>
 * Uses a Singleton pattern for the following reasons:
 * 1. Room databases are expensive to create, and it's recommended to only create one instance
 * that's shared across the entire application to avoid unnecessary overhead
 * 2. Sharing a single database instance ensures consistent access to data across components
 * 3. This approach follows the official Android architecture recommendation for Room databases
 * 4. The database operations are thread-safe due to Room's internal synchronization
 */
@Database(
    entities = {DrawingData.class, User.class, Image.class}, version = 14, exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

  // Singleton instance using AtomicReference for thread safety
  private static final AtomicReference<AppDatabase> instance = new AtomicReference<>();

  // Protected constructor to allow Room to access it while preventing direct instantiation
  protected AppDatabase() {
  }

  /**
   * Returns the singleton database instance.
   * <p>
   * Thread safety is ensured through AtomicReference and synchronized block.
   *
   * @param context Application context for database creation (required on first call)
   * @return The singleton database instance
   */
  public static AppDatabase getInstance(Context context) {
    // First check (no synchronization overhead)
    AppDatabase db = instance.get();

    if (db == null) {
      synchronized (AppDatabase.class) {
        // Second check inside synchronized block
        db = instance.get();
        if (db == null) {
          if (context == null) {
            throw new IllegalArgumentException(
                "Context cannot be null on first database initialization");
          }

          db = Room
              .databaseBuilder(context.getApplicationContext(),
                               AppDatabase.class,
                               "drawing_database.db"
              )
              .fallbackToDestructiveMigration(true)
              .addCallback(roomDatabaseCallback)
              .build();

          instance.set(db);
        }
      }
    }

    return instance.get();
  }  // Database initialization callback

  // Abstract DAO methods that Room will implement
  public abstract DrawingDataDao drawingDataDao();

  public abstract UserDao userDao();

  public abstract ImageDao imageDao();

  private static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
      super.onCreate(db);

      Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase database = getInstance(null);
        if (database != null && database.imageDao().getAllImages().isEmpty()) {
          // Populate with initial data when database is first created
          List<Image> images = InitialData.getImages();
          database.imageDao().insertAll(images);
        }
      });
    }
  };


}

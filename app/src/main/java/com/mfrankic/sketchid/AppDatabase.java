package com.mfrankic.sketchid;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for the application.
 * Uses a singleton pattern to ensure a single database instance across the app.
 */
@Database(
    entities = {DrawingData.class, User.class, Image.class}, version = 15, exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
  private static AppDatabase instance;

  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room
          .databaseBuilder(context.getApplicationContext(),
                           AppDatabase.class,
                           "drawing_database.db"
          )
          .fallbackToDestructiveMigration()
          .build();

      new Thread(() -> {
        if (instance.imageDao().getAllImages().isEmpty()) {
          instance.imageDao().insertAll(InitialData.getImages());
        }
      }).start();
    }
    return instance;
  }

  public abstract ImageDao imageDao();

  public abstract DrawingDataDao drawingDataDao();

  public abstract UserDao userDao();
}

package com.mfrankic.sketchid;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.Executors;

@Database(
    entities = {DrawingData.class, User.class, Image.class}, version = 14, exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase instance;
  private static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
      super.onCreate(db);

      Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase database = instance;
        if (database != null && database.imageDao().getAllImages().isEmpty()) {
          List<Image> images = InitialData.getImages();
          database.imageDao().insertAll(images);
        }
      });
    }

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      super.onOpen(db);
    }
  };

  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room
          .databaseBuilder(context.getApplicationContext(),
                           AppDatabase.class,
                           "drawing_database.db"
          )
          .fallbackToDestructiveMigration()
          .addCallback(roomDatabaseCallback)
          .build();
    }
    return instance;
  }

  public abstract DrawingDataDao drawingDataDao();

  public abstract UserDao userDao();

  public abstract ImageDao imageDao();
}

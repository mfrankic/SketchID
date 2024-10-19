package com.mfrankic.sketchid;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {DrawingData.class, User.class, Image.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(1);

    public abstract DrawingDataDao drawingDataDao();

    public abstract UserDao userDao();

    public abstract ImageDao imageDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "drawing_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomDatabaseCallback)  // Add a callback for database creation
                    .build();
        }
        return instance;
    }

    // RoomDatabase.Callback to handle actions on database creation
    private static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            Log.d("AppDatabase", "Database created");
            super.onCreate(db);
            // Insert default images when the database is created
            databaseWriteExecutor.execute(() -> {
                AppDatabase database = instance;
                ImageDao imageDao = database.imageDao();

                // Insert the default images
                Image sunImage = new Image("Sun", "@drawable/sun");
                Image houseImage = new Image("House", "@drawable/house");

                imageDao.insertImage(sunImage);
                imageDao.insertImage(houseImage);
            });
        }
    };
}

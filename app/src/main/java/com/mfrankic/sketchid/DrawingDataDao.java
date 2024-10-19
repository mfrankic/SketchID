package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DrawingDataDao {

    @Insert
    void insertDrawingData(DrawingData drawingData);

    @Query("SELECT * FROM drawing_data")
    List<DrawingData> getAllDrawingData();
}

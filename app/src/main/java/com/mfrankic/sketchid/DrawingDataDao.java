package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DrawingDataDao {

    @Insert
    void insertAll(List<DrawingData> drawingDataList);

    @Query("SELECT\n" +
            "d.id, d.time, d.x, d.y, d.`action`, d.attempt, d.item_type AS itemType,\n" +
            "u.id AS userID, u.name AS userName,\n" +
            "d.image_id AS imageID, i.name AS imageName\n" +
            "FROM drawing_data d\n" +
            "LEFT JOIN user u ON d.user_id = u.id\n" +
            "LEFT JOIN image i ON d.image_id = i.id\n" +
            "WHERE u.id = :userId")
    List<DrawingExportData> getAllDrawingDataWithUsersAndImagesByUserID(int userId);

    @Query("DELETE FROM drawing_data")
    void deleteAllDrawingData();

}

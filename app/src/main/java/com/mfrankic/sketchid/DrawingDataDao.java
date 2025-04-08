package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DrawingDataDao {

  @Insert
  void insertAll(List<DrawingData> drawingDataList);

  @Query(
      "SELECT\n"
      + "d.id, d.time, d.x, d.y, d.`action`, d.attempt, d.item_type AS itemType,\n"
      + "u.id AS userID, u.name AS userName,\n"
      + "d.image_id AS imageID, i.name AS imageName\n"
      + "FROM drawing_data d\n"
      + "LEFT JOIN user u ON d.user_id = u.id\n"
      + "LEFT JOIN image i ON d.image_id = i.id\n"
      + "WHERE u.id = :userID"
  )
  List<DrawingExportData> getAllDrawingDataWithUsersAndImagesByUserID(long userID);

  @Query(
      "SELECT\n"
      + "d.id, d.time, d.x, d.y, d.`action`, d.attempt, d.item_type AS itemType,\n"
      + "u.id AS userID, u.name AS userName,\n"
      + "d.image_id AS imageID, i.name AS imageName\n"
      + "FROM drawing_data d\n"
      + "LEFT JOIN user u ON d.user_id = u.id\n"
      + "LEFT JOIN image i ON d.image_id = i.id"
  )
  List<DrawingExportData> getAllDrawingData();

  @Query("DELETE FROM drawing_data")
  void deleteAllDrawingData();

  @Query("DELETE FROM drawing_data WHERE user_id = :userID")
  int deleteDrawingDataByUserID(long userID);

  @Query("DELETE FROM drawing_data WHERE session_id = :sessionId")
  int deleteDrawingDataBySessionId(String sessionId);

  @Query("DELETE FROM drawing_data WHERE user_id = :userID AND session_id = :sessionId")
  int deleteUserSessionData(long userID, String sessionId);

  @Query("SELECT DISTINCT session_id FROM drawing_data WHERE user_id = :userID")
  List<String> getUserSessionIds(long userID);

  @Query("SELECT COUNT(*) FROM drawing_data WHERE user_id = :userID AND session_id = :sessionId")
  int getSessionDrawingCount(long userID, String sessionId);

  @Query("SELECT COUNT(*) FROM drawing_data WHERE user_id = :userID")
  int getDrawingDataCountByUserID(long userID);

}

package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link DrawingData} entities.
 * Provides methods to interact with the drawing_data table in the database.
 */
@Dao
public interface DrawingDataDao {

  /**
   * Inserts a list of drawing data records into the database.
   *
   * @param drawingDataList A list of {@link DrawingData} to insert.
   */
  @Insert
  void insertAll(List<DrawingData> drawingDataList);

  /**
   * Retrieves a list of {@link DrawingExportData} for a specific user,
   * including associated user and image names.
   * The query joins drawing_data with user and image tables to provide comprehensive export data.
   *
   * @param userID The ID of the user for whom to retrieve drawing data.
   * @return A list of {@link DrawingExportData} objects.
   */
  @Query(
      "SELECT\n"
      + "d.id, d.time, d.x, d.y, d.`action`, d.attempt, d.item_type AS itemType,\n"
      + "d.session_id AS sessionID, d.size, d.pressure, d.orientation,\n"
      + "u.id AS userID, u.name AS userName,\n"
      + "d.image_id AS imageID, i.name AS imageName\n"
      + "FROM drawing_data d\n"
      + "LEFT JOIN user u ON d.user_id = u.id\n"
      + "LEFT JOIN image i ON d.image_id = i.id\n"
      + "WHERE u.id = :userID"
  )
  List<DrawingExportData> getAllDrawingDataWithUsersAndImagesByUserID(long userID);

  /**
   * Deletes all drawing data from the database.
   * Use with caution as this will remove all drawing records.
   */
  @Query("DELETE FROM drawing_data")
  void deleteAllDrawingData();

  /**
   * Deletes all drawing data for a specific user.
   *
   * @param userID The ID of the user whose drawing data is to be deleted.
   * @return The number of rows affected.
   */
  @Query("DELETE FROM drawing_data WHERE user_id = :userID")
  int deleteDrawingDataByUserID(long userID);

  /**
   * Deletes all drawing data for a specific user and session.
   *
   * @param userID    The ID of the user.
   * @param sessionId The ID of the session.
   */
  @Query("DELETE FROM drawing_data WHERE user_id = :userID AND session_id = :sessionId")
  void deleteUserSessionData(long userID, String sessionId);

}

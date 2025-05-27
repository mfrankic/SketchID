package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link AccelerometerData} entities.
 * Provides methods to interact with the accelerometer_data table in the database.
 */
@Dao
public interface AccelerometerDataDao {

  /**
   * Inserts a list of accelerometer data records into the database.
   *
   * @param accelerometerDataList A list of {@link AccelerometerData} to insert.
   */
  @Insert
  void insertAll(List<AccelerometerData> accelerometerDataList);

  /**
   * Retrieves a list of {@link AccelerometerExportData} for a specific user,
   * including associated user and image names.
   * The query joins accelerometer_data with user and image tables to provide comprehensive
   * export data.
   *
   * @param userId The ID of the user for whom to retrieve accelerometer data.
   * @return A list of {@link AccelerometerExportData} objects.
   */
  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "a.id, a.timestamp, a.x, a.y, a.z, a.attempt, a.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "a.image_id AS imageId, i.name AS imageName\n"
      + "FROM accelerometer_data a\n"
      + "LEFT JOIN user u ON a.user_id = u.id\n"
      + "LEFT JOIN image i ON a.image_id = i.id\n"
      + "WHERE u.id = :userId"
  )
  List<AccelerometerExportData> getAccelerometerDataWithUsersAndImagesByUserId(long userId);
} 

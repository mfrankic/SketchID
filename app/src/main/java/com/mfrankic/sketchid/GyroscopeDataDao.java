package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link GyroscopeData} entities.
 * Provides methods to interact with the gyroscope_data table in the database.
 */
@Dao
public interface GyroscopeDataDao {

  /**
   * Inserts a list of gyroscope data records into the database.
   *
   * @param gyroscopeDataList A list of {@link GyroscopeData} to insert.
   */
  @Insert
  void insertAll(List<GyroscopeData> gyroscopeDataList);

  /**
   * Retrieves a list of {@link GyroscopeExportData} for a specific user,
   * including associated user and image names.
   * The query joins gyroscope_data with user and image tables to provide comprehensive export data.
   *
   * @param userId The ID of the user for whom to retrieve gyroscope data.
   * @return A list of {@link GyroscopeExportData} objects.
   */
  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "g.id, g.timestamp, g.x, g.y, g.z, g.attempt, g.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "g.image_id AS imageId, i.name AS imageName\n"
      + "FROM gyroscope_data g\n"
      + "LEFT JOIN user u ON g.user_id = u.id\n"
      + "LEFT JOIN image i ON g.image_id = i.id\n"
      + "WHERE u.id = :userId"
  )
  List<GyroscopeExportData> getGyroscopeDataWithUsersAndImagesByUserId(long userId);

  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "g.id, g.timestamp, g.x, g.y, g.z, g.attempt, g.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "g.image_id AS imageId, i.name AS imageName\n"
      + "FROM gyroscope_data g\n"
      + "LEFT JOIN user u ON g.user_id = u.id\n"
      + "LEFT JOIN image i ON g.image_id = i.id\n"
      + "WHERE u.id = :userId\n"
      + "ORDER BY g.id ASC\n"
      + "LIMIT :limit OFFSET :offset"
  )
  List<GyroscopeExportData> getPaginatedGyroscopeDataWithUsersAndImagesByUserId(
      long userId,
      int limit,
      int offset
  );
} 

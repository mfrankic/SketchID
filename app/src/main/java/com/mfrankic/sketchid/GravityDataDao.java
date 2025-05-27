package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link GravityData} entities.
 * Provides methods to interact with the gravity_data table in the database.
 */
@Dao
public interface GravityDataDao {

  /**
   * Inserts a list of gravity data records into the database.
   *
   * @param gravityDataList A list of {@link GravityData} to insert.
   */
  @Insert
  void insertAll(List<GravityData> gravityDataList);

  /**
   * Retrieves a list of {@link GravityExportData} for a specific user,
   * including associated user and image names.
   * The query joins gravity_data with user and image tables to provide comprehensive export data.
   *
   * @param userId The ID of the user for whom to retrieve gravity data.
   * @return A list of {@link GravityExportData} objects.
   */
  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "g.id, g.timestamp, g.x, g.y, g.z, g.attempt, g.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "g.image_id AS imageId, i.name AS imageName\n"
      + "FROM gravity_data g\n"
      + "LEFT JOIN user u ON g.user_id = u.id\n"
      + "LEFT JOIN image i ON g.image_id = i.id\n"
      + "WHERE u.id = :userId"
  )
  List<GravityExportData> getGravityDataWithUsersAndImagesByUserId(long userId);
} 

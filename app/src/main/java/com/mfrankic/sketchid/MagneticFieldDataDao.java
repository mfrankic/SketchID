package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link MagneticFieldData} entities.
 * Provides methods to interact with the magnetic_field_data table in the database.
 */
@Dao
public interface MagneticFieldDataDao {

  /**
   * Inserts a list of magnetic field data records into the database.
   *
   * @param magneticFieldDataList A list of {@link MagneticFieldData} to insert.
   */
  @Insert
  void insertAll(List<MagneticFieldData> magneticFieldDataList);

  /**
   * Retrieves a list of {@link MagneticFieldExportData} for a specific user,
   * including associated user and image names.
   * The query joins magnetic_field_data with user and image tables to provide comprehensive
   * export data.
   *
   * @param userId The ID of the user for whom to retrieve magnetic field data.
   * @return A list of {@link MagneticFieldExportData} objects.
   */
  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "m.id, m.timestamp, m.x, m.y, m.z, m.attempt, m.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "m.image_id AS imageId, i.name AS imageName\n"
      + "FROM magnetic_field_data m\n"
      + "LEFT JOIN user u ON m.user_id = u.id\n"
      + "LEFT JOIN image i ON m.image_id = i.id\n"
      + "WHERE u.id = :userId"
  )
  List<MagneticFieldExportData> getMagneticFieldDataWithUsersAndImagesByUserId(long userId);

  @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
  @Query(
      "SELECT\n"
      + "m.id, m.timestamp, m.x, m.y, m.z, m.attempt, m.session_id AS sessionId,\n"
      + "u.id AS userId, u.name AS userName,\n"
      + "m.image_id AS imageId, i.name AS imageName\n"
      + "FROM magnetic_field_data m\n"
      + "LEFT JOIN user u ON m.user_id = u.id\n"
      + "LEFT JOIN image i ON m.image_id = i.id\n"
      + "WHERE u.id = :userId\n"
      + "ORDER BY m.id ASC\n"
      + "LIMIT :limit OFFSET :offset"
  )
  List<MagneticFieldExportData> getPaginatedMagneticFieldDataWithUsersAndImagesByUserId(
      long userId,
      int limit,
      int offset
  );
} 

package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link MagneticFieldBaselineData} entities.
 * Provides methods to interact with the magnetic_field_baseline_data table in the database.
 */
@Dao
public interface MagneticFieldBaselineDataDao {

  /**
   * Inserts a new magnetic field baseline data record into the database.
   *
   * @param baselineData The {@link MagneticFieldBaselineData} to insert.
   */
  @Insert
  void insertBaseline(MagneticFieldBaselineData baselineData);

  /**
   * Retrieves a list of {@link MagneticFieldBaselineExportData} for a specific user,
   * including associated user and image names.
   * The results are ordered by timestamp in ascending order.
   *
   * @param userId The ID of the user for whom to retrieve baseline data.
   * @return A list of {@link MagneticFieldBaselineExportData} objects.
   */
  @Query(
      "SELECT mfbd.*, u.name as user_name, i.name as image_name "
      + "FROM magnetic_field_baseline_data mfbd "
      + "INNER JOIN user u ON mfbd.user_id = u.id "
      + "INNER JOIN image i ON mfbd.image_id = i.id "
      + "WHERE mfbd.user_id = :userId "
      + "ORDER BY mfbd.timestamp ASC"
  )
  List<MagneticFieldBaselineExportData> getBaselineDataWithUsersAndImagesByUserId(long userId);
} 

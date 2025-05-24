package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MagneticFieldBaselineDataDao {

  @Insert
  void insertBaseline(MagneticFieldBaselineData baselineData);

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

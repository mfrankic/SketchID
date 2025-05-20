package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

@Dao
public interface MagneticFieldDataDao {

  @Insert
  void insertAll(List<MagneticFieldData> magneticFieldDataList);

  @Query("SELECT * FROM magnetic_field_data WHERE user_id = :userId AND session_id = :sessionId")
  List<MagneticFieldData> getMagneticFieldDataByUserAndSession(int userId, String sessionId);

  @Query("DELETE FROM magnetic_field_data WHERE user_id = :userId AND session_id = :sessionId")
  void deleteMagneticFieldDataByUserAndSession(int userId, String sessionId);

  @Query("DELETE FROM magnetic_field_data WHERE user_id = :userId")
  void deleteMagneticFieldDataByUser(int userId);

  @Query("DELETE FROM magnetic_field_data")
  void deleteAllMagneticFieldData();

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
} 

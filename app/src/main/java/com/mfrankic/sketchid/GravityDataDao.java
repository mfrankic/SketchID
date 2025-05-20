package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

@Dao
public interface GravityDataDao {

  @Insert
  void insertAll(List<GravityData> gravityDataList);

  @Query("SELECT * FROM gravity_data WHERE user_id = :userId AND session_id = :sessionId")
  List<GravityData> getGravityDataByUserAndSession(int userId, String sessionId);

  @Query("DELETE FROM gravity_data WHERE user_id = :userId AND session_id = :sessionId")
  void deleteGravityDataByUserAndSession(int userId, String sessionId);

  @Query("DELETE FROM gravity_data WHERE user_id = :userId")
  void deleteGravityDataByUser(int userId);

  @Query("DELETE FROM gravity_data")
  void deleteAllGravityData();

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

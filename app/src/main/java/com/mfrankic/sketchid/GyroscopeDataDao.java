package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

@Dao
public interface GyroscopeDataDao {

  @Insert
  void insertAll(List<GyroscopeData> gyroscopeDataList);

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
} 

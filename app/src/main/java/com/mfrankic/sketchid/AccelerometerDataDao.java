package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

@Dao
public interface AccelerometerDataDao {

  @Insert
  void insertAll(List<AccelerometerData> accelerometerDataList);

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

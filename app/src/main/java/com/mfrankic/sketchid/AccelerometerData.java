package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "accelerometer_data")
public class AccelerometerData {

  @PrimaryKey(autoGenerate = true)
  public int id;

  @ColumnInfo(name = "user_id")
  public int userId;

  @ColumnInfo(name = "image_id")
  public int imageId;

  @ColumnInfo(name = "session_id")
  public String sessionId;

  @ColumnInfo(name = "attempt")
  public int attempt;

  @ColumnInfo(name = "timestamp")
  public long timestamp;

  @ColumnInfo(name = "x")
  public float x;

  @ColumnInfo(name = "y")
  public float y;

  @ColumnInfo(name = "z")
  public float z;

  public AccelerometerData() {
  }

  @Ignore
  public AccelerometerData(
      int userId,
      int imageId,
      String sessionId,
      int attempt,
      long timestamp,
      float x,
      float y,
      float z
  ) {
    this.userId = userId;
    this.imageId = imageId;
    this.sessionId = sessionId;
    this.attempt = attempt;
    this.timestamp = timestamp;
    this.x = x;
    this.y = y;
    this.z = z;
  }
} 

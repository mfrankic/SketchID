package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;

public class MagneticFieldBaselineExportData {
  @ColumnInfo(name = "id")
  private long id;

  @ColumnInfo(name = "user_id")
  private long userId;

  @ColumnInfo(name = "user_name")
  private String userName;

  @ColumnInfo(name = "image_id")
  private int imageId;

  @ColumnInfo(name = "image_name")
  private String imageName;

  @ColumnInfo(name = "session_id")
  private String sessionId;

  @ColumnInfo(name = "attempt")
  private int attempt;

  @ColumnInfo(name = "timestamp")
  private long timestamp;

  @ColumnInfo(name = "avg_x")
  private float avgX;

  @ColumnInfo(name = "avg_y")
  private float avgY;

  @ColumnInfo(name = "avg_z")
  private float avgZ;

  @ColumnInfo(name = "duration_ms")
  private long durationMs;

  @ColumnInfo(name = "sample_count")
  private int sampleCount;

  @ColumnInfo(name = "std_dev_x")
  private float stdDevX;

  @ColumnInfo(name = "std_dev_y")
  private float stdDevY;

  @ColumnInfo(name = "std_dev_z")
  private float stdDevZ;

  private String drawingMode = Constants.DRAWING_MODE_NORMAL;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getImageId() {
    return imageId;
  }

  public void setImageId(int imageId) {
    this.imageId = imageId;
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public int getAttempt() {
    return attempt;
  }

  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public float getAvgX() {
    return avgX;
  }

  public void setAvgX(float avgX) {
    this.avgX = avgX;
  }

  public float getAvgY() {
    return avgY;
  }

  public void setAvgY(float avgY) {
    this.avgY = avgY;
  }

  public float getAvgZ() {
    return avgZ;
  }

  public void setAvgZ(float avgZ) {
    this.avgZ = avgZ;
  }

  public long getDurationMs() {
    return durationMs;
  }

  public void setDurationMs(long durationMs) {
    this.durationMs = durationMs;
  }

  public int getSampleCount() {
    return sampleCount;
  }

  public void setSampleCount(int sampleCount) {
    this.sampleCount = sampleCount;
  }

  public float getStdDevX() {
    return stdDevX;
  }

  public void setStdDevX(float stdDevX) {
    this.stdDevX = stdDevX;
  }

  public float getStdDevY() {
    return stdDevY;
  }

  public void setStdDevY(float stdDevY) {
    this.stdDevY = stdDevY;
  }

  public float getStdDevZ() {
    return stdDevZ;
  }

  public void setStdDevZ(float stdDevZ) {
    this.stdDevZ = stdDevZ;
  }

  public String getDrawingMode() {
    return drawingMode;
  }

  public void setDrawingMode(String drawingMode) {
    this.drawingMode = drawingMode;
  }
} 

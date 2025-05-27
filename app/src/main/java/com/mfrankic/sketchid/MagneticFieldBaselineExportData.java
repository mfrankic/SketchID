package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;

/**
 * Represents magnetic field baseline data formatted for export.
 * This class is a Plain Old Java Object (POJO) used by Room queries to join data from
 * {@link MagneticFieldBaselineData}, {@link User}, and {@link Image} tables for exporting purposes.
 * It includes all fields from {@link MagneticFieldBaselineData} along with user and image names.
 */
public class MagneticFieldBaselineExportData {
  /**
   * The unique identifier of the baseline data record.
   */
  @ColumnInfo(name = "id")
  private long id;

  /**
   * The ID of the user associated with this data.
   */
  @ColumnInfo(name = "user_id")
  private long userId;

  /**
   * The name of the user.
   */
  @ColumnInfo(name = "user_name")
  private String userName;

  /**
   * The ID of the image associated with this data.
   */
  @ColumnInfo(name = "image_id")
  private int imageId;

  /**
   * The name of the image.
   */
  @ColumnInfo(name = "image_name")
  private String imageName;

  /**
   * The session identifier for this data.
   */
  @ColumnInfo(name = "session_id")
  private String sessionId;

  /**
   * The attempt number for this data.
   */
  @ColumnInfo(name = "attempt")
  private int attempt;

  /**
   * The timestamp when this data was recorded.
   */
  @ColumnInfo(name = "timestamp")
  private long timestamp;

  /**
   * The average magnetic field strength on the X-axis.
   */
  @ColumnInfo(name = "avg_x")
  private float avgX;

  /**
   * The average magnetic field strength on the Y-axis.
   */
  @ColumnInfo(name = "avg_y")
  private float avgY;

  /**
   * The average magnetic field strength on the Z-axis.
   */
  @ColumnInfo(name = "avg_z")
  private float avgZ;

  /**
   * The duration (in milliseconds) over which this baseline data was collected.
   */
  @ColumnInfo(name = "duration_ms")
  private long durationMs;

  /**
   * The number of samples collected for this baseline.
   */
  @ColumnInfo(name = "sample_count")
  private int sampleCount;

  /**
   * The standard deviation of magnetic field strength on the X-axis.
   */
  @ColumnInfo(name = "std_dev_x")
  private float stdDevX;

  /**
   * The standard deviation of magnetic field strength on the Y-axis.
   */
  @ColumnInfo(name = "std_dev_y")
  private float stdDevY;

  /**
   * The standard deviation of magnetic field strength on the Z-axis.
   */
  @ColumnInfo(name = "std_dev_z")
  private float stdDevZ;

  /**
   * The drawing mode active when this data was recorded (e.g., "normal", "overlay"). Defaults
   * to "normal".
   */
  private String drawingMode = Constants.DRAWING_MODE_NORMAL;

  /**
   * @return The unique identifier of the baseline data record.
   */
  public long getId() {
    return id;
  }

  /**
   * @param id The unique identifier to set.
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return The ID of the user associated with this data.
   */
  public long getUserId() {
    return userId;
  }

  /**
   * @param userId The user ID to set.
   */
  public void setUserId(long userId) {
    this.userId = userId;
  }

  /**
   * @return The name of the user.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName The user name to set.
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * @return The ID of the image associated with this data.
   */
  public int getImageId() {
    return imageId;
  }

  /**
   * @param imageId The image ID to set.
   */
  public void setImageId(int imageId) {
    this.imageId = imageId;
  }

  /**
   * @return The name of the image.
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * @param imageName The image name to set.
   */
  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  /**
   * @return The session identifier for this data.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * @param sessionId The session ID to set.
   */
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * @return The attempt number for this data.
   */
  public int getAttempt() {
    return attempt;
  }

  /**
   * @param attempt The attempt number to set.
   */
  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  /**
   * @return The timestamp when this data was recorded.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp The timestamp to set.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return The average magnetic field strength on the X-axis.
   */
  public float getAvgX() {
    return avgX;
  }

  /**
   * @param avgX The average X-axis magnetic field strength to set.
   */
  public void setAvgX(float avgX) {
    this.avgX = avgX;
  }

  /**
   * @return The average magnetic field strength on the Y-axis.
   */
  public float getAvgY() {
    return avgY;
  }

  /**
   * @param avgY The average Y-axis magnetic field strength to set.
   */
  public void setAvgY(float avgY) {
    this.avgY = avgY;
  }

  /**
   * @return The average magnetic field strength on the Z-axis.
   */
  public float getAvgZ() {
    return avgZ;
  }

  /**
   * @param avgZ The average Z-axis magnetic field strength to set.
   */
  public void setAvgZ(float avgZ) {
    this.avgZ = avgZ;
  }

  /**
   * @return The duration (in milliseconds) over which this baseline data was collected.
   */
  public long getDurationMs() {
    return durationMs;
  }

  /**
   * @param durationMs The duration in milliseconds to set.
   */
  public void setDurationMs(long durationMs) {
    this.durationMs = durationMs;
  }

  /**
   * @return The number of samples collected for this baseline.
   */
  public int getSampleCount() {
    return sampleCount;
  }

  /**
   * @param sampleCount The sample count to set.
   */
  public void setSampleCount(int sampleCount) {
    this.sampleCount = sampleCount;
  }

  /**
   * @return The standard deviation of magnetic field strength on the X-axis.
   */
  public float getStdDevX() {
    return stdDevX;
  }

  /**
   * @param stdDevX The standard deviation of X-axis magnetic field strength to set.
   */
  public void setStdDevX(float stdDevX) {
    this.stdDevX = stdDevX;
  }

  /**
   * @return The standard deviation of magnetic field strength on the Y-axis.
   */
  public float getStdDevY() {
    return stdDevY;
  }

  /**
   * @param stdDevY The standard deviation of Y-axis magnetic field strength to set.
   */
  public void setStdDevY(float stdDevY) {
    this.stdDevY = stdDevY;
  }

  /**
   * @return The standard deviation of magnetic field strength on the Z-axis.
   */
  public float getStdDevZ() {
    return stdDevZ;
  }

  /**
   * @param stdDevZ The standard deviation of Z-axis magnetic field strength to set.
   */
  public void setStdDevZ(float stdDevZ) {
    this.stdDevZ = stdDevZ;
  }

  /**
   * @return The drawing mode active when this data was recorded.
   */
  public String getDrawingMode() {
    return drawingMode;
  }

  /**
   * @param drawingMode The drawing mode to set.
   */
  public void setDrawingMode(String drawingMode) {
    this.drawingMode = drawingMode;
  }
} 

package com.mfrankic.sketchid;

/**
 * Represents magnetic field sensor data formatted for export.
 * This class is a Plain Old Java Object (POJO) used by Room queries to join data from
 * {@link MagneticFieldData}, {@link User}, and {@link Image} tables for exporting purposes.
 * It includes all fields from {@link MagneticFieldData} along with user name, image name, and
 * drawing mode.
 */
public class MagneticFieldExportData {
  /**
   * The unique identifier of the magnetic field data record.
   */
  private int id;
  /**
   * The ID of the user associated with this data.
   */
  private int userId;
  /**
   * The name of the user.
   */
  private String userName;
  /**
   * The ID of the image associated with this data.
   */
  private int imageId;
  /**
   * The name of the image.
   */
  private String imageName;
  /**
   * The session identifier for this data.
   */
  private String sessionId;
  /**
   * The attempt number for this data.
   */
  private int attempt;
  /**
   * The timestamp when this data was recorded.
   */
  private long timestamp;
  /**
   * Magnetic field strength on the X-axis in micro-Tesla (μT).
   */
  private float x;
  /**
   * Magnetic field strength on the Y-axis in micro-Tesla (μT).
   */
  private float y;
  /**
   * Magnetic field strength on the Z-axis in micro-Tesla (μT).
   */
  private float z;
  /**
   * The drawing mode active when this data was recorded (e.g., "normal", "overlay").
   */
  private String drawingMode;

  /**
   * @return The unique identifier of the magnetic field data record.
   */
  public int getId() {
    return id;
  }

  /**
   * @param id The unique identifier to set.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return The ID of the user associated with this data.
   */
  public int getUserId() {
    return userId;
  }

  /**
   * @param userId The user ID to set.
   */
  public void setUserId(int userId) {
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
   * @return Magnetic field strength on the X-axis in micro-Tesla (μT).
   */
  public float getX() {
    return x;
  }

  /**
   * @param x The X-axis magnetic field strength to set.
   */
  public void setX(float x) {
    this.x = x;
  }

  /**
   * @return Magnetic field strength on the Y-axis in micro-Tesla (μT).
   */
  public float getY() {
    return y;
  }

  /**
   * @param y The Y-axis magnetic field strength to set.
   */
  public void setY(float y) {
    this.y = y;
  }

  /**
   * @return Magnetic field strength on the Z-axis in micro-Tesla (μT).
   */
  public float getZ() {
    return z;
  }

  /**
   * @param z The Z-axis magnetic field strength to set.
   */
  public void setZ(float z) {
    this.z = z;
  }

  /**
   * @return The drawing mode active when this data was recorded.
   * Defaults to {@link Constants#DRAWING_MODE_NORMAL} if not explicitly set.
   */
  public String getDrawingMode() {
    return drawingMode != null ? drawingMode : Constants.DRAWING_MODE_NORMAL;
  }

  /**
   * @param drawingMode The drawing mode to set.
   */
  public void setDrawingMode(String drawingMode) {
    this.drawingMode = drawingMode;
  }
} 

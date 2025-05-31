package com.mfrankic.sketchid;

public class AccelerometerExportData implements ExportableData {
  private int id;
  private int userId;
  private String userName;
  private int imageId;
  private String imageName;
  private String sessionId;
  private int attempt;
  private long timestamp;
  private float x;
  private float y;
  private float z;
  private String drawingMode;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
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

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  @Override
  public String getDrawingMode() {
    return drawingMode != null ? drawingMode : Constants.DRAWING_MODE_NORMAL;
  }

  public void setDrawingMode(String drawingMode) {
    this.drawingMode = drawingMode;
  }
} 

package com.mfrankic.sketchid;

import androidx.room.Ignore;

public class DrawingExportData {
  private int id;
  private long time;
  private float x;
  private float y;
  private String action;
  private int userID;
  private String userName;
  private int attempt;
  private Item.Type itemType;
  private int imageID;
  private String imageName;
  private String sessionID;
  private float size;
  private float pressure;
  private float orientation;

  @Ignore
  private String drawingMode;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
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

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public int getUserID() {
    return userID;
  }

  public void setUserID(int userID) {
    this.userID = userID;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getAttempt() {
    return attempt;
  }

  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  public Item.Type getItemType() {
    return itemType;
  }

  public void setItemType(Item.Type itemType) {
    this.itemType = itemType;
  }

  public int getImageID() {
    return imageID;
  }

  public void setImageID(int imageID) {
    this.imageID = imageID;
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public String getSessionID() {
    return sessionID;
  }

  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  public float getSize() {
    return size;
  }

  public void setSize(float size) {
    this.size = size;
  }

  public float getPressure() {
    return pressure;
  }

  public void setPressure(float pressure) {
    this.pressure = pressure;
  }

  public float getOrientation() {
    return orientation;
  }

  public void setOrientation(float orientation) {
    this.orientation = orientation;
  }

  public String getDrawingMode() {
    return drawingMode != null ? drawingMode : Constants.DRAWING_MODE_NORMAL;
  }

  public void setDrawingMode(String drawingMode) {
    this.drawingMode = drawingMode;
  }
}

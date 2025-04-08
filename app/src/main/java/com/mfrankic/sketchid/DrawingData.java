package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawing_data")
public class DrawingData {

  @PrimaryKey(autoGenerate = true)
  public int id;
  @ColumnInfo(name = "time")
  public long time;
  @ColumnInfo(name = "x")
  public float x;
  @ColumnInfo(name = "y")
  public float y;
  @ColumnInfo(name = "action")
  public String action;
  @ColumnInfo(name = "user_id")
  public int userID;
  @ColumnInfo(name = "image_id")
  public int imageID;
  @ColumnInfo(name = "item_type")
  public Item.Type itemType;
  @ColumnInfo(name = "attempt")
  public int attempt;
  @ColumnInfo(name = "session_id")
  public String sessionID;

  public DrawingData(
      long time,
      float x,
      float y,
      String action,
      int userID,
      int imageID,
      Item.Type itemType,
      int attempt,
      String sessionID
  ) {
    this.time = time;
    this.x = x;
    this.y = y;
    this.action = action;
    this.userID = userID;
    this.imageID = imageID;
    this.itemType = itemType;
    this.attempt = attempt;
    this.sessionID = sessionID;
  }

  public DrawingData copy() {
    return new DrawingData(time, x, y, action, userID, imageID, itemType, attempt, sessionID);
  }
}
